"""Izvozi podatke iz SQLite baze za mini web (web/index.html, web/zakoni.html).

    python web/build.py      # poziva ga i run_all.py na kraju

Piše web/data/natjecaji.js i web/data/propisi.js (window.POC_* varijable), pa se
stranice otvaraju izravno iz datoteke, bez servera.
"""
import json
import sys
from datetime import datetime
from pathlib import Path

sys.path.insert(0, str(Path(__file__).resolve().parent.parent))

from common import db  # noqa: E402

OUT = Path(__file__).with_name("data")

IZVORI = {
    "apprrr": "APPRRR", "fzoeu": "FZOEU", "fondovieu": "EU fondovi (NPOO)", "hamag": "HAMAG-BICRO",
    "hzz": "HZZ mjere", "udruge": "Ured za udruge", "mzom": "MZOM", "ampeu": "AMPEU",
    "rijeka": "Grad Rijeka", "osijek": "Grad Osijek", "zagreb": "Grad Zagreb",
}


def _natjecaji(con) -> list[dict]:
    out = []
    for r in con.execute("SELECT * FROM natjecaji"):
        rec = db.row_to_rec(r)
        out.append({
            "id": rec["id"], "izvor": rec["izvor"], "url": rec["url"], "naziv": rec["naziv"],
            "davatelj": rec["davatelj"], "vrsta": rec["vrsta"], "status": rec["status"],
            "objava": rec["datum_objave"], "rok": rec["rok_prijave"],
            "ukupno": rec["iznos_ukupno_eur"], "min": rec["iznos_po_korisniku_min_eur"],
            "max": rec["iznos_po_korisniku_max_eur"],
            "skupine": rec["ciljne_skupine"] or [], "podrucja": rec["podrucja"] or [],
            "zupanije": rec["zupanije"] or [], "uvjeti": rec["uvjeti_sazetak"],
            "dokumenti": rec["potrebni_dokumenti"] or [], "prilozi": (rec["prilozi"] or [])[:6],
            "llm": rec["izvuceno_llm"], "detalji": bool(r["detalji_dohvaceni"]),
        })
    return out


def _izvori(con) -> list[dict]:
    from report import TEZINA
    logs = {}
    for l in con.execute("SELECT * FROM izvori_log ORDER BY run_id"):
        logs[l["izvor"]] = l
    out = []
    for izvor, naziv in IZVORI.items():
        l = logs.get(izvor)
        nap = json.loads(l["napomena"] or "{}") if l else {}
        uk = nap.get("ukupno_na_izvoru")
        if isinstance(uk, dict):
            uk = sum(v or 0 for v in uk.values())
        n = con.execute("SELECT COUNT(*), SUM(detalji_dohvaceni), SUM(izvuceno_llm) FROM natjecaji WHERE izvor=?",
                        (izvor,)).fetchone()
        out.append({"izvor": izvor, "naziv": naziv, "metoda": l["metoda"] if l else None,
                    "u_bazi": n[0], "s_detaljima": n[1] or 0, "llm": n[2] or 0, "na_izvoru": uk,
                    "tezina": TEZINA.get(izvor, (None, ""))[0], "problemi": TEZINA.get(izvor, (None, ""))[1]})
    return out


def _propisi(con) -> list[dict]:
    rows = con.execute("SELECT * FROM propisi ORDER BY datum_objave DESC, dokument DESC").fetchall()
    return [{"id": r["id"], "naziv": r["naziv"], "vrsta": r["vrsta"], "nn": r["nn_broj"],
             "objava": r["datum_objave"], "doneseno": r["datum_dokumenta"], "url": r["url"],
             "eli": r["eli"], "pdf": r["pdf"], "podrucja": json.loads(r["podrucja"]) if r["podrucja"] else []}
            for r in rows]


def _write(name: str, var: str, payload: dict):
    OUT.mkdir(exist_ok=True)
    (OUT / name).write_text(f"window.{var} = " + json.dumps(payload, ensure_ascii=False) + ";\n", encoding="utf-8")


def build(con=None):
    con = con or db.connect()
    gen = datetime.now().isoformat(timespec="minutes")
    nat = _natjecaji(con)
    _write("natjecaji.js", "POC_NATJECAJI", {"generirano": gen, "natjecaji": nat, "izvori": _izvori(con)})
    pro = _propisi(con)
    _write("propisi.js", "POC_PROPISI", {"generirano": gen, "propisi": pro})
    print(f"Web: {len(nat)} natječaja, {len(pro)} propisa -> {OUT}")


if __name__ == "__main__":
    build()
