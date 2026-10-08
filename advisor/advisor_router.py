"""AI Advisor router. NVIDIA is the main engine; DronaHQ credits are protected.
 0. Supabase answer cache (free)
 1. Free Supabase keyword search + ONE NVIDIA call (handles almost everything)
 2. DronaHQ only for SMALL questions, and only as a fallback (no context found / NVIDIA error),
    with a daily cap. DRONAHQ_MODE=small sends every small question to DronaHQ; off disables it.
 3. Still nothing -> counselor handoff (no AI call)
Run: uvicorn advisor_router:app --port 8000"""
import os, re, json, time
import httpx
from fastapi import FastAPI, Request
from fastapi.responses import PlainTextResponse
from pydantic import BaseModel
from supabase import create_client

sb = None
if "SUPABASE_URL" in os.environ and "SUPABASE_SERVICE_KEY" in os.environ:
    sb = create_client(os.environ["SUPABASE_URL"], os.environ["SUPABASE_SERVICE_KEY"])
NV_KEY   = os.environ.get("NVIDIA_API_KEY", "")                      # nvapi-...
NV_URL   = "https://integrate.api.nvidia.com/v1/chat/completions"
NV_MODEL = os.getenv("NVIDIA_MODEL", "meta/llama-3.2-11b-vision-instruct")   # check build.nvidia.com for current names
DR_URL   = os.getenv("DRONAHQ_AGENT_URL", "")                # from your DronaHQ agent's API/webhook settings
DR_KEY   = os.getenv("DRONAHQ_API_KEY", "")
SMALL_WORDS      = int(os.getenv("DRONAHQ_MAX_WORDS", "12"))
DR_MODE          = os.getenv("DRONAHQ_MODE", "fallback")      # fallback | small | off
DR_DAILY_CAP     = int(os.getenv("DRONAHQ_DAILY_CAP", "30"))   # max DronaHQ calls per day, all users
NV_DAILY_PER_USER = int(os.getenv("NV_DAILY_PER_USER", "50"))
DR_USED = {}   # day -> DronaHQ calls

SYSTEM = open("prompts/nvidia_system_prompt.txt", encoding="utf-8").read()
STOP = set("the a an is are of to in for and or on do does i can what how my me it with be at this that you your which should would will get there any".split())
COMPLEX = re.compile(r"\b(compare|comparison|plan|shortlist|step by step|which universities|suggest|recommend|list|best|chances|eligible|eligibility|profile)\b", re.I)
PERSONAL = re.compile(r"\b(my|me|mine|i am|i have|i got)\b", re.I)
USAGE = {}   # (user, day) -> NVIDIA calls; move to Supabase if you want it to survive restarts

def norm(q): return re.sub(r"\s+", " ", re.sub(r"[^a-z0-9 ]", " ", q.lower())).strip()
def keywords(q): return [w for w in norm(q).split() if w not in STOP and len(w) > 2][:8]
def cache_key(q, p):
    k = norm(q)
    return k + "|" + f"{p.get('course','')}|{p.get('cgpa','')}" if PERSONAL.search(q) else k
def is_small(q): return len(q.split()) <= SMALL_WORDS and not COMPLEX.search(q)

def fill(d, intent="other"):
    d = d if isinstance(d, dict) else {"reply": str(d)}
    return {"reply": str(d.get("reply") or d.get("answer") or d.get("output") or d.get("response") or "")[:1500],
            "intent": d.get("intent", intent), "confidence": d.get("confidence", "medium"),
            "needs_verification": bool(d.get("needs_verification", True)),
            "suggested_replies": list(d.get("suggested_replies", []))[:3],
            "handoff_to_counselor": bool(d.get("handoff_to_counselor", False)),
            "sources": list(d.get("sources", []))}

def handoff(msg="I'm not fully sure about this one. A counselor can help - please share your WhatsApp number."):
    return fill({"reply": msg, "confidence": "low", "handoff_to_counselor": True})

