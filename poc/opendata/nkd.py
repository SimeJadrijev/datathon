"""NKD (Nacionalna klasifikacija djelatnosti) -> područja interesa za podudaranje.

Mapiranje je na razini odjeljka (prve dvije znamenke), što je dovoljno za
grubo podudaranje s područjima natječaja. Nazivi odjeljaka mogu se povući iz
DZS skupa "Nacionalna klasifikacija djelatnosti 2025." (data.gov.hr), ali za
podudaranje trebaju samo šifre.
"""

ODJELJCI = [
    ((1, 3), ["poljoprivreda"]),
    ((5, 9), ["poduzetnistvo"]),
    ((10, 33), ["poduzetnistvo", "inovacije"]),
    ((35, 35), ["energetika"]),
    ((36, 39), ["okolis"]),
    ((41, 43), ["poduzetnistvo", "energetika"]),
    ((45, 47), ["poduzetnistvo"]),
    ((49, 53), ["promet"]),
    ((55, 56), ["turizam"]),
    ((58, 63), ["digitalizacija", "inovacije"]),
    ((64, 68), ["poduzetnistvo"]),
    ((69, 71), ["poduzetnistvo"]),
    ((72, 72), ["znanost", "inovacije"]),
    ((73, 82), ["poduzetnistvo"]),
    ((85, 85), ["obrazovanje"]),
    ((86, 86), ["zdravstvo"]),
    ((87, 88), ["socijala"]),
    ((90, 91), ["kultura"]),
    ((93, 93), ["sport"]),
    ((94, 94), ["civilno_drustvo"]),
]


def podrucja_za_nkd(sifra: str) -> list[str]:
    try:
        odjeljak = int(str(sifra).split(".")[0])
    except ValueError:
        return []
    for (lo, hi), p in ODJELJCI:
        if lo <= odjeljak <= hi:
            return p + ["zaposljavanje"]
    return ["poduzetnistvo", "zaposljavanje"]
