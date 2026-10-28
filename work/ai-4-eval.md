# AI-4: provjera prihvatljivosti, rezultati

3 demo poziva × 2 scenarija. Pitanja je napisao model (Haiku) iz teksta poziva, odgovore sam upisao ja. Provjera je radila preko prave rute servisa, s pravim modelom i pravim tekstom poziva iz seeda.

| Poziv | Scenarij | Očekivano | Sonnet 5.5 | Haiku 5.5 | Razloga s citatom | Primjer citata |
|---|---|---|---|---|---:|---|
| FZOEU-275 | eligible | LIKELY_ELIGIBLE | **LIKELY_ELIGIBLE** (ok) | UNCLEAR | 4 | "Korisnici sredstava Fonda u smislu ovog Poziva su punoljetne fizičke osobe – građani s prebivalištem na područ…" |
| FZOEU-275 | not eligible | LIKELY_NOT_ELIGIBLE | **LIKELY_NOT_ELIGIBLE** (ok) | LIKELY_NOT_ELIGIBLE | 4 | "Korisnici sredstava Fonda u smislu ovog Poziva su punoljetne fizičke osobe – građani s prebivalištem na područ…" |
| HZZ-4347 | eligible | LIKELY_ELIGIBLE | **LIKELY_ELIGIBLE** (ok) | UNCLEAR | 5 | "Osobe za koje se traži dodjela potpore trebaju biti prijavljene u evidenciju nezaposlenih osoba Zavoda u vreme…" |
| HZZ-4347 | not eligible | LIKELY_NOT_ELIGIBLE | **LIKELY_NOT_ELIGIBLE** (ok) | LIKELY_NOT_ELIGIBLE | 4 | "Poslodavci koji su osnovani najmanje 12 mjeseci prije dana podnošenja zahtjeva.…" |
| MZOM-8068 | eligible | LIKELY_ELIGIBLE | **LIKELY_ELIGIBLE** (ok) | LIKELY_ELIGIBLE | 5 | "da su hrvatski državljani, državljani države članice Europske unije koji imaju prijavljen boravak u Republici …" |
| MZOM-8068 | not eligible | LIKELY_NOT_ELIGIBLE | **LIKELY_NOT_ELIGIBLE** (ok) | LIKELY_NOT_ELIGIBLE | 3 | "da imaju status studenta sveučilišnoga ili stručnog studija na visokom učilištu u Republici Hrvatskoj, što se …" |

- **Sonnet daje svih 6 očekivanih verdikta, Haiku 4 od 6** (u oba "prihvatljiva" scenarija za FZOEU i HZZ vraća UNCLEAR, jer nepitane sporedne uvjete tretira kao zapreku). Zato je `LLM_ELIGIBILITY_MODEL` zadano `claude-sonnet-5-5`; ostale značajke ostaju na Haikuu.
- Trošak provjere sa Sonnetom: oko 0,035 USD po pozivu (6 scenarija 0,21 USD). Pri proračunu od 2 USD to je oko 55 živih provjera; kad se potroši, ruta vraća pravila s UNCLEAR umjesto greške.
- Svaki razlog u odgovoru ima doslovni citat iz teksta poziva. Citati se još jednom provjeravaju u kodu (ignoriraju se razmaci, interpunkcija i velika/mala slova); razlog čiji citat nije u tekstu se odbacuje, a verdikt bez ijednog valjanog razloga postaje UNCLEAR.
- Odgovor uvijek ima `disclaimer` i `sourceUrl`.
- Poznata slabost: pitanja ponekad imaju negaciju ("Nemate li dospjelih obveza?"), pa DA nije jasno. Prompt to zabranjuje, ali Haiku to i dalje ponekad napravi (FZOEU 275, pitanje 5).
