"""Sudski registar (sudreg-data.gov.hr): tvrtka po OIB-u -> NKD + sjedište -> profil.

API traži OAuth2 client credentials (besplatna registracija na portalu, ključ
odmah nakon potvrde e-maila). Ključ se čita iz SUDREG_CLIENT_ID i SUDREG_CLIENT_SECRET (poc/.env). Bez ključa koristi se mock iz
`sudreg_mock.json` (izmišljene tvrtke, jasno označene, ista struktura kao API).
"""
import json
import os
from pathlib import Path

import httpx

import config

BASE = "https://sudreg-data.gov.hr/api/javni"
TOKEN_URL = "https://sudreg-data.gov.hr/api/oauth/token"
MOCK = Path(__file__).with_name("sudreg_mock.json")

# kratica pravnog oblika (pravni_oblik.vrsta_pravnog_oblika.kratica) -> ciljna skupina
PRAVNI_OBLIK_SKUPINA = {"d.o.o.": "doo", "j.d.o.o.": "doo", "d.d.": "doo", "k.d.": "doo", "j.t.d.": "doo",
                        "zadruga": "doo", "ustanova": "jls"}


def has_credentials() -> bool:
    return bool(os.environ.get("SUDREG_CLIENT_ID") and os.environ.get("SUDREG_CLIENT_SECRET"))


def _token() -> str:
    r = httpx.post(TOKEN_URL, data={"grant_type": "client_credentials"},
                   auth=(os.environ["SUDREG_CLIENT_ID"], os.environ["SUDREG_CLIENT_SECRET"]),
                   headers={"User-Agent": config.USER_AGENT}, timeout=30)
    r.raise_for_status()
    return r.json()["access_token"]


def probe_without_key(oib: str = "02535697732") -> dict:
    """Bilježi što API vrati bez ključa (za izvještaj)."""
    r = httpx.get(f"{BASE}/detalji_subjekta", params={"tip_identifikatora": "oib", "identifikator": oib},
                  headers={"User-Agent": config.USER_AGENT}, timeout=30)
    return {"status": r.status_code, "content_type": r.headers.get("content-type", "")[:40]}


def openapi_info() -> dict:
    r = httpx.get(f"{BASE}/dokumentacija/open_api", headers={"User-Agent": config.USER_AGENT}, timeout=30)
    d = r.json()
    return {"status": r.status_code, "endpointa": len(d.get("paths", {})),
            "security": list((d.get("components") or {}).get("securitySchemes", {}).keys()),
            "token_url": d["components"]["securitySchemes"]["Oauth2"]["flows"]["clientCredentials"]["tokenUrl"]}


def nkd_iz_glavne(glavna: int | str | None) -> str | None:
    """`glavna_djelatnost` je NKD 2007 šifra bez točke (npr. 1072 -> "10.72", 150 -> "01.50").

    Provjereno na stvarnim tvrtkama (24. 10. 2026.). Polje `pretezita_djelatnost` je za
    javne korisnike prazno, a šifrarnik /nacionalna_klasifikacija_djelatnosti koristi druge ID-eve.
    """
    if glavna in (None, "", 0):
        return None
    s = f"{int(glavna):04d}"
    return f"{s[:2]}.{s[2:]}"


def detalji(oib: str) -> dict:
    """Detalji subjekta. Pravi API ako postoji ključ, inače mock (ključ '_mock': True).

    Izmišljeni OIB-ovi iz mocka uvijek idu na mock, da testni profili rade i s ključem.
    """
    mock = json.loads(MOCK.read_text(encoding="utf-8"))
    if has_credentials() and oib not in mock:
        h = {"Authorization": f"Bearer {_token()}", "User-Agent": config.USER_AGENT}
        r = httpx.get(f"{BASE}/detalji_subjekta", headers=h, timeout=30,
                      params={"tip_identifikatora": "oib", "identifikator": oib, "expand_relations": "true"})
        r.raise_for_status()
        return r.json()
    if oib not in mock:
        raise KeyError(f"OIB {oib} nije u mocku (bez SUDREG ključa rade samo: {', '.join(mock)})")
    return {**mock[oib], "_mock": True}


def profil_iz_oib(oib: str) -> dict:
    from opendata.nkd import podrucja_za_nkd
    d = detalji(oib)
    nkd = nkd_iz_glavne(d.get("glavna_djelatnost"))
    sj = d.get("sjediste") or {}
    kratica = ((d.get("pravni_oblik") or {}).get("vrsta_pravnog_oblika") or {}).get("kratica") or ""
    return {
        "naziv": (d.get("tvrtka") or {}).get("ime"),
        "tip": PRAVNI_OBLIK_SKUPINA.get(kratica.lower(), "doo"),
        "nkd": [nkd] if nkd else [],
        "interesi": podrucja_za_nkd(nkd) if nkd else ["poduzetnistvo"],
        "lokacija": {"grad": sj.get("naziv_opcine"), "zupanija": sj.get("naziv_zupanije")},
        "izvor_profila": "sudreg_mock" if d.get("_mock") else "sudreg_api",
    }
