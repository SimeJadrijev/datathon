"""Izvlačenje polja LLM-om (Claude API, structured outputs).

Ako ANTHROPIC_API_KEY (ili drugi Anthropic credential) nije postavljen, korak se
preskače: `available()` vraća False i ostatak pipelinea radi samo s pravilima.
"""
import json
import os
from datetime import date, datetime

import config
from common.models import CILJNE_SKUPINE, PODRUCJA, VRSTE

MAX_CHARS_PAGE = 15000
MAX_CHARS_PDF = 25000


def _nullable(t: str) -> dict:
    return {"type": [t, "null"]}


SCHEMA = {
    "type": "object",
    "properties": {
        "davatelj": _nullable("string"),
        "vrsta": {"anyOf": [{"type": "string", "enum": VRSTE}, {"type": "null"}]},
        "datum_objave": {**_nullable("string"), "description": "YYYY-MM-DD"},
        "rok_prijave": {**_nullable("string"), "description": "YYYY-MM-DD; null ako je rok 'do utroška sredstava' ili ga nema"},
        "iznos_ukupno_eur": _nullable("number"),
        "iznos_po_korisniku_min_eur": _nullable("number"),
        "iznos_po_korisniku_max_eur": _nullable("number"),
        "ciljne_skupine": {"type": "array", "items": {"type": "string", "enum": CILJNE_SKUPINE}},
        "podrucja": {"type": "array", "items": {"type": "string", "enum": PODRUCJA}},
        "nkd_sifre": {"type": "array", "items": {"type": "string"}},
        "zupanije": {"type": "array", "items": {"type": "string"}},
        "uvjeti_sazetak": _nullable("string"),
        "potrebni_dokumenti": {"type": "array", "items": {"type": "string"}},
    },
    "required": ["davatelj", "vrsta", "datum_objave", "rok_prijave", "iznos_ukupno_eur",
                 "iznos_po_korisniku_min_eur", "iznos_po_korisniku_max_eur", "ciljne_skupine",
                 "podrucja", "nkd_sifre", "zupanije", "uvjeti_sazetak", "potrebni_dokumenti"],
    "additionalProperties": False,
}

SYSTEM = f"""Iz teksta hrvatskog natječaja, javnog poziva, poticaja ili stipendije izvuci strukturirane podatke.
Pravila:
- Koristi samo ono što piše u tekstu. Ako podatka nema, vrati null ili prazan popis. Ne pogađaj.
- Iznosi su u eurima kao broj (20.000.000,00 eura -> 20000000). Iznose u kunama ignoriraj.
- iznos_ukupno_eur je ukupno raspoloživ iznos poziva; min/max su iznosi po jednom korisniku/projektu.
- ciljne_skupine (tko se smije prijaviti): gradjanin = fizičke osobe/kućanstva, obrt, doo = trgovačka društva i poduzetnici,
  udruga = udruge i druge organizacije civilnog društva, jls = jedinice lokalne/regionalne samouprave i javne ustanove,
  student = učenici i studenti, poljoprivrednik = OPG-ovi i poljoprivredna gospodarstva.
- podrucja: najviše 3 najvažnija područja.
- nkd_sifre samo ako su NKD šifre djelatnosti izričito navedene (npr. "62.01").
- zupanije: popis županija na koje se poziv odnosi; ["sve"] ako je nacionalni poziv za cijelu Hrvatsku.
- uvjeti_sazetak: 1-3 rečenice na hrvatskom: tko se može prijaviti i za što.
- potrebni_dokumenti: kratki nazivi dokumenata koje prijavitelj prilaže (najviše 10).
Dopuštene vrste: {", ".join(VRSTE)}."""


def available() -> bool:
    if os.environ.get("POC_NO_LLM"):
        return False
    return bool(os.environ.get("ANTHROPIC_API_KEY") or os.environ.get("ANTHROPIC_AUTH_TOKEN"))


def cost(model: str, inp: int, out: int) -> float:
    pin, pout = config.LLM_PRICES[model]
    return inp / 1e6 * pin + out / 1e6 * pout


