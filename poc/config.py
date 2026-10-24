"""Zajedničke postavke POC-a."""
import os
from pathlib import Path

ROOT = Path(__file__).resolve().parent


def _load_dotenv(path: Path):
    """Učita KLJUČ=vrijednost iz poc/.env (ako postoji). Varijable već postavljene u okruženju imaju prednost."""
    if not path.exists():
        return
    for line in path.read_text(encoding="utf-8").splitlines():
        line = line.strip()
        if not line or line.startswith("#") or "=" not in line:
            continue
        key, value = line.split("=", 1)
        os.environ.setdefault(key.strip(), value.strip().strip('"').strip("'"))


_load_dotenv(ROOT / ".env")

DATA = ROOT / "data"
CACHE = DATA / "cache"
DB_PATH = DATA / "natjecaji.sqlite"

USER_AGENT = "DatathonPOC/0.1 (studentski projekt)"
MIN_INTERVAL_S = 1.0          # najviše 1 zahtjev u sekundi po domeni
HTML_CACHE_TTL_H = float(os.environ.get("POC_CACHE_TTL_H", "24"))  # PDF-ovi se nikad ne preuzimaju ponovno

# Ograničenja da POC završi u razumnom vremenu (izvor može imati tisuće stavki)
MAX_LIST_PAGES = int(os.environ.get("POC_MAX_LIST_PAGES", "15"))
MAX_DETAILS_PER_SOURCE = int(os.environ.get("POC_MAX_DETAILS", "40"))
MAX_PDF_PER_ITEM = 3
MAX_PDF_ITEMS_PER_SOURCE = int(os.environ.get("POC_MAX_PDF_ITEMS", "15"))
MAX_PDF_PAGES = 8

# LLM
LLM_MODEL_BULK = "claude-haiku-5-5"
LLM_MODEL_COMPARE = "claude-sonnet-5-5"
LLM_BUDGET_USD = float(os.environ.get("POC_LLM_BUDGET_USD", "1.5"))
# USD po milijun tokena (ulaz, izlaz), Anthropic cjenik listopad 2026.
LLM_PRICES = {
    "claude-haiku-5-5": (0.10, 0.50),
    "claude-sonnet-5-5": (2.00, 10.00),
}
