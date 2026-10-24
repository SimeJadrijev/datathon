"""Registar izvora. Svaki modul ima IZVOR, METODA, fetch_list() i fetch_detail(item).

KATEGORIJA povezuje modul s točkom 3 upute (8 skupina izvora).
"""
from sources import (ampeu, apprrr, fondovieu, fzoeu, hamag, hzz, mzom, osijek,
                     rijeka, udruge, zagreb)

IZVORI = [
    ("1. APPRRR", apprrr),
    ("2. FZOEU", fzoeu),
    ("3. EU fondovi (NPOO)", fondovieu),
    ("4. HAMAG-BICRO", hamag),
    ("5. HZZ mjere", hzz),
    ("6. Ured za udruge", udruge),
    ("7. Stipendije", mzom),
    ("7. Stipendije", ampeu),
    ("8. Gradovi", rijeka),
    ("8. Gradovi", osijek),
    ("8. Gradovi", zagreb),
]
