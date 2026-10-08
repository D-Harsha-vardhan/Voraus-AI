# AI Advisor - low-credit workflow
1. Supabase: run supabase_schema.sql in the SQL editor.
2. pip install -r requirements.txt ; copy .env.example to .env and fill it (export the variables).
3. python load_kb.py ./germany_xlsx      (loads all datasets + pre-fills the free FAQ cache)
4. uvicorn advisor_router:app --port 8000
5. Call POST /advisor with {"question": "...", "session_id": "919...", "profile": {"course": "...", "cgpa": "7.5-8.5", "city": "Bangalore"}}
6. In DronaHQ: paste prompts/dronahq_agent_instruction.txt into the agent, upload the same datasets, copy its API/webhook URL into DRONAHQ_AGENT_URL.
DronaHQ credits are protected: DRONAHQ_MODE=fallback (default) uses DronaHQ only for small questions when Supabase finds nothing or NVIDIA fails, capped by DRONAHQ_DAILY_CAP per day. Set off to never use it.
NVIDIA returns plain English. POST /advisor/text returns only the text (for WhatsApp); POST /advisor returns JSON with the same text in "reply" plus route and suggested_replies.
Response includes "route": cache | dronahq | nvidia | handoff_no_context | limit | error  (use it to track where credits go).
