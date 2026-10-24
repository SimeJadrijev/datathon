"""Cijeli POC: svi izvori -> pravila -> (LLM) -> SQLite -> otvoreni podaci -> podudaranje -> IZVJESTAJ.md

    python run_all.py                     # sve, LLM na 5 natječaja (ako postoji ključ)
    python run_all.py --llm-limit 300     # skaliraj LLM nakon provjere na 5
    python run_all.py --izvori fzoeu,hzz  # samo neki izvori
    python run_all.py --refresh           # ignoriraj HTML cache (PDF-ovi se nikad ne preuzimaju ponovno)
    python run_all.py --samo-izvjestaj    # samo ponovno generiraj IZVJESTAJ.md iz baze
"""
import argparse
import json
import sys
import threading
import time
import traceback
from concurrent.futures import ThreadPoolExecutor, as_completed
from datetime import datetime

import config
from common import db
from common.http import get_client, set_refresh
from extract import llm, pdf, rules
from sources import IZVORI

STATUS_RED = {"otvoren": 0, "najavljen": 1, None: 2, "zatvoren": 3}


def hints_of(item) -> list[str]:
    h = []
    if item.status_hint:
        h.append("status")
    if item.rok_hint:
        h.append("rok_prijave")
    if item.datum_objave:
        h.append("datum_objave")
    if item.zupanije_hint:
        h.append("zupanije")
    if item.davatelj:
        h.append("davatelj")
    h += list(item.extra.get("polja", {}))
    return h


def run_source(kategorija: str, mod) -> dict:
    """Izvodi jedan izvor do kraja; nikad ne baca iznimku (greška ide u log)."""
    t0 = time.monotonic()
    log = {"izvor": mod.IZVOR, "kategorija": kategorija, "metoda": mod.METODA,
           "pocetak": datetime.now().isoformat(timespec="seconds"), "greske": [], "napomena": {}}
    items = []
    try:
        items = mod.fetch_list()
    except Exception as e:
        log["greske"].append(f"fetch_list: {type(e).__name__}: {e}")
        traceback.print_exc()
    log["broj_popis"] = len(items)
    if items:
        log["napomena"].update({k: v for k, v in items[0].extra.items()
                                if k in ("ukupno_na_izvoru", "preskoceno_nije_financiranje", "rss_stavki")})
    # detalji: najprije otvoreni i najnoviji
    items.sort(key=lambda i: (STATUS_RED.get(i.status_hint, 2), -(int((i.datum_objave or "0000").replace("-", "")[:8] or 0))))
    n_det = n_pdf_items = n_pdf = 0
    for i, it in enumerate(items):
        if i < config.MAX_DETAILS_PER_SOURCE:
            try:
                mod.fetch_detail(it)
            except Exception as e:
                log["greske"].append(f"detalj {it.id}: {type(e).__name__}: {str(e)[:120]}")
            if it.detalji_dohvaceni:
                n_det += 1
            if n_pdf_items < config.MAX_PDF_ITEMS_PER_SOURCE and pdf.pick(it.prilozi):
                k, errs = pdf.download(it)
                n_pdf += k
                n_pdf_items += 1 if k else 0
                log["greske"] += errs[:3]
    ts = get_client().thread_stats[threading.current_thread().name]
    log.update({"broj_detalji": n_det, "broj_pdf": n_pdf, "trajanje_s": round(time.monotonic() - t0, 1),
                "http_zahtjeva": ts["zahtjeva"], "cache_pogodaka": ts["cache"]})
    recs = []
    for it in items:
        rec = rules.extract(it)
        recs.append((rec, it, hints_of(it)))
    log["recs"] = recs
    print(f"  [{mod.IZVOR}] popis {len(items)}, detalji {n_det}, PDF {n_pdf}, {log['trajanje_s']} s, greške {len(log['greske'])}",
          flush=True)
    return log


def save(con, run_id: int, logs: list[dict]):
    for log in logs:
        for rec, it, hints in log.pop("recs"):
            sirovi = (it.tekst + "\n\n" + it.pdf_tekst)[:80000]
            extra = {
                "sirovi_tekst": sirovi, "detalji_dohvaceni": int(it.detalji_dohvaceni),
                "pdf_preuzeto": it.pdf_preuzeto,
                "pravila_json": json.dumps({**rec, "_hints": hints, "_kategorije": it.kategorije}, ensure_ascii=False),
            }
            # zadrži LLM rezultat ako se tekst nije promijenio (ne plaćamo isto izvlačenje dvaput)
            prev = con.execute("SELECT llm_json, sirovi_tekst_hash FROM natjecaji WHERE id=?", (rec["id"],)).fetchone()
            if prev and prev["llm_json"] and prev["sirovi_tekst_hash"] == rec["sirovi_tekst_hash"]:
                rec = llm.merge(rec, json.loads(prev["llm_json"]), set(hints))
                extra["llm_json"] = prev["llm_json"]
            db.upsert(con, rec, run_id, extra)
        con.execute("INSERT INTO izvori_log VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?)", (
            run_id, log["izvor"], log["kategorija"], log["metoda"], log["pocetak"], log["trajanje_s"],
            log["broj_popis"], log["broj_detalji"], log["broj_pdf"],
            log["http_zahtjeva"], log["cache_pogodaka"],
            json.dumps(log["greske"][:20], ensure_ascii=False), json.dumps(log["napomena"], ensure_ascii=False)))
        if log["broj_popis"]:      # zapisi koji više nisu na izvoru (ili ih filtar sad odbacuje)
            con.execute("DELETE FROM natjecaji WHERE izvor=? AND zadnji_run_id<?", (log["izvor"], run_id))
    con.commit()


