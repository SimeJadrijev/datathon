"""Tests for export_poc_snapshot.py. Run: python -m unittest tools/test_export_poc_snapshot.py"""
import json
import sqlite3
import sys
import unittest
from datetime import date
from pathlib import Path

sys.path.insert(0, str(Path(__file__).resolve().parent))
import export_poc_snapshot as export  # noqa: E402

POC_ROW = {
    "id": "fzoeu-275", "izvor": "fzoeu", "url": "https://www.fzoeu.hr/hr/nacionalni-javni-pozivi-i-natjecaji/1367#275",
    "naziv": "Javni poziv EnU-2/26", "davatelj": "FZOEU", "vrsta": "javni poziv", "status": "otvoren",
    "datum_objave": "2026-03-02", "rok_prijave": None, "iznos_ukupno_eur": None,
    "iznos_po_korisniku_min_eur": None, "iznos_po_korisniku_max_eur": 9000.0,
    "ciljne_skupine": json.dumps(["gradjanin"]), "podrucja": json.dumps(["promet", "nepostojece"]),
    "nkd_sifre": None, "zupanije": json.dumps(["sve"]), "uvjeti_sazetak": "Sažetak",
    "potrebni_dokumenti": None, "prilozi": json.dumps([{"naziv": "Poziv", "url": "https://www.fzoeu.hr/docs/275/a.pdf"}]),
    "sirovi_tekst_hash": "abc", "izvuceno_llm": 1, "dohvaceno": "2026-10-24T10:15:00",
    "sirovi_tekst": "tekst", "llm_json": json.dumps({"uvjeti_sazetak": "Sažetak", "rok_prijave": None}),
    "pravila_json": json.dumps({"uvjeti_sazetak": None}),
}


def row(**overrides) -> sqlite3.Row:
    values = {**POC_ROW, **overrides}
    con = sqlite3.connect(":memory:")
    con.row_factory = sqlite3.Row
    columns = ", ".join(values)
    con.execute(f"CREATE TABLE natjecaji ({columns})")
    con.execute(f"INSERT INTO natjecaji VALUES ({', '.join('?' * len(values))})", list(values.values()))
    return con.execute("SELECT * FROM natjecaji").fetchone()


class ExportFundingCallTest(unittest.TestCase):

    def test_maps_poc_record_to_seed_record(self):
        record = export.funding_call(row(), date(2026, 10, 26))

        self.assertEqual(record["source"], "FZOEU")
        self.assertEqual(record["externalId"], "275")
        self.assertEqual(record["type"], "PUBLIC_CALL")
        self.assertEqual(record["status"], "OPEN")
        self.assertIsNone(record["deadline"])
        self.assertEqual(record["counties"], ["ALL"])
        self.assertEqual(record["targetGroups"], ["CITIZEN"])
        self.assertEqual(record["areas"], ["TRANSPORT"])  # unknown area dropped with a warning
        self.assertEqual(record["attachments"], [{"name": "Poziv", "url": "https://www.fzoeu.hr/docs/275/a.pdf"}])
        self.assertEqual(record["aiFields"], ["conditionsSummary"])
        self.assertEqual(record["fetchedAt"], "2026-10-24T08:15:00Z")
        self.assertEqual(record["sourceText"], "tekst")

    def test_skips_sources_outside_the_mvp(self):
        self.assertIsNone(export.funding_call(row(izvor="rijeka", id="rijeka-1"), date(2026, 10, 26)))

    def test_drops_text_of_closed_calls_older_than_a_year(self):
        record = export.funding_call(row(status="zatvoren", rok_prijave="2024-05-01"), date(2026, 10, 26))

        self.assertIsNone(record["sourceText"])


class RegulationTypeTest(unittest.TestCase):

    def test_maps_free_text_type(self):
        self.assertEqual(export.regulation_type("zakon"), "LAW")
        self.assertEqual(export.regulation_type("uredba"), "DECREE")
        self.assertEqual(export.regulation_type("pravilnik"), "ORDINANCE")
        self.assertEqual(export.regulation_type("odluka"), "DECISION")
        self.assertEqual(export.regulation_type("naredba"), "OTHER")


if __name__ == "__main__":
    unittest.main()
