"""Deterministic first-year cost calculator - let CODE do the maths, not the LLM.
Usage: python finance_calc.py 1     (program_id from 34_university_fee_details)"""
import csv, sys, os
HERE = os.path.dirname(os.path.abspath(__file__))
def rows(name): return list(csv.DictReader(open(os.path.join(HERE, "csv", name + ".csv"), encoding="utf-8-sig")))
FEES  = {r["program_id"]: r for r in rows("34_university_fee_details")}
CITY  = {r["city"]: r for r in rows("33_city_cost_components")}
RATE  = float(rows("37_exchange_rates")[0]["rate"])   # replace with a live API value at runtime

def f(x): return float(x) if str(x).strip() != "" else None

def estimate(program_id, rate=RATE, semesters=2):
    fee = FEES[str(program_id)]; city = CITY.get(fee["city"])
    items, warnings = [], []
    def add(label, lo, hi, verified=True): items.append({"label": label, "eur_low": round(lo), "eur_high": round(hi), "verified": verified})
    t_lo, t_hi = f(fee["tuition_per_semester_low_eur"]), f(fee["tuition_per_semester_high_eur"])
    if t_lo is None: warnings.append("Tuition not verified for this program; excluded from the total."); add("Tuition fees", 0, 0, False)
    else: add("Tuition fees", t_lo * semesters, t_hi * semesters, fee["status"] != "to_verify")
    add("Semester contribution", f(fee["semester_contribution_low_eur"]) * semesters, f(fee["semester_contribution_high_eur"]) * semesters, False)
    if city: add("Living expenses (room, food, transport)", f(city["monthly_total_low_eur"]) * 12, f(city["monthly_total_high_eur"]) * 12, False)
    else: warnings.append("City costs unknown; using Tier 2 average."); add("Living expenses (room, food, transport)", 950 * 12, 1250 * 12, False)
    add("Health insurance", 130 * 12, 150 * 12, False)
    add("Visa & travel", 75 + 1800 / rate + 40000 / rate + 18000 / rate, 75 + 2500 / rate + 80000 / rate + 18000 / rate, False)
    add("Initial setup", 800, 1500, False)
    add("Blocked account provider fees", 100, 250, False)
    lo, hi = sum(i["eur_low"] for i in items), sum(i["eur_high"] for i in items)
    for i in items: i["inr_low"], i["inr_high"] = round(i["eur_low"] * rate), round(i["eur_high"] * rate)
    return {"program_id": program_id, "university": fee["university"], "program": fee["program"], "rate": rate,
            "total_eur_low": lo, "total_eur_high": hi, "total_inr_low": round(lo * rate), "total_inr_high": round(hi * rate),
            "breakdown": items, "warnings": warnings,
            "note": "Estimate only. EUR 11,904 blocked account is proof of funds that pays living costs; it is not added on top.",
            "source_link": fee["source_link"], "last_checked": fee["last_checked"]}

if __name__ == "__main__":
    r = estimate(sys.argv[1] if len(sys.argv) > 1 else 1)
    print(r["university"], "-", r["program"]); print(f"Total: EUR {r['total_eur_low']:,} - {r['total_eur_high']:,}  (INR {r['total_inr_low']:,} - {r['total_inr_high']:,} at {r['rate']})")
    for i in r["breakdown"]: print(f"  {i['label']:42s} EUR {i['eur_low']:>6,} - {i['eur_high']:>6,}  {'' if i['verified'] else '(estimate)'}")
    print(r["warnings"])
