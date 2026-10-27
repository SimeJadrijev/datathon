import argparse
import json
import sqlite3
import sys
from datetime import date, datetime, timedelta
from pathlib import Path
from zoneinfo import ZoneInfo

ROOT = Path(__file__).resolve().parent.parent
SEED_DIR = ROOT / "apps/api/src/main/resources/seed"
ZAGREB = ZoneInfo("Europe/Zagreb")

SOURCES = {
    "udruge": "ASSOCIATIONS_OFFICE",
    "fzoeu": "FZOEU",
    "fondovieu": "EU_FUNDS_NPOO",
    "hzz": "HZZ",
    "apprrr": "APPRRR",
    "mzom": "MZOM",
}
SKIPPED_SOURCES = {"rijeka", "osijek", "zagreb", "ampeu", "hamag"}

TYPES = {
    "natječaj": "GRANT",
    "poticaj": "SUBSIDY",
    "javni poziv": "PUBLIC_CALL",
    "stipendija": "SCHOLARSHIP",
    "zajam": "LOAN",
    "nagrada": "AWARD",
}
STATUSES = {"najavljen": "ANNOUNCED", "otvoren": "OPEN", "zatvoren": "CLOSED"}
TARGET_GROUPS = {
    "gradjanin": "CITIZEN",
    "obrt": "CRAFT_BUSINESS",
    "doo": "COMPANY",
    "udruga": "ASSOCIATION",
    "jls": "LOCAL_GOVERNMENT",
    "student": "STUDENT",
    "poljoprivrednik": "FARMER",
}
AREAS = {
    "okolis": "ENVIRONMENT",
    "energetika": "ENERGY",
    "promet": "TRANSPORT",
    "poljoprivreda": "AGRICULTURE",
    "obrazovanje": "EDUCATION",
    "znanost": "SCIENCE",
    "kultura": "CULTURE",
    "sport": "SPORT",
    "socijala": "SOCIAL",
    "zdravstvo": "HEALTH",
    "zaposljavanje": "EMPLOYMENT",
    "poduzetnistvo": "ENTREPRENEURSHIP",
    "inovacije": "INNOVATION",
    "digitalizacija": "DIGITALIZATION",
    "turizam": "TOURISM",
    "mladi": "YOUTH",
    "civilno_drustvo": "CIVIL_SOCIETY",
}
FIELD_NAMES = {
    "vrsta": "type",
    "status": "status",
    "rok_prijave": "deadline",
    "iznos_ukupno_eur": "totalAmount",
    "iznos_po_korisniku_min_eur": "minAmount",
    "iznos_po_korisniku_max_eur": "maxAmount",
    "ciljne_skupine": "targetGroups",
    "podrucja": "areas",
    "nkd_sifre": "activityCodes",
    "zupanije": "counties",
    "uvjeti_sazetak": "conditionsSummary",
    "potrebni_dokumenti": "requiredDocuments",
}

warnings: list[str] = []


def main():
    parser = argparse.ArgumentParser(description="Export the POC SQLite database into the API seed files")
    parser.add_argument("database", type=Path, help="path to the POC natjecaji.sqlite")
    args = parser.parse_args()
    if not args.database.exists():
        sys.exit(f"Database not found: {args.database}")

    con = sqlite3.connect(args.database)
    con.row_factory = sqlite3.Row
    snapshot_date = date.today()

    funding_calls = [fc for row in con.execute("SELECT * FROM natjecaji ORDER BY izvor, id")
                     if (fc := funding_call(row, snapshot_date)) is not None]
    regulations = [regulation(row) for row in con.execute("SELECT * FROM propisi ORDER BY id")]

    SEED_DIR.mkdir(parents=True, exist_ok=True)
    write(SEED_DIR / "funding-calls.json", {"snapshotDate": snapshot_date.isoformat(), "fundingCalls": funding_calls})
    write(SEED_DIR / "regulations.json", {"snapshotDate": snapshot_date.isoformat(), "regulations": regulations})
    print_summary(funding_calls, regulations)