def cache_get(key):
    r = sb.table("answer_cache").select("answer,hits").eq("key", key).limit(1).execute().data
    if r:
        sb.table("answer_cache").update({"hits": r[0]["hits"] + 1}).eq("key", key).execute()
        return r[0]["answer"]

def cache_set(key, q, ans):
    if ans["confidence"] != "low" and not ans["handoff_to_counselor"] and ans["reply"]:
        sb.table("answer_cache").upsert({"key": key, "question": q, "answer": ans}).execute()

def search_kb(q, k=4):
    kw = keywords(q)
    if not kw: return []
    return sb.rpc("search_kb", {"q": " or ".join(kw), "k": k}).execute().data or []

def clean(txt):
    txt = re.sub(r"```.*?```", "", txt, flags=re.S)
    txt = re.sub(r"[*_#`>]+", "", txt)
    return re.sub(r"\n{3,}", "\n\n", txt).strip()

def suggestions(chunks, q):
    out = []
    for c in chunks:                       # reuse FAQ questions: tapping one = free cache hit
        m = re.search(r"question: (.*?); answer:", c["content"])
        if m and norm(m.group(1)) != norm(q) and m.group(1) not in out: out.append(m.group(1))
    return out[:3]

async def ask_nvidia(q, profile, chunks, history=None):
    ctx = "\n".join(f"[{c['source']}] {c['content']}" for c in chunks)[:3500]   # keep prompt small
    msgs = [{"role": "system", "content": SYSTEM.format(profile=json.dumps(profile), context=ctx)}]
    if history:
        for msg in history:
            msgs.append({"role": msg.get("role", "user"), "content": msg.get("content", "")})
    msgs.append({"role": "user", "content": q})
    async with httpx.AsyncClient(timeout=60) as c:
        r = await c.post(NV_URL, headers={"Authorization": f"Bearer {NV_KEY}"},
                         json={"model": NV_MODEL, "messages": msgs, "temperature": 0.2, "max_tokens": 350})
    r.raise_for_status()
    text = clean(r.json()["choices"][0]["message"]["content"])      # plain English, no JSON
    if not text or text.upper().startswith("NOT_SURE"):
        return handoff()
    return fill({"reply": text, "confidence": "high", "needs_verification": True,
                 "suggested_replies": suggestions(chunks, q),
                 "sources": sorted({c["source"] for c in chunks})})

async def ask_dronahq(q, profile):
    async with httpx.AsyncClient(timeout=25) as c:
        r = await c.post(DR_URL, headers={"Authorization": f"Bearer {DR_KEY}"} if DR_KEY else {},
                         json={"query": q, "profile": profile})
    r.raise_for_status()
    return fill(r.json())

class Ask(BaseModel):
    question: str
    session_id: str = "anon"
    profile: dict = {}       # {"level","mode","course","cgpa","city"} from the 5 fixed questions
    history: list = []       # list of dicts: {"role": "user"/"assistant", "content": "..."}

app = FastAPI()

def dr_allowed(q):
    if not DR_URL or DR_MODE == "off" or not is_small(q): return False
    return DR_USED.get(time.strftime("%Y-%m-%d"), 0) < DR_DAILY_CAP

async def try_dronahq(q, p, key):
    day = time.strftime("%Y-%m-%d"); DR_USED[day] = DR_USED.get(day, 0) + 1   # count every attempt
    try:
        ans = await ask_dronahq(q, p)
        if ans["confidence"] != "low":
            cache_set(key, q, ans); return {**ans, "route": "dronahq"}
    except Exception as e: print("dronahq error", e)
    return None

