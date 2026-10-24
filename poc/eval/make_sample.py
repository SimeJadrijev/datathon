"""Napravi eval/uzorak.csv: 15 nasumičnih natječaja s izvučenim vrijednostima.

Za svako polje upisuju se vrijednosti iz pravila, Haikua (ako je LLM pokrenut) i
Sonneta (ako postoji ključ, pokreće se ovdje na istom uzorku). Tim ručno upisuje
točne vrijednosti u stupce tocno_*.

    python eval/make_sample.py [--n 15] [--seed 42] [--bez-sonneta] [--prepisi]
"""
import argparse
import csv
import json
import random
import sys
from pathlib import Path

sys.path.insert(0, str(Path(__file__).resolve().parent.parent))

import config  # noqa: E402
from common import db  # noqa: E402
from extract import llm  # noqa: E402

POLJA_EVAL = ["rok_prijave", "iznos_po_korisniku_max_eur", "iznos_ukupno_eur", "ciljne_skupine", "podrucja"]
OUT = Path(__file__).with_name("uzorak.csv")


def fmt(v):
    if v is None:
        return ""
    if isinstance(v, list):
        return ";".join(sorted(map(str, v)))
    if isinstance(v, float) and v.is_integer():
        return str(int(v))
    return str(v)


def main():
    ap = argparse.ArgumentParser()
    ap.add_argument("--n", type=int, default=15)
    ap.add_argument("--seed", type=int, default=42)
    ap.add_argument("--bez-sonneta", action="store_true")
    ap.add_argument("--prepisi", action="store_true")
    a = ap.parse_args()
    if OUT.exists() and not a.prepisi:
        with OUT.open(encoding="utf-8-sig") as fh:
            if any((v or "").strip() for r in csv.DictReader(fh) for k, v in r.items() if k.startswith("tocno_")):
                sys.exit(f"{OUT} već ima ručne ispravke; dodaj --prepisi ako ga stvarno želiš prepisati.")
    con = db.connect()
    # ako je Haiku pokrenut, uzorak samo iz natječaja koje je obradio (inače usporedba nije poštena)
    llm_uvjet = " AND llm_json IS NOT NULL" if con.execute("SELECT 1 FROM natjecaji WHERE llm_json IS NOT NULL LIMIT 1").fetchone() else ""
    rows = con.execute("SELECT * FROM natjecaji WHERE length(sirovi_tekst) > 500 AND detalji_dohvaceni=1" + llm_uvjet).fetchall()
    random.Random(a.seed).shuffle(rows)
    # najviše 2 po izvoru, da uzorak pokrije različite izvore
    picked, per = [], {}
    for r in rows:
        if per.get(r["izvor"], 0) < 2:
            picked.append(r)
            per[r["izvor"]] = per.get(r["izvor"], 0) + 1
        if len(picked) == a.n:
            break
    run_sonnet = llm.available() and not a.bez_sonneta
    run_id = con.execute("SELECT MAX(run_id) FROM pokretanja").fetchone()[0]
    header = ["id", "izvor", "url", "naziv"]
    for f in POLJA_EVAL:
        header += [f"pravila_{f}", f"haiku_{f}", f"sonnet_{f}", f"tocno_{f}"]
    out_rows = []
    for r in picked:
        pr = json.loads(r["pravila_json"])
        hk = json.loads(r["llm_json"]) if r["llm_json"] else {}
        sn = {}
        if run_sonnet:
            page, _, pdf_t = (r["sirovi_tekst"] or "").partition("\n\n=== ")
            sn, usage = llm.extract(pr, page, ("=== " + pdf_t) if pdf_t else "", model=config.LLM_MODEL_COMPARE)
            llm.log_usage(con, run_id, config.LLM_MODEL_COMPARE, r["id"], usage, "eval_sonnet")
            con.commit()
            sn = sn or {}
        row = {"id": r["id"], "izvor": r["izvor"], "url": r["url"], "naziv": r["naziv"]}
        for f in POLJA_EVAL:
            row[f"pravila_{f}"] = fmt(pr.get(f))
            row[f"haiku_{f}"] = fmt(hk.get(f)) if hk else ""
            row[f"sonnet_{f}"] = fmt(sn.get(f)) if sn else ""
            row[f"tocno_{f}"] = ""
        out_rows.append(row)
    with OUT.open("w", encoding="utf-8-sig", newline="") as fh:
        wr = csv.DictWriter(fh, fieldnames=header)
        wr.writeheader()
        wr.writerows(out_rows)
    print(f"Zapisano {len(out_rows)} redaka u {OUT}. Sonnet: {'da' if run_sonnet else 'ne (nema ključa)'}.")
    print("Ispunite stupce tocno_* (liste odvojene ';', iznosi kao broj, datum YYYY-MM-DD; '-' = nema u izvoru),"
          " zatim: python eval/usporedi.py")


if __name__ == "__main__":
    main()
