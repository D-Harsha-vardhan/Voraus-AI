-- Finance & Visa agent. Create tables, then import each CSV (Supabase Table Editor > Import data from CSV).
create table if not exists city_costs(
  city text primary key,
  tier int,
  rent_low_eur numeric,
  rent_high_eur numeric,
  food_low_eur numeric,
  food_high_eur numeric,
  transport_low_eur numeric,
  transport_high_eur numeric,
  phone_internet_eur_low numeric,
  phone_internet_eur_high numeric,
  personal_misc_low_eur numeric,
  personal_misc_high_eur numeric,
  monthly_total_low_eur numeric,
  monthly_total_high_eur numeric,
  status text,
  basis text
);
create table if not exists university_fees(
  program_id int primary key,
  university text,
  program text,
  city text,
  tuition_per_semester_low_eur numeric,
  tuition_per_semester_high_eur numeric,
  semester_contribution_low_eur numeric,
  semester_contribution_high_eur numeric,
  semester_contribution_note text,
  transport_ticket_included text,
  fee_note text,
  source_link text,
  status text,
  last_checked text
);
create table if not exists exchange_rates(
  currency_pair text,
  rate text,
  as_of text,
  source text,
  note text
);

-- Per-student data
create table if not exists visa_applications(
  id bigserial primary key, user_phone text not null, category text default 'Student Visa (Type D)',
  status text default 'Not Started', appointment_date date, created_at timestamptz default now());
create table if not exists visa_checklist_items(
  visa_application_id bigint references visa_applications(id) on delete cascade, doc_order int, done boolean default false, done_at timestamptz,
  primary key(visa_application_id, doc_order));
create table if not exists saved_estimates(
  id bigserial primary key, user_phone text, program_id int, intake text, total_eur_low numeric, total_eur_high numeric,
  rate numeric, rate_as_of text, created_at timestamptz default now());
