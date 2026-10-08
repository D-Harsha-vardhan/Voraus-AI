-- Educaro tracker schema. Create tables, then import each CSV with Supabase Table Editor > Import data from CSV.
create table if not exists universities(
  university_id int primary key,
  university text,
  city text,
  state text,
  type text,
  public_or_private text,
  about text,
  website text,
  admissions_or_apply_link text,
  application_portal text,
  uni_assist_link text,
  image_url text,
  link_status text,
  last_verified date
);
create table if not exists programs(
  program_id int primary key,
  university_id int,
  university text,
  program text,
  field text,
  duration_semesters text,
  ects text,
  tuition_per_semester_non_eu_eur text,
  semester_contribution_eur text,
  language_of_instruction text,
  english_taught text,
  ielts_min text,
  german_requirement text,
  gre_gate_requirement text,
  aps_required text,
  uni_assist_vpd text,
  application_portal text,
  apply_or_program_link text,
  requirements_summary text,
  interview_or_test text,
  min_cgpa_numeric numeric,
  competitiveness_score_1_5 int,
  verification_status text,
  last_verified date,
  notes text
);
create table if not exists deadlines(
  deadline_id int primary key,
  program_id int,
  university text,
  program text,
  intake text,
  application_opens date,
  deadline_date date,
  applicant_group text,
  status text,
  source_link text,
  notes text
);
create table if not exists checklist_template(
  step_no int primary key,
  step_name text,
  what_to_do text,
  typical_time text,
  start_days_before_deadline int,
  linked_dataset text,
  required text,
  tip text
);

-- Per-student data (not a dataset): this is what "Track my application" reads.
create table if not exists applications(
  id bigserial primary key, user_phone text not null, program_id int references programs(program_id),
  intake text, status text default 'Not Started', started_at timestamptz, submitted_at timestamptz,
  created_at timestamptz default now());
create table if not exists application_steps(
  application_id bigint references applications(id) on delete cascade, step_no int, done boolean default false, done_at timestamptz,
  primary key(application_id, step_no));
create or replace function seed_steps() returns trigger language plpgsql as $$
begin insert into application_steps(application_id, step_no) select new.id, step_no from checklist_template; return new; end $$;
drop trigger if exists trg_seed on applications;
create trigger trg_seed after insert on applications for each row execute function seed_steps();

create or replace view v_deadline_tracker as
select a.id as application_id, a.user_phone, p.university, p.program, a.intake, a.status,
  d.deadline_date, (d.deadline_date - current_date) as days_left,
  case when d.deadline_date is null then 'Unknown'
       when d.deadline_date < current_date then 'Overdue'
       when d.deadline_date - current_date <= 45 then 'High'
       when d.deadline_date - current_date <= 120 then 'Medium' else 'Low' end as priority,
  (select count(*) from application_steps s where s.application_id=a.id and s.done) as tasks_done,
  (select count(*) from application_steps s where s.application_id=a.id) as tasks_total,
  p.apply_or_program_link as apply_link
from applications a join programs p on p.program_id=a.program_id
left join deadlines d on d.program_id=a.program_id and d.intake=a.intake;
