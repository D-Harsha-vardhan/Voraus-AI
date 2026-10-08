-- Run once in Supabase > SQL Editor
create table if not exists kb_chunks(
  id bigserial primary key,
  source text not null,          -- dataset name, e.g. 02_programs_masters
  title text,
  content text not null,
  tsv tsvector generated always as (to_tsvector('english', coalesce(title,'') || ' ' || content)) stored
);
create index if not exists kb_chunks_tsv on kb_chunks using gin(tsv);

create table if not exists answer_cache(
  key text primary key,          -- normalised question (+ profile if personal)
  question text,
  answer jsonb not null,
  hits int default 0,
  created_at timestamptz default now()
);

-- Free keyword search (no AI credits). Pass keywords joined with " or ".
create or replace function search_kb(q text, k int default 5)
returns table(source text, content text, rank real)
language sql stable as $$
  select source, content, ts_rank_cd(tsv, websearch_to_tsquery('english', q)) as rank
  from kb_chunks
  where tsv @@ websearch_to_tsquery('english', q)
  order by rank desc
  limit k;
$$;
-- Keep RLS ON. The server uses the service-role key; never put that key in the app/browser.
