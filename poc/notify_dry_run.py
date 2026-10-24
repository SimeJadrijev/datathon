"""Test obavijesti bez slanja e-maila.

Simulira dva pokretanja nad stvarnim podacima iz baze:
  - pokretanje A = stanje prije N dana (natječaji objavljeni u zadnjih N dana se "još ne vide"),
  - pokretanje B = današnje stanje.
Natječaji iz B koji nisu u A su "novi"; za svakog korisnika (profili iz profiles/)
ispisuje se koje bi nove natječaje dobio u obavijesti. Ništa se ne šalje.

    python notify_dry_run.py              # N = 14 dana
    python notify_dry_run.py --dana 30
    python notify_dry_run.py --stvarno    # usporedi zadnja dva stvarna pokretanja run_all.py (prvi_run_id)
"""
import argparse
from datetime import date, timedelta

from common import db
from match import PROFILI, load_profile, match


def main():
    ap = argparse.ArgumentParser()
    ap.add_argument("--dana", type=int, default=14)
    ap.add_argument("--stvarno", action="store_true")
    a = ap.parse_args()
    con = db.connect()
    recs = db.all_records(con)
    if a.stvarno:
        runs = [r[0] for r in con.execute("SELECT run_id FROM pokretanja ORDER BY run_id DESC LIMIT 2")]
        if len(runs) < 2:
            raise SystemExit("Trebaju barem dva pokretanja run_all.py")
        novi_ids = {r[0] for r in con.execute("SELECT id FROM natjecaji WHERE prvi_run_id=?", (runs[0],))}
        print(f"Stvarna usporedba: pokretanje {runs[1]} -> {runs[0]}")
    else:
        granica = (date.today() - timedelta(days=a.dana)).isoformat()
        stari = {r["id"] for r in recs if (r["datum_objave"] or "0000") < granica}
        novi_ids = {r["id"] for r in recs} - stari
        print(f"Simulacija: pokretanje A = stanje na {granica} ({len(stari)} natječaja), "
              f"pokretanje B = danas ({len(recs)} natječaja)")
    novi = [r for r in recs if r["id"] in novi_ids]
    print(f"Novih natječaja u B: {len(novi)}")
    for r in sorted(novi, key=lambda r: r["datum_objave"] or "")[-15:]:
        print(f"  + {r['datum_objave']}  [{r['izvor']}] {r['naziv'][:90]}")

    print("\n=== Obavijesti koje bi se poslale (DRY RUN, ništa se ne šalje) ===")
    ukupno = 0
    for pf in PROFILI:
        p = load_profile(pf)
        hits = match(p, novi, top=5)
        if not hits:
            print(f"\n[{p.get('naziv')}] nema novih relevantnih natječaja -> bez obavijesti")
            continue
        ukupno += 1
        print(f"\n[{p.get('naziv')}] -> obavijest s {len(hits)} natječaja:")
        for h in hits:
            print(f"   - {h['naziv'][:90]} ({h['izvor']}, rok {h['rok_prijave'] or '?'})")
            print(f"     zašto: {'; '.join(h['razlozi'])}")
    print(f"\nKorisnika za obavijestiti: {ukupno} od {len(PROFILI)}")


if __name__ == "__main__":
    main()
