"""Load your .xlsx datasets into Supabase and pre-fill the free answer cache from FAQ sheets.
Usage: python load_kb.py ./germany_xlsx"""
import os, sys, glob, re
import openpyxl
from supabase import create_client
sb = create_client(os.environ["SUPABASE_URL"], os.environ["SUPABASE_SERVICE_KEY"])
FAQ_FILES = ("01_faq_general", "19_course_specific_faq", "23_documents_upload_faq")

def norm(q): return re.sub(r"\s+", " ", re.sub(r"[^a-z0-9 ]", " ", q.lower())).strip()

def batches(rows, n=100):
    for i in range(0, len(rows), n): yield rows[i:i+n]

chunks, cache = [], []
for f in sorted(glob.glob(os.path.join(sys.argv[1], "*.xlsx"))):
    name = os.path.basename(f)[:-5]
    ws = openpyxl.load_workbook(f).active
    rows = list(ws.iter_rows(values_only=True))
    head = [str(h) for h in rows[0]]
    for r in rows[1:]:
        d = {h: str(v) for h, v in zip(head, r) if v not in (None, "")}
        if not d: continue
        title = d.get("question") or d.get("document_name") or d.get("program") or d.get("course") or d.get("topic") or d.get("item") or d.get("city") or ""
        chunks.append({"source": name, "title": title, "content": "; ".join(f"{k}: {v}" for k, v in d.items())})
        if name in FAQ_FILES and "question" in d and "answer" in d:
            cache.append({"key": norm(d["question"]), "question": d["question"], "answer": {
                "reply": d["answer"], "intent": "other", "confidence": "high", "needs_verification": True,
                "suggested_replies": [], "handoff_to_counselor": False, "sources": [name]}})
sb.table("kb_chunks").delete().neq("id", 0).execute()          # reload cleanly
for b in batches(chunks): sb.table("kb_chunks").insert(b).execute()
for b in batches(cache): sb.table("answer_cache").upsert(b).execute()
print(f"Loaded {len(chunks)} chunks, seeded {len(cache)} cached FAQ answers")