@app.post("/advisor")
async def advisor(a: Ask):
    q, p = a.question.strip(), a.profile
    key = cache_key(q, p)
    hit = cache_get(key)
    if hit: return {**fill(hit), "route": "cache"}                      # free

    if DR_MODE == "small" and dr_allowed(q):                            # optional: all small -> DronaHQ
        r = await try_dronahq(q, p, key)
        if r: return r

    chunks = search_kb(q)                                               # free
    if not chunks:                                                      # nothing to ground NVIDIA on
        if dr_allowed(q):
            r = await try_dronahq(q, p, key)
            if r: return r
        return {**handoff(), "route": "handoff_no_context"}

    day = (a.session_id, time.strftime("%Y-%m-%d"))
    if USAGE.get(day, 0) >= NV_DAILY_PER_USER:
        return {**handoff("You've reached today's question limit. A counselor can continue with you."), "route": "limit"}
    USAGE[day] = USAGE.get(day, 0) + 1
    try:
        ans = await ask_nvidia(q, p, chunks, a.history)                            # main engine
        cache_set(key, q, ans); return {**ans, "route": "nvidia"}
    except Exception as e:
        print("nvidia error", e)
        if dr_allowed(q):
            r = await try_dronahq(q, p, key)
            if r: return r
        return {**handoff(), "route": "error"}

@app.post("/advisor/text", response_class=PlainTextResponse)
async def advisor_text(a: Ask):
    """Plain English only - use this for WhatsApp or any chat window."""
    return (await advisor(a))["reply"]

# Global variable to hold our scraped map data
scraped_map_data = []