def run_llm(con, run_id: int, limit: int):
    if not llm.available():
        print("LLM: ANTHROPIC_API_KEY nije postavljen -> korak preskočen (radi samo s pravilima).")
        return
    rows = con.execute(
        """SELECT * FROM natjecaji WHERE zadnji_run_id=? AND izvuceno_llm=0 AND length(sirovi_tekst) > 300
           ORDER BY CASE status WHEN 'otvoren' THEN 0 WHEN 'najavljen' THEN 1 WHEN NULL THEN 2 ELSE 3 END,
                    datum_objave DESC""", (run_id,)).fetchall()
    # ravnomjerno po izvorima, da uzorak pokrije sve izvore
    by_src: dict[str, list] = {}
    for r in rows:
        by_src.setdefault(r["izvor"], []).append(r)
    order = []
    while any(by_src.values()) and len(order) < limit:
        for k in list(by_src):
            if by_src[k] and len(order) < limit:
                order.append(by_src[k].pop(0))
    print(f"LLM ({config.LLM_MODEL_BULK}): {len(order)} natječaja, budžet {config.LLM_BUDGET_USD} USD, potrošeno dosad {llm.spent(con):.4f} USD")
    for r in order:
        if llm.spent(con) >= config.LLM_BUDGET_USD:
            print("  budžet potrošen, prekidam")
            break
        rules_rec = json.loads(r["pravila_json"])
        hints = set(rules_rec.pop("_hints", []))
        rules_rec.pop("_kategorije", None)
        tekst = r["sirovi_tekst"] or ""
        page, _, pdf_t = tekst.partition("\n\n=== ")
        out, usage = llm.extract(rules_rec, page, ("=== " + pdf_t) if pdf_t else "")
        c = llm.log_usage(con, run_id, config.LLM_MODEL_BULK, r["id"], usage, "bulk")
        if out is None:
            print(f"  {r['id']}: bez rezultata ({usage.get('error') or usage.get('stop_reason')})")
            con.commit()
            continue
        merged = llm.merge(rules_rec, out, hints)
        db.upsert(con, merged, run_id, {
            "sirovi_tekst": r["sirovi_tekst"], "detalji_dohvaceni": r["detalji_dohvaceni"],
            "pdf_preuzeto": r["pdf_preuzeto"], "pravila_json": r["pravila_json"],
            "llm_json": json.dumps(out, ensure_ascii=False)})
        con.commit()
        print(f"  {r['id']}: ok ({usage['input_tokens']}+{usage['output_tokens']} tok, {c:.5f} USD)")


def main():
    ap = argparse.ArgumentParser()
    ap.add_argument("--izvori", help="popis izvora odvojen zarezom (npr. fzoeu,hzz)")
    ap.add_argument("--refresh", action="store_true")
    ap.add_argument("--llm-limit", type=int, default=5)
    ap.add_argument("--bez-opendata", action="store_true")
    ap.add_argument("--bez-nn", action="store_true", help="preskoči Narodne novine (zakoni i propisi)")
    ap.add_argument("--samo-izvjestaj", action="store_true")
    a = ap.parse_args()
    import report
    con = db.connect()
    if a.samo_izvjestaj:
        report.generate(con)
        return
    set_refresh(a.refresh)
    run_id = db.new_run(con, " ".join(sys.argv[1:]))
    izvori = [(k, m) for k, m in IZVORI if not a.izvori or m.IZVOR in a.izvori.split(",")]
    print(f"Pokretanje {run_id}: {len(izvori)} izvora")
    t0 = time.monotonic()
    # svaki izvor u svojoj dretvi; HttpClient svejedno drži 1 zahtjev/s po domeni
    with ThreadPoolExecutor(max_workers=len(izvori), thread_name_prefix="izvor") as ex:
        futs = [ex.submit(run_source, k, m) for k, m in izvori]
        logs = [f.result() for f in as_completed(futs)]
    save(con, run_id, logs)
    print(f"Izvori gotovi za {time.monotonic() - t0:.0f} s, u bazi {con.execute('SELECT COUNT(*) FROM natjecaji').fetchone()[0]} natječaja")
    if not a.bez_nn and not a.izvori:
        from sources import nn
        t1 = time.monotonic()
        akti = nn.run()
        for akt in akti:
            db.upsert_propis(con, akt, run_id)
        con.commit()
        print(f"Narodne novine: {len(akti)} propisa ({sum(x['vrsta'] == 'zakon' for x in akti)} zakona) za {time.monotonic() - t1:.0f} s")
    run_llm(con, run_id, a.llm_limit)
    if not a.bez_opendata:
        report.collect_opendata()
    report.generate(con)
    from web.build import build
    build(con)
    print(f"Gotovo. Izvještaj: {config.ROOT / 'IZVJESTAJ.md'}, web: {config.ROOT / 'web' / 'index.html'}")


if __name__ == "__main__":
    main()