def funding_call(row: sqlite3.Row, snapshot_date: date) -> dict | None:
    source = row["izvor"]
    if source in SKIPPED_SOURCES:
        return None
    if source not in SOURCES:
        warnings.append(f"unknown source '{source}', record {row['id']} skipped")
        return None

    status = enum_value(STATUSES, row["status"], "status")
    deadline = row["rok_prijave"]
    published_at = row["datum_objave"]
    return {
        "source": SOURCES[source],
        "externalId": external_id(row["id"], source),
        "url": row["url"],
        "title": row["naziv"],
        "provider": row["davatelj"],
        "type": enum_value(TYPES, row["vrsta"], "type"),
        "status": status,
        "publishedAt": published_at,
        "deadline": deadline,
        "totalAmount": row["iznos_ukupno_eur"],
        "minAmount": row["iznos_po_korisniku_min_eur"],
        "maxAmount": row["iznos_po_korisniku_max_eur"],
        "targetGroups": enum_list(TARGET_GROUPS, row["ciljne_skupine"], "target group"),
        "areas": enum_list(AREAS, row["podrucja"], "area"),
        "counties": counties(row["zupanije"]),
        "activityCodes": json_list(row["nkd_sifre"]),
        "conditionsSummary": row["uvjeti_sazetak"],
        "requiredDocuments": json_list(row["potrebni_dokumenti"]),
        "attachments": [{"name": a.get("naziv"), "url": a.get("url")} for a in json_list(row["prilozi"])],
        "rawTextHash": row["sirovi_tekst_hash"],
        "extractedByLlm": bool(row["izvuceno_llm"]),
        "aiFields": ai_fields(row),
        "fetchedAt": to_instant(row["dohvaceno"]),
        "sourceText": None if is_old_closed(status, deadline or published_at, snapshot_date) else row["sirovi_tekst"],
    }


def regulation(row: sqlite3.Row) -> dict:
    return {
        "externalId": row["id"].removeprefix("nn-"),
        "title": row["naziv"],
        "type": regulation_type(row["vrsta"]),
        "gazetteNumber": row["nn_broj"],
        "documentNumber": row["dokument"],
        "publishedAt": row["datum_objave"],
        "enactedAt": row["datum_dokumenta"],
        "url": row["url"],
        "eliUrl": row["eli"],
        "pdfUrl": row["pdf"],
        "areas": enum_list(AREAS, row["podrucja"], "area"),
        "eliMetadata": bool(row["eli_meta"]),
        "fetchedAt": to_instant(row["dohvaceno"]),
    }


def external_id(poc_id: str, source: str) -> str:
    return poc_id.removeprefix(source + "-")


def regulation_type(value: str | None) -> str:
    kind = (value or "").lower()
    for prefix, code in (("zakon", "LAW"), ("uredb", "DECREE"), ("pravilnik", "ORDINANCE"), ("odluk", "DECISION")):
        if kind.startswith(prefix):
            return code
    return "OTHER"


def counties(value: str | None) -> list[str]:
    return ["ALL" if county == "sve" else county for county in json_list(value)]


def ai_fields(row: sqlite3.Row) -> list[str]:
    if not row["izvuceno_llm"] or not row["llm_json"]:
        return []
    llm = json.loads(row["llm_json"])
    rules = json.loads(row["pravila_json"] or "{}")
    return sorted(java_name for poc_name, java_name in FIELD_NAMES.items()
                  if llm.get(poc_name) not in (None, [], "") and rules.get(poc_name) in (None, [], ""))


def is_old_closed(status: str | None, reference_date: str | None, snapshot_date: date) -> bool:
    if status != "CLOSED" or not reference_date:
        return False
    return date.fromisoformat(reference_date[:10]) < snapshot_date - timedelta(days=365)


def enum_value(mapping: dict, value: str | None, label: str) -> str | None:
    if value is None:
        return None
    if value not in mapping:
        warnings.append(f"unknown {label} '{value}' dropped")
        return None
    return mapping[value]


def enum_list(mapping: dict, value: str | None, label: str) -> list[str]:
    return [code for item in json_list(value) if (code := enum_value(mapping, item, label)) is not None]


def json_list(value: str | None) -> list:
    return json.loads(value) if value else []


def to_instant(value: str | None) -> str | None:
    if not value:
        return None
    local = datetime.fromisoformat(value)
    if local.tzinfo is None:
        local = local.replace(tzinfo=ZAGREB)
    return local.astimezone(ZoneInfo("UTC")).strftime("%Y-%m-%dT%H:%M:%SZ")


def write(path: Path, payload: dict):
    path.write_text(json.dumps(payload, ensure_ascii=False, indent=1) + "\n", encoding="utf-8")
    print(f"{path.relative_to(ROOT)}: {path.stat().st_size / 1_000_000:.1f} MB")


def print_summary(funding_calls: list[dict], regulations: list[dict]):
    by_source: dict[str, list[int]] = {}
    for fc in funding_calls:
        counts = by_source.setdefault(fc["source"], [0, 0])
        counts[0] += 1
        counts[1] += fc["status"] == "OPEN"
    for source, (total, open_count) in sorted(by_source.items()):
        print(f"  {source}: {total} funding calls, {open_count} open")
    print(f"  regulations: {len(regulations)}")
    for warning in sorted(set(warnings)):
        print(f"  warning: {warning} (x{warnings.count(warning)})")


if __name__ == "__main__":
    main()
