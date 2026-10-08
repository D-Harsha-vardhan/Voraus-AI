"""AI Advisor router. NVIDIA is the main engine; DronaHQ credits are protected.
 0. Supabase answer cache (free)
 1. Free Supabase keyword search + ONE NVIDIA call (handles almost everything)
 2. DronaHQ only for SMALL questions, and only as a fallback (no context found / NVIDIA error),
    with a daily cap. DRONAHQ_MODE=small sends every small question to DronaHQ; off disables it.
 3. Still nothing -> counselor handoff (no AI call)
Run: uvicorn advisor_router:app --port 8000"""
import os, re, json, time
import httpx
from fastapi import FastAPI
from fastapi.responses import PlainTextResponse
from pydantic import BaseModel
from supabase import create_client

sb = create_client(os.environ["SUPABASE_URL"], os.environ["SUPABASE_SERVICE_KEY"])
NV_KEY   = os.environ["NVIDIA_API_KEY"]                      # nvapi-...
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