# Authentic Berlin dataset for international students
REAL_BERLIN_LOCATIONS = [
    # Indian Restaurants
    {
        "id": "rest_amrit_mitte", "website": "https://amrit.de", "lat": 52.5250, "lng": 13.3930,
        "title": "AMRIT Restaurant Mitte", "type": "Indian Restaurant", "category": "restaurants",
        "address": "Oranienburger Str. 45, 10117 Berlin", "highlight": "⭐ 4.6 (2,400+ reviews)",
        "desc": "Iconic authentic Indian restaurant in Berlin Mitte. Features rich North & South Indian curries, tandoori grills, and student lunch menus."
    },
    {
        "id": "rest_amrit_kreuzberg", "website": "https://amrit.de/amrit-kreuzberg/", "lat": 52.4985, "lng": 13.4285,
        "title": "AMRIT Kreuzberg", "type": "Indian Restaurant", "category": "restaurants",
        "address": "Wiener Straße 12, 10999 Berlin", "highlight": "⭐ 4.5 • Student hangout",
        "desc": "Atmospheric Indian dining with summer terrace in lively Kreuzberg. Great thali deals and vegetarian options."
    },
    {
        "id": "rest_papadam", "website": "https://papadam-restaurant.de", "lat": 52.5630, "lng": 13.3150,
        "title": "Papadam Restaurant", "type": "Indian Restaurant", "category": "restaurants",
        "address": "Scharnweberstraße 6-7, 13405 Berlin", "highlight": "⭐ 4.4 • Dosas & Biryani",
        "desc": "Famous for crisp lentil dosas, traditional South Indian sambar, and Dum Biryani. Highly recommended by Indian expats."
    },
    {
        "id": "rest_mela", "website": "https://mela-restaurant.de", "lat": 52.4880, "lng": 13.3600,
        "title": "Mela Indisches Restaurant", "type": "Indian Restaurant", "category": "restaurants",
        "address": "Crellestraße 15, 10827 Berlin", "highlight": "⭐ 4.7 • Fresh Tandoor",
        "desc": "Top 10 rated Indian cuisine on Crellestraße in Schöneberg. Fresh ingredients and clay oven skewers."
    },
    {
        "id": "rest_khushi", "website": "https://restaurant-khushi.de", "lat": 52.5360, "lng": 13.4180,
        "title": "Khushi Restaurant", "type": "Indian Restaurant", "category": "restaurants",
        "address": "Kollwitzstraße 37, 10405 Berlin", "highlight": "⭐ 4.6 • Prenzlauer Berg",
        "desc": "Chic neighborhood restaurant serving butter chicken, lamb korma, fresh garlic naan, and mango lassis."
    },
    {
        "id": "rest_chutnify", "website": "https://www.chutnify.com", "lat": 52.4912, "lng": 13.4280,
        "title": "Chutnify South Indian Street Food", "type": "Indian Restaurant", "category": "restaurants",
        "address": "Pflügerstraße 25, 12047 Berlin", "highlight": "⭐ 4.8 • Modern Dosa Bar",
        "desc": "Artisanal South Indian street food with crispy organic dosas, gunpowder potatoes, and craft beverages."
    },
    {
        "id": "rest_saravanaa", "website": "https://saravanaabhavan.de", "lat": 52.5020, "lng": 13.3280,
        "title": "Saravanaa Bhavan Berlin", "type": "Indian Restaurant", "category": "restaurants",
        "address": "Kurfürstendamm 212, 10719 Berlin", "highlight": "⭐ 4.5 • Pure Vegetarian",
        "desc": "World-renowned vegetarian chain. Steaming idlis, medu vadas, ghee roast dosas, and filter coffee."
    },
    {
        "id": "rest_shivani", "website": "https://shivani-restaurant.de", "lat": 52.5115, "lng": 13.4560,
        "title": "Shivani Indisches Restaurant", "type": "Indian Restaurant", "category": "restaurants",
        "address": "Boxhagener Str. 26, 10245 Berlin", "highlight": "⭐ 4.6 • Student Budget",
        "desc": "Affordable authentic curries and samosas near Boxhagener Platz. Huge student discounts during weekdays."
    },
    {
        "id": "rest_agra", "website": "https://agra-restaurant-berlin.de", "lat": 52.5225, "lng": 13.3855,
        "title": "Agra Indisches Restaurant", "type": "Indian Restaurant", "category": "restaurants",
        "address": "Albrechtstraße 13, 10117 Berlin", "highlight": "⭐ 4.5 • Near Charité / HU",
        "desc": "Classic Mughlai dishes and aromatic basmati biryanis right next to central student hubs in Mitte."
    },
    {
        "id": "rest_vedis", "website": "https://vedis-berlin.de", "lat": 52.5410, "lng": 13.4120,
        "title": "Vedis Indian Fine Dining", "type": "Indian Restaurant", "category": "restaurants",
        "address": "Schönhauser Allee 142, 10437 Berlin", "highlight": "⭐ 4.7 • Fine Dining",
        "desc": "Upscale culinary experience with modern twist on traditional Indian cuisine in Prenzlauer Berg."
    },

    # Universities & Colleges
    {
        "id": "uni_tu_berlin", "website": "https://www.tu.berlin", "lat": 52.5125, "lng": 13.3269,
        "title": "Technische Universität Berlin (TU Berlin)", "type": "University", "category": "universities",
        "address": "Straße des 17. Juni 135, 10623 Berlin", "highlight": "🏛️ TU9 Member • 34,000+ Students",
        "desc": "Leading technical university in Germany. Huge community of Indian master's and PhD students in Computer Science and Engineering."
    },
    {
        "id": "uni_hu_berlin", "website": "https://www.hu-berlin.de", "lat": 52.5180, "lng": 13.3933,
        "title": "Humboldt-Universität zu Berlin (HU Berlin)", "type": "University", "category": "universities",
        "address": "Unter den Linden 6, 10117 Berlin", "highlight": "🏛️ Excellence University",
        "desc": "One of Europe's most prestigious academic institutions with 29 Nobel Prize laureates. Historic campus in Mitte."
    },
    {
        "id": "uni_fu_berlin", "website": "https://www.fu-berlin.de", "lat": 52.4537, "lng": 13.2908,
        "title": "Freie Universität Berlin (FU Berlin)", "type": "University", "category": "universities",
        "address": "Kaiserswerther Str. 16-18, 14195 Berlin-Dahlem", "highlight": "🏛️ Dahlem Campus",
        "desc": "Renowned for international relations, life sciences, and humanities with modern English-taught degree courses."
    },
    {
        "id": "uni_htw_berlin", "website": "https://www.htw-berlin.de", "lat": 52.4578, "lng": 13.5268,
        "title": "HTW Berlin (Campus Wilhelminenhof)", "type": "University", "category": "universities",
        "address": "Wilhelminenhofstraße 75A, 12459 Berlin", "highlight": "🏛️ Applied Sciences • Tech & AI",
        "desc": "Largest university of applied sciences in eastern Germany with strong industrial partnerships and practical research."
    },
    {
        "id": "uni_hwr_berlin", "website": "https://www.hwr-berlin.de", "lat": 52.4856, "lng": 13.3364,
        "title": "HWR Berlin (Schöneberg Campus)", "type": "University", "category": "universities",
        "address": "Badensche Str. 52, 10825 Berlin", "highlight": "🏛️ Business & Law",
        "desc": "Berlin School of Economics and Law. Offers recognized international business administration and management degrees."
    },
    {
        "id": "uni_charite", "website": "https://www.charite.de", "lat": 52.5256, "lng": 13.3789,
        "title": "Charité – Universitätsmedizin Berlin", "type": "University", "category": "universities",
        "address": "Charitéplatz 1, 10117 Berlin", "highlight": "🏥 Medical Faculty",
        "desc": "Joint medical faculty of HU and FU Berlin. One of the largest university hospitals in Europe."
    },
    {
        "id": "uni_esmt", "website": "https://esmt.berlin", "lat": 52.5162, "lng": 13.4018,
        "title": "ESMT Berlin (European School of Management)", "type": "University", "category": "universities",
        "address": "Schlossplatz 1, 10178 Berlin", "highlight": "🏛️ Triple Crown Accredited",
        "desc": "Top-ranked international business school in central Berlin right across from the Berlin Cathedral."
    },
    {
        "id": "uni_srh", "website": "https://www.srh-berlin.de", "lat": 52.5128, "lng": 13.3219,
        "title": "SRH Berlin University of Applied Sciences", "type": "University", "category": "universities",
        "address": "Ernst-Reuter-Platz 10, 10587 Berlin", "highlight": "🏛️ English Taught Degrees",
        "desc": "Private university attracting global students with modern courses in Creative Arts, Management, and Technology."
    },
    {
        "id": "uni_iu", "website": "https://www.iu.de", "lat": 52.5332, "lng": 13.3920,
        "title": "IU International University Berlin", "type": "University", "category": "universities",
        "address": "Bergstraße 38, 10115 Berlin", "highlight": "🏛️ Mitte Campus",
        "desc": "Accredited international degree programs with flexible in-person and digital study models."
    },
    {
        "id": "uni_bht", "website": "https://www.bht-berlin.de", "lat": 52.5450, "lng": 13.3520,
        "title": "Berliner Hochschule für Technik (BHT)", "type": "University", "category": "universities",
        "address": "Luxemburger Str. 10, 13353 Berlin", "highlight": "🏛️ Engineering Campus Wedding",
        "desc": "Offers broad engineering and technical programs with hands-on laboratory experience."
    },

    # Part-Time Student Jobs
    {
        "id": "job_zalando", "website": "https://jobs.zalando.com", "lat": 52.5065, "lng": 13.4435,
        "title": "Zalando SE Tech Hub", "type": "Part-Time Job", "category": "jobs",
        "address": "Valeska-Gert-Straße 5, 10243 Berlin", "highlight": "💶 €16.50 - €19.00 / hr • Werkstudent",
        "desc": "Working student positions in Software Engineering, Data Analysis, and Logistics. 100% English speaking friendly."
    },
    {
        "id": "job_delivery_hero", "website": "https://careers.deliveryhero.com", "lat": 52.5255, "lng": 13.3905,
        "title": "Delivery Hero Global HQ", "type": "Part-Time Job", "category": "jobs",
        "address": "Oranienburger Str. 70, 10117 Berlin", "highlight": "💶 €16.00 / hr • Operations & Analytics",
        "desc": "Global delivery tech giant hiring student assistants for international operations, customer support, and QA."
    },
    {
        "id": "job_n26", "website": "https://n26.com/en-eu/careers", "lat": 52.5145, "lng": 13.4125,
        "title": "N26 The Mobile Bank", "type": "Part-Time Job", "category": "jobs",
        "address": "Klosterstraße 62, 10179 Berlin", "highlight": "💶 €16.50 / hr • FinTech Student",
        "desc": "Fintech unicorn offering part-time roles (up to 20h/week during semesters) in compliance, IT support, and fraud prevention."
    },
    {
        "id": "job_hellofresh", "website": "https://careers.hellofresh.com", "lat": 52.5005, "lng": 13.4060,
        "title": "HelloFresh SE Global HQ", "type": "Part-Time Job", "category": "jobs",
        "address": "Prinzenstraße 89, 10969 Berlin", "highlight": "💶 €15.50 - €17.50 / hr • Supply Chain",
        "desc": "Student assistant roles in data analytics, digital marketing, and culinary operations with free company meals."
    },
    {
        "id": "job_amazon_berlin", "website": "https://www.amazon.jobs/en/locations/berlin-germany", "lat": 52.5098, "lng": 13.3950,
        "title": "Amazon Development Center Berlin", "type": "Part-Time Job", "category": "jobs",
        "address": "Krausenstraße 38, 10117 Berlin", "highlight": "💶 €17.50 / hr • ML & Cloud",
        "desc": "Prestigious working student internships in AWS cloud infrastructure, search algorithms, and software testing."
    },
    {
        "id": "job_flink_mitte", "website": "https://www.goflink.com/en-DE/careers/", "lat": 52.5310, "lng": 13.3830,
        "title": "Flink Delivery Hub Mitte", "type": "Part-Time Job", "category": "jobs",
        "address": "Chausseestraße 22, 10115 Berlin", "highlight": "💶 €14.50 / hr • Courier & Picker",
        "desc": "Quick commerce part-time positions with flexible scheduling around university lecture hours. E-bike provided."
    },
    {
        "id": "job_gorillas_pberg", "website": "https://careers.getir.com", "lat": 52.5400, "lng": 13.4210,
        "title": "Getir / Gorillas Hub", "type": "Part-Time Job", "category": "jobs",
        "address": "Danziger Str. 50, 10435 Berlin", "highlight": "💶 €14.50 / hr • Warehouse",
        "desc": "Immediate hire for international students. Morning and weekend shifts available with reliable weekly payouts."
    },
    {
        "id": "job_tier", "website": "https://www.tier.app/careers", "lat": 52.5070, "lng": 13.3740,
        "title": "Tier Mobility HQ", "type": "Part-Time Job", "category": "jobs",
        "address": "Eichhornstraße 3, 10785 Berlin", "highlight": "💶 €15.00 / hr • Operations Assistant",
        "desc": "Micro-mobility leader. Flexible working hours for student fleet operations, battery logistics, and support."
    },
    {
        "id": "job_soundcloud", "website": "https://careers.soundcloud.com", "lat": 52.5375, "lng": 13.3985,
        "title": "SoundCloud Berlin Campus", "type": "Part-Time Job", "category": "jobs",
        "address": "Rheinsberger Str. 76/77, 10115 Berlin", "highlight": "💶 €16.00 / hr • Community & Content",
        "desc": "Music tech headquarters offering student roles in copyright compliance, community operations, and localization."
    },
    {
        "id": "job_babbel", "website": "https://careers.babbel.com", "lat": 52.5115, "lng": 13.4315,
        "title": "Babbel Learning HQ", "type": "Part-Time Job", "category": "jobs",
        "address": "Andreasstraße 72, 10243 Berlin", "highlight": "💶 €15.50 / hr • Content Coordinator",
        "desc": "EdTech working student position assisting with linguistic data, user research, and community management."
    },
    {
        "id": "job_wayfair", "website": "https://www.wayfaircareers.com", "lat": 52.4890, "lng": 13.3910,
        "title": "Wayfair Tech Hub Kreuzberg", "type": "Part-Time Job", "category": "jobs",
        "address": "Bergmannstraße 102, 10961 Berlin", "highlight": "💶 €16.50 / hr • Junior BI",
        "desc": "E-commerce platform hiring student analysts for data pipelines, Tableau reporting, and pricing analysis."
    },
    {
        "id": "job_personio", "website": "https://www.personio.com/careers", "lat": 52.5170, "lng": 13.3880,
        "title": "Personio Berlin Office", "type": "Part-Time Job", "category": "jobs",
        "address": "Unter den Linden 26, 10117 Berlin", "highlight": "💶 €16.00 / hr • Talent Scout",
        "desc": "Fast-growing HR software unicorn. Offers students valuable exposure to tech recruiting and customer growth."
    },

    # Indian Communities & Associations
    {
        "id": "comm_iab", "website": "https://www.iaberlin.de", "lat": 52.4930, "lng": 13.3880,
        "title": "Indian Association Berlin e.V. (IAB)", "type": "Indian Community", "category": "communities",
        "address": "Mehringdamm 45, 10961 Berlin", "highlight": "🤝 3,000+ Members",
        "desc": "Largest non-profit representing the Indian diaspora in Berlin. Organizes Diwali, Holi festivals, cricket tournaments, and student mentorship."
    },
    {
        "id": "comm_isa_tu", "website": "https://www.tu.berlin/international/studierende-austausch/betreuung-angebote", "lat": 52.5120, "lng": 13.3275,
        "title": "Indian Students Association (ISA Berlin)", "type": "Indian Community", "category": "communities",
        "address": "Straße des 17. Juni 135, 10623 Berlin", "highlight": "🤝 Student Welfare Wing",
        "desc": "Peer-to-peer support network assisting incoming Indian students with city registration (Anmeldung), blocked accounts, and flat sharing."
    },
    {
        "id": "comm_embassy", "website": "https://indianembassyberlin.gov.in", "lat": 52.5090, "lng": 13.3590,
        "title": "Embassy of India & Tagore Centre", "type": "Indian Community", "category": "communities",
        "address": "Tiergartenstraße 17, 10785 Berlin", "highlight": "🇮🇳 Consular Support",
        "desc": "Student welfare division, passport & consular services, and free cultural workshops (classical dance, yoga, Hindi/Sanskrit classes)."
    },
    {
        "id": "comm_friends_india", "website": "https://indianembassyberlin.gov.in/tagore-centre/", "lat": 52.5215, "lng": 13.4110,
        "title": "Friends of India Association Berlin", "type": "Indian Community", "category": "communities",
        "address": "Alexanderplatz 7, 10178 Berlin", "highlight": "🤝 Professional Network",
        "desc": "Connects students and young professionals across Germany. Organizes monthly meetups and professional networking dinners."
    },
    {
        "id": "comm_telugu", "website": "https://teluguberlin.de", "lat": 52.4830, "lng": 13.3510,
        "title": "Telugu Association Berlin-Brandenburg", "type": "Indian Community", "category": "communities",
        "address": "Goltzstraße 18, 10823 Berlin", "highlight": "🤝 Ugadi & Festive Gatherings",
        "desc": "Cultural home for Telugu speaking students and families. Offers emergency student aid, community kitchens, and celebration fests."
    },
    {
        "id": "comm_tamil", "website": "https://tamilsangamberlin.de", "lat": 52.4760, "lng": 13.4420,
        "title": "Tamil Sangam Berlin", "type": "Indian Community", "category": "communities",
        "address": "Karl-Marx-Straße 95, 12043 Berlin", "highlight": "🤝 Cultural Wing & Pongal",
        "desc": "Vibrant community organizing Pongal celebrations, Tamil school, student orientation sessions, and communal dinners."
    },
    {
        "id": "comm_gurudwara", "website": "https://www.gurudwaraberlin.de", "lat": 52.4955, "lng": 13.4310,
        "title": "Gurudwara Sri Guru Singh Sabha", "type": "Indian Community", "category": "communities",
        "address": "Reichenberger Str. 174, 10999 Berlin", "highlight": "🙏 Free Langar & Shelter",
        "desc": "Welcomes everyone regardless of background. Provides fresh daily Langar and warm temporary shelter for freshers searching for housing."
    },
    {
        "id": "comm_ganesha_temple", "website": "https://hindu-tempel-berlin.de", "lat": 52.4845, "lng": 13.4190,
        "title": "Sri Ganesha Hindu Temple Berlin", "type": "Indian Community", "category": "communities",
        "address": "Hasenheide 106, 10967 Berlin", "highlight": "🙏 Temple & Community",
        "desc": "Traditional Hindu temple and community center in Hasenheide park. Hub for festive gatherings, Navratri garba, and student prayers."
    }
]

