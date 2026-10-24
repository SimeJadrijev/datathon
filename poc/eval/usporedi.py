"""Točnost po polju na ručno ispravljenom uzorku (eval/uzorak.csv).

Pravila usporedbe:
  - rok: točan datum;  iznosi: unutar 1 %;  prazno/'-' u tocno_* = polje ne postoji
    (točno je samo ako je i izvučena vrijednost prazna);
  - ciljne_skupine, podrucja: Jaccard sličnost skupova (1.0 = isto), prikazan prosjek.

    python eval/usporedi.py
"""
import csv
import sys
from pathlib import Path

sys.path.insert(0, str(Path(__file__).resolve().parent.parent))

UZORAK = Path(__file__).with_name("uzorak.csv")
POLJA = ["rok_prijave", "iznos_po_korisniku_max_eur", "iznos_ukupno_eur", "ciljne_skupine", "podrucja"]
METODE = ["pravila", "haiku", "sonnet"]


def _empty(v: str) -> bool:
    return (v or "").strip() in ("", "-")


def score_field(field: str, got: str, truth: str) -> float:
    if _empty(truth):
        return 1.0 if _empty(got) else 0.0
    if _empty(got):
        return 0.0
    if field in ("ciljne_skupine", "podrucja"):
        a = {x.strip() for x in got.split(";") if x.strip()}
        b = {x.strip() for x in truth.split(";") if x.strip()}
        return len(a & b) / len(a | b) if a | b else 1.0
    if field.startswith("iznos"):
        try:
            g, t = float(got.replace(",", ".")), float(truth.replace(",", "."))
        except ValueError:
            return 0.0
        return 1.0 if abs(g - t) <= 0.01 * max(abs(t), 1) else 0.0
    return 1.0 if got.strip() == truth.strip() else 0.0


def evaluate(rows: list[dict]) -> dict:
    """rows = ocijenjeni retci (barem jedan tocno_* ispunjen; '-' znači da polje ne postoji u izvoru)."""
    res = {}
    for m in METODE:
        # metoda koja nije pokrenuta (npr. nema LLM ključa) nema nijednu vrijednost u uzorku
        pokrenuta = m == "pravila" or any(r.get(f"{m}_{x}") for r in rows for x in POLJA)
        for f in POLJA:
            if not pokrenuta:
                res[(f, m)] = (None, 0)
                continue
            vals = [score_field(f, r.get(f"{m}_{f}", ""), r.get(f"tocno_{f}", "")) for r in rows]
            res[(f, m)] = (sum(vals) / len(vals), len(vals)) if vals else (None, 0)
    return res


def format_table(res: dict) -> str:
    lines = ["| Polje | Pravila | Haiku 5.5 | Sonnet 5.5 |", "|---|---:|---:|---:|"]
    for f in POLJA:
        cells = []
        for m in METODE:
            v, n = res[(f, m)]
            cells.append(f"{100 * v:.0f} % (n={n})" if v is not None else "-")
        lines.append(f"| {f} | " + " | ".join(cells) + " |")
    return "\n".join(lines) + "\n"


def main():
    if not UZORAK.exists():
        sys.exit("Nema eval/uzorak.csv: pokreni python eval/make_sample.py")
    with UZORAK.open(encoding="utf-8-sig") as fh:
        rows = list(csv.DictReader(fh))
    ocijenjeni = [r for r in rows if any((r.get(f"tocno_{x}") or "").strip() for x in POLJA)]
    if not ocijenjeni:
        sys.exit("Nijedan redak nema ispunjene stupce tocno_*. Ispunite ih ručno pa ponovno pokrenite.")
    print(f"Ocijenjeno redaka: {len(ocijenjeni)} od {len(rows)}\n")
    print(format_table(evaluate(ocijenjeni)))


if __name__ == "__main__":
    main()