_client = None


def _get_client():
    global _client
    if _client is None:
        import anthropic
        _client = anthropic.Anthropic(max_retries=3)
    return _client


def build_user_message(meta: dict, page_text: str, pdf_text: str) -> str:
    return (
        f"Današnji datum: {date.today().isoformat()}\n"
        f"Izvor: {meta['izvor']}\nNaslov: {meta['naziv']}\nURL: {meta['url']}\n"
        f"Davatelj (prema izvoru): {meta.get('davatelj') or 'nepoznat'}\n\n"
        f"--- TEKST STRANICE ---\n{page_text[:MAX_CHARS_PAGE]}\n\n"
        f"--- TEKST PDF PRILOGA ---\n{(pdf_text or '(nema)')[:MAX_CHARS_PDF]}"
    )


def extract(meta: dict, page_text: str, pdf_text: str, model: str = config.LLM_MODEL_BULK) -> tuple[dict | None, dict]:
    """Vraća (polja, usage). polja je None ako model odbije ili odgovor nije valjan JSON."""
    import anthropic
    client = _get_client()
    try:
        resp = client.messages.create(
            model=model,
            max_tokens=4000,
            system=SYSTEM,
            messages=[{"role": "user", "content": build_user_message(meta, page_text, pdf_text)}],
            output_config={"effort": "low", "format": {"type": "json_schema", "schema": SCHEMA}},
        )
    except anthropic.RateLimitError as e:
        return None, {"error": f"rate_limit: {e}", "input_tokens": 0, "output_tokens": 0}
    except anthropic.APIStatusError as e:
        return None, {"error": f"{e.status_code}: {e.message}", "input_tokens": 0, "output_tokens": 0}
    except anthropic.APIConnectionError as e:
        return None, {"error": f"connection: {e}", "input_tokens": 0, "output_tokens": 0}
    usage = {"input_tokens": resp.usage.input_tokens, "output_tokens": resp.usage.output_tokens,
             "stop_reason": resp.stop_reason}
    if resp.stop_reason in ("refusal", "max_tokens"):
        return None, usage
    text = next((b.text for b in resp.content if b.type == "text"), None)
    try:
        return json.loads(text), usage
    except (TypeError, json.JSONDecodeError):
        return None, usage


def log_usage(con, run_id: int, model: str, nid: str, usage: dict, svrha: str) -> float:
    c = cost(model, usage.get("input_tokens", 0), usage.get("output_tokens", 0))
    con.execute("INSERT INTO llm_log VALUES (?,?,?,?,?,?,?,?)",
                (datetime.now().isoformat(timespec="seconds"), run_id, model, nid,
                 usage.get("input_tokens", 0), usage.get("output_tokens", 0), c, svrha))
    return c


def spent(con) -> float:
    return con.execute("SELECT COALESCE(SUM(cijena_usd),0) FROM llm_log").fetchone()[0]


# polja za koja izvor daje strukturirani podatak: tada pravila imaju prednost pred LLM-om
def merge(rules: dict, llm: dict, hints: set[str]) -> dict:
    """Strukturirani podatak izvora (hint) > LLM > pravila.

    LLM je pročitao cijeli tekst, pa i njegov null vrijedi: npr. pravila uhvate "31. 3. 2028." kao rok,
    a to je kraj razdoblja nabave, ne rok prijave. Zato LLM null briše vrijednost iz pravila.
    """
    out = dict(rules)
    for k, v in llm.items():
        if k in hints and rules.get(k) is not None:
            continue
        out[k] = None if v in ([], "") else v
    out["izvuceno_llm"] = True
    if "status" not in hints and out.get("status") != "najavljen":
        if out.get("rok_prijave"):
            out["status"] = "otvoren" if out["rok_prijave"] >= date.today().isoformat() else "zatvoren"
        elif rules.get("rok_prijave"):
            out["status"] = None        # status pravila je bio izveden iz roka koji LLM odbacuje
    return out