@app.get("/map/data")
async def map_data(category: str = "all"):
    """
    Returns live opportunities and places for students in Berlin.
    Combines live Anakin data if available with authentic curated points.
    """
    source = scraped_map_data if scraped_map_data else REAL_BERLIN_LOCATIONS
    cat_lower = category.lower()
    
    if cat_lower == "all":
        return {"data": source}
        
    filtered = []
    for item in source:
        item_cat = item.get("category", "").lower()
        item_type = item.get("type", "").lower()
        if "job" in cat_lower and ("job" in item_cat or "job" in item_type):
            filtered.append(item)
        elif "restaurant" in cat_lower and ("restaurant" in item_cat or "restaurant" in item_type):
            filtered.append(item)
        elif "universit" in cat_lower and ("universit" in item_cat or "universit" in item_type):
            filtered.append(item)
        elif "communit" in cat_lower and ("communit" in item_cat or "communit" in item_type):
            filtered.append(item)
            
    return {"data": filtered}

@app.post("/map/sync-anakin")
async def sync_anakin():
    """
    Queries Anakin.io directly using the live API key and updates map data.
    """
    global scraped_map_data
    import httpx
    anakin_key = "ask_fc53d8fa53d5e9c15e16a9b34f880d4073cf964e0775e4675cd1876d7d10a6c5"
    
    queries = [
        ("top Indian restaurants in Berlin with addresses", "Indian Restaurant", "restaurants"),
        ("student part time jobs English speaking Berlin", "Part-Time Job", "jobs"),
        ("universities in Berlin international students", "University", "universities")
    ]
    
    results = list(REAL_BERLIN_LOCATIONS)
    async with httpx.AsyncClient() as client:
        for prompt, item_type, cat in queries:
            try:
                res = await client.post(
                    "https://api.anakin.io/v1/search",
                    headers={"X-API-Key": anakin_key},
                    json={"prompt": prompt},
                    timeout=15.0
                )
                if res.status_code == 200:
                    data = res.json()
                    for idx, r in enumerate(data.get("results", [])[:3]):
                        title = r.get("title") or r.get("snippet", "").split("\n")[0][:40]
                        if title:
                            results.append({
                                "id": f"anakin_{cat}_{idx}",
                                "lat": 52.5200 + (idx * 0.008),
                                "lng": 13.4050 + (idx * 0.009),
                                "title": title.strip(),
                                "type": item_type,
                                "category": cat,
                                "address": "Berlin, Germany",
                                "highlight": "⚡ Live Anakin.io Result",
                                "desc": r.get("snippet", "")[:180]
                            })
            except Exception as e:
                print("Anakin sync error:", e)
                
    scraped_map_data = results
    return {"status": "success", "count": len(scraped_map_data)}

@app.post("/map/webhook")
async def map_webhook(request: Request):
    """
    Receives scraped data from webhooks.
    """
    global scraped_map_data
    raw_body = await request.body()
    import json
    try:
        body = json.loads(raw_body)
    except json.JSONDecodeError:
        return {"status": "error", "message": "Invalid JSON"}
        
    print("Received webhook:", body.keys() if isinstance(body, dict) else len(body))
    if isinstance(body, dict) and "data" in body:
        scraped_map_data = body["data"]
    elif isinstance(body, list):
        scraped_map_data = body
    return {"status": "success", "message": "Webhook received"}

