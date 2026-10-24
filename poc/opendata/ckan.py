"""data.gov.hr (CKAN API): skupovi za povezivanje s natječajima.

Za svaki skup: postoji li, tko ga objavljuje, kad je ažuriran, formati resursa
i (za skupove s rezultatima) koliko redaka/godina ima i postoji li OIB primatelja
kao ključ za spajanje.
"""
import csv
import io
import re
import warnings
from collections import Counter

import openpyxl

from common.http import decode, get_client

API = "https://data.gov.hr/ckan/api/3/action"

SKUPOVI = [
    # (id ili pretraga, uloga)
    ("podaci-o-financiranju-udruga", "rezultati"),
    ("subvencije-poduzetnicima-grada-rijeke-lista-korisnika", "rezultati"),
    ("potpore-poduzetnicima-grad-karlovac", "rezultati"),
    ("potpore-inovatorima-grada-zagreba", "rezultati"),
    ("registar-proracunskih-i-izvanproracunskih-korisnika", "javna_tijela"),
    ("popis-javnopravnih-tijela", "javna_tijela"),
    ("popis-zupanija-gradova-i-opcina", "javna_tijela"),
    ("https-web-dzs-hr-app-klasus-default-aspx-lang-hr", "klasifikacija"),
    ("registar-udruga", "udruge"),
]
PRETRAGE = ["potpore", "subvencije", "stipendije", "natječaj", "dodijeljene potpore", "lista korisnika"]


def package(pid: str) -> dict | None:
    try:
        d = get_client().get_json(f"{API}/package_show?id={pid}")
    except Exception:
        return None
    return d.get("result") if d.get("success") else None


def search_count(q: str) -> tuple[int, list[str]]:
    from urllib.parse import quote
    d = get_client().get_json(f"{API}/package_search?q={quote(q)}&rows=10")["result"]
    return d["count"], [r["name"] for r in d["results"]]


def _table_profile(url: str, fmt: str, max_sheets: int = 30) -> dict:
    """Broj redaka, zaglavlje, godine i ima li OIB stupac (prvi resurs koji se da pročitati)."""
    http = get_client()
    ext = ".xlsx" if fmt in ("XLS", "XLSX") else ".csv"
    data = http.get_bytes(url, ext, 24 * 7)
    if ext == ".csv":
        t = decode(data)
        rows = list(csv.reader(io.StringIO(t), delimiter=";" if t.count(";") > t.count(",") else ","))
        hdr = rows[0] if rows else []
        return {"redaka": max(len(rows) - 1, 0), "stupci": hdr, "listova": 1}
    with warnings.catch_warnings():
        warnings.simplefilter("ignore")
        wb = openpyxl.load_workbook(io.BytesIO(data), read_only=True)
    total, hdr, sheets, oibs, godine = 0, None, [], set(), Counter()
    for ws in wb.worksheets[:max_sheets]:
        sheets.append(ws.title)
        oib_idx = god_idx = None
        for i, r in enumerate(ws.iter_rows(values_only=True)):
            if i == 0:
                hdr = hdr or [str(c) for c in r if c is not None]
                names = [str(c or "").strip().upper() for c in r]
                oib_idx = names.index("OIB") if "OIB" in names else None
                god_idx = names.index("GODINA") if "GODINA" in names else None
                continue
            if not any(r):
                continue
            total += 1
            if oib_idx is not None and r[oib_idx]:
                oibs.add(str(r[oib_idx]).strip())
            if god_idx is not None and r[god_idx]:
                godine[str(r[god_idx])[:4]] += 1
    return {"redaka": total, "stupci": hdr, "listova": len(sheets), "listovi": sheets[:8],
            "razlicitih_oib": len(oibs), "godine": dict(sorted(godine.items()))}


def run() -> dict:
    out = {"skupovi": [], "pretrage": {}}
    for pid, uloga in SKUPOVI:
        pkg = package(pid)
        if pkg is None:
            out["skupovi"].append({"id": pid, "uloga": uloga, "postoji": False})
            continue
        res = pkg.get("resources", [])
        info = {
            "id": pid, "uloga": uloga, "postoji": True, "naslov": pkg["title"],
            "izdavatelj": (pkg.get("organization") or {}).get("title"),
            "azurirano": (pkg.get("metadata_modified") or "")[:10],
            "formati": dict(Counter((r.get("format") or "?").upper() for r in res)),
            "datastore_api": sum(1 for r in res if r.get("datastore_active")),
            "resursa": len(res),
        }
        if uloga == "rezultati":
            tablicni = [r for r in res if (r.get("format") or "").upper() in ("CSV", "XLSX", "XLS")]
            info["citljivo"] = None
            for r in tablicni[:3]:
                try:
                    info["profil"] = _table_profile(r["url"], (r.get("format") or "").upper())
                    info["citljivo"] = True
                    info["resurs"] = r["url"]
                    break
                except Exception as e:
                    info.setdefault("greske", []).append(f"{type(e).__name__}: {r['url'][:90]}")
            if not tablicni:
                info["citljivo"] = False
                info["napomena"] = "nema tabličnog resursa (DOCX/PDF/link na web)"
            elif not info.get("citljivo"):
                info["citljivo"] = False
        out["skupovi"].append(info)
    for q in PRETRAGE:
        n, names = search_count(q)
        out["pretrage"][q] = {"broj": n, "primjeri": names[:5]}
    return out


def udruge_rezultati_po_davatelju(davatelj_regex: str) -> dict:
    """Primjer spoja: koliko je davatelj (npr. 'Rijeka') isplatio udrugama po godinama."""
    pkg = package("podaci-o-financiranju-udruga")
    url = next(r["url"] for r in pkg["resources"] if "gradovi" in r["url"])
    data = get_client().get_bytes(url, ".xlsx", 24 * 7)
    with warnings.catch_warnings():
        warnings.simplefilter("ignore")
        wb = openpyxl.load_workbook(io.BytesIO(data), read_only=True)
    rx = re.compile(davatelj_regex, re.I)
    po_god: Counter = Counter()
    n = 0
    for ws in wb.worksheets:
        for i, r in enumerate(ws.iter_rows(values_only=True)):
            if i == 0 or not r or not r[0] or not rx.search(str(r[0])):
                continue
            n += 1
            try:
                po_god[str(r[6])[:4]] += float(r[7] or 0)
            except (TypeError, ValueError, IndexError):
                pass
    return {"zapisa": n, "iznos_po_godini": {k: round(v) for k, v in sorted(po_god.items())}}
