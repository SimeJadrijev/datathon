"""Podudaranje profila i natječaja.

    python match.py profiles/student_rijeka.json
    python match.py --oib 00000000001          # Sudski registar -> NKD + sjedište -> profil
    python match.py --svi-profili              # tri testna profila iz profiles/

Profil: {"naziv", "tip": "student|gradjanin|obrt|doo|udruga|poljoprivrednik|jls",
         "dodatni_tipovi": [...], "interesi": [područja], "kljucne_rijeci": [...],
         "nkd": ["62.01"], "lokacija": {"grad", "zupanija"}}
"""
import argparse
import json
import re
import sys
from datetime import date, timedelta
from pathlib import Path

import config
from common import db

PROFILI = sorted((config.ROOT / "profiles").glob("*.json"))


def _norm(s: str) -> str:
    s = s.lower().replace("županija", "").replace("grad ", "").strip(" -")
    return re.sub(r"\s+", " ", s)


def score(profile: dict, rec: dict, ukljuci_zatvorene: bool = False) -> tuple[float, list[str]] | None:
    today = date.today().isoformat()
    razlozi, s = [], 0.0
    status = rec.get("status")
    if status == "zatvoren" and not ukljuci_zatvorene:
        return None
    if rec.get("rok_prijave") and rec["rok_prijave"] < today and not ukljuci_zatvorene:
        return None
    if status is None and not rec.get("rok_prijave"):
        objava = rec.get("datum_objave")
        if not objava or objava < (date.today() - timedelta(days=365)).isoformat():
            return None          # nepoznat status i star (ili bez datuma): vjerojatno zatvoren
        s -= 1
        razlozi.append("status nepoznat")

    # tko se smije prijaviti
    tipovi = [profile["tip"]] + profile.get("dodatni_tipovi", [])
    cs = rec.get("ciljne_skupine")
    if cs:
        pogodak = [t for t in tipovi if t in cs]
        if not pogodak:
            return None
        s += 4 if pogodak[0] == profile["tip"] else 2
        razlozi.append(f"ciljna skupina: {pogodak[0]}")
    else:
        razlozi.append("ciljna skupina nije navedena")

    # lokacija
    zup = [z for z in (rec.get("zupanije") or []) if z]
    lok = profile.get("lokacija") or {}
    mjesta = {_norm(x) for x in (lok.get("zupanija"), lok.get("grad")) if x}
    if zup and "sve" not in [z.lower() for z in zup]:
        if any(_norm(z) in mjesta or any(m in _norm(z) for m in mjesta) for z in zup):
            s += 2
            razlozi.append(f"lokalno ({', '.join(zup)})")
        else:
            return None
    # područja i ključne riječi
    preklop = [p for p in profile.get("interesi", []) if p in (rec.get("podrucja") or [])]
    if preklop:
        s += 3 * min(len(preklop), 2)
        razlozi.append("područje: " + ", ".join(preklop))
    naziv = (rec.get("naziv") or "").lower()
    kw = [k for k in profile.get("kljucne_rijeci", []) if k.lower() in naziv]
    if kw:
        s += 3 * len(kw)
        razlozi.append("ključne riječi u nazivu: " + ", ".join(kw))
    nkd = [n for n in rec.get("nkd_sifre") or [] if any(n.startswith(p[:2]) for p in profile.get("nkd", []))]
    if nkd:
        s += 4
        razlozi.append("NKD: " + ", ".join(nkd))
    if not preklop and not kw and not nkd:
        return None            # samo formalno prihvatljiv, ali tematski nevezan
    if status == "otvoren":
        s += 2
        razlozi.append("otvoren")
    elif status == "najavljen":
        s += 1
        razlozi.append("najavljen")
    if rec.get("rok_prijave") and rec["rok_prijave"] <= (date.today() + timedelta(days=30)).isoformat():
        s += 0.5
        razlozi.append(f"rok uskoro ({rec['rok_prijave']})")
    return s, razlozi


def _kljuc(naziv: str) -> str:
    """Normalizirani naziv: isti natječaj objavljuju različiti izvori (npr. MZOM i AMPEU iste stipendije)."""
    return re.sub(r"[^a-zčćšđž0-9]+", " ", (naziv or "").lower()).strip()[:80]


def match(profile: dict, records: list[dict], top: int = 10, ukljuci_zatvorene: bool = False) -> list[dict]:
    out = []
    for r in records:
        res = score(profile, r, ukljuci_zatvorene)
        if res:
            out.append({"score": res[0], "razlozi": res[1], **{k: r.get(k) for k in
                        ("id", "izvor", "naziv", "status", "rok_prijave", "iznos_po_korisniku_max_eur", "url")}})
    out.sort(key=lambda x: (-x["score"], x.get("rok_prijave") or "9999"))
    seen, unique = set(), []
    for x in out:
        k = _kljuc(x["naziv"])
        if k not in seen:
            seen.add(k)
            unique.append(x)
    return unique[:top]


def load_profile(path: str | Path) -> dict:
    p = json.loads(Path(path).read_text(encoding="utf-8"))
    if p.get("oib"):
        from opendata.sudreg import profil_iz_oib
        p = {**profil_iz_oib(p["oib"]), **{k: v for k, v in p.items() if k not in ("oib",)}, "oib": p["oib"]}
    return p


def print_results(profile: dict, res: list[dict]):
    print(f"\n### {profile.get('naziv') or profile['tip']}  (tip={profile['tip']}, interesi={profile.get('interesi')}, "
          f"lokacija={profile.get('lokacija')})")
    if not res:
        print("  (nema podudaranja)")
    for i, r in enumerate(res, 1):
        print(f"  {i:2}. [{r['score']:.1f}] {r['naziv'][:90]}  ({r['izvor']}, {r['status'] or '?'}, rok {r['rok_prijave'] or '?'})")
        print(f"      zašto: {'; '.join(r['razlozi'])}")


def main():
    ap = argparse.ArgumentParser()
    ap.add_argument("profil", nargs="?")
    ap.add_argument("--oib")
    ap.add_argument("--svi-profili", action="store_true")
    ap.add_argument("--zatvoreni", action="store_true", help="uključi i zatvorene natječaje")
    ap.add_argument("--top", type=int, default=10)
    a = ap.parse_args()
    records = db.all_records(db.connect())
    if not records:
        sys.exit("Baza je prazna: najprije pokreni python run_all.py")
    if a.oib:
        try:
            profiles = [load_profile_from_oib(a.oib)]
        except KeyError as e:
            sys.exit(str(e.args[0]))
    elif a.svi_profili or not a.profil:
        profiles = [load_profile(p) for p in PROFILI]
    else:
        profiles = [load_profile(a.profil)]
    for p in profiles:
        print_results(p, match(p, records, a.top, a.zatvoreni))


def load_profile_from_oib(oib: str) -> dict:
    from opendata.sudreg import profil_iz_oib
    return {**profil_iz_oib(oib), "oib": oib}


if __name__ == "__main__":
    main()
