"""SQLite pohrana: natjecaji, izvori_log, llm_log, pokretanja."""
import json
import sqlite3
from datetime import datetime

import config
from common.models import LIST_POLJA, POLJA

SCHEMA = """
CREATE TABLE IF NOT EXISTS natjecaji (
    id TEXT PRIMARY KEY,
    izvor TEXT NOT NULL,
    url TEXT,
    naziv TEXT,
    davatelj TEXT,
    vrsta TEXT,
    status TEXT,
    datum_objave TEXT,
    rok_prijave TEXT,
    iznos_ukupno_eur REAL,
    iznos_po_korisniku_min_eur REAL,
    iznos_po_korisniku_max_eur REAL,
    ciljne_skupine TEXT,
    podrucja TEXT,
    nkd_sifre TEXT,
    zupanije TEXT,
    uvjeti_sazetak TEXT,
    potrebni_dokumenti TEXT,
    prilozi TEXT,
    sirovi_tekst_hash TEXT,
    izvuceno_llm INTEGER DEFAULT 0,
    dohvaceno TEXT,
    -- pomoćni stupci (nisu dio javne sheme)
    sirovi_tekst TEXT,
    detalji_dohvaceni INTEGER DEFAULT 0,
    pdf_preuzeto INTEGER DEFAULT 0,
    pravila_json TEXT,
    llm_json TEXT,
    prvi_run_id INTEGER,
    zadnji_run_id INTEGER
);
CREATE TABLE IF NOT EXISTS izvori_log (
    run_id INTEGER, izvor TEXT, kategorija TEXT, metoda TEXT, pocetak TEXT,
    trajanje_s REAL, broj_popis INTEGER, broj_detalji INTEGER, broj_pdf INTEGER,
    http_zahtjeva INTEGER, cache_pogodaka INTEGER, greske TEXT, napomena TEXT
);
CREATE TABLE IF NOT EXISTS llm_log (
    ts TEXT, run_id INTEGER, model TEXT, natjecaj_id TEXT, input_tokens INTEGER,
    output_tokens INTEGER, cijena_usd REAL, svrha TEXT
);
CREATE TABLE IF NOT EXISTS pokretanja (run_id INTEGER PRIMARY KEY, ts TEXT, napomena TEXT);
"""


def connect() -> sqlite3.Connection:
    config.DATA.mkdir(parents=True, exist_ok=True)
    con = sqlite3.connect(config.DB_PATH, check_same_thread=False)
    con.row_factory = sqlite3.Row
    con.executescript(SCHEMA)
    return con


def new_run(con, napomena="") -> int:
    cur = con.execute("INSERT INTO pokretanja (ts, napomena) VALUES (?, ?)",
                      (datetime.now().isoformat(timespec="seconds"), napomena))
    con.commit()
    return cur.lastrowid


def upsert(con, rec: dict, run_id: int, extra: dict):
    """rec su polja javne sheme, extra pomoćni stupci."""
    row = {k: rec.get(k) for k in POLJA}
    for k in LIST_POLJA:
        row[k] = json.dumps(row[k], ensure_ascii=False) if row[k] is not None else None
    row["izvuceno_llm"] = int(bool(row["izvuceno_llm"]))
    row.update(extra)
    row["zadnji_run_id"] = run_id
    prev = con.execute("SELECT prvi_run_id FROM natjecaji WHERE id=?", (row["id"],)).fetchone()
    row["prvi_run_id"] = prev["prvi_run_id"] if prev else run_id
    cols = list(row)
    con.execute(
        f"INSERT OR REPLACE INTO natjecaji ({','.join(cols)}) VALUES ({','.join('?' * len(cols))})",
        [row[c] for c in cols],
    )


def row_to_rec(row) -> dict:
    rec = {k: row[k] for k in POLJA}
    for k in LIST_POLJA:
        rec[k] = json.loads(rec[k]) if rec[k] else None
    rec["izvuceno_llm"] = bool(rec["izvuceno_llm"])
    return rec


def all_records(con) -> list[dict]:
    return [row_to_rec(r) for r in con.execute("SELECT * FROM natjecaji ORDER BY izvor, id")]
