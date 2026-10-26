# Zadaci

| Zadatak | Vlasnik | Rok | Status |
|---|---|---|---|
| Naći mentora (nastavnik na visokom učilištu na kojem studira barem jedan član tima) i dobiti njegove kontakt podatke za obrazac | | 23.10. | GOTOVO (prijava predana) |
| Potvrditi da su svi članovi studenti i da nitko nije u drugom Datathon timu | | 23.10. | OTVORENO |
| Odlučiti izazov(e) za prijavu | tim | 23.10. | GOTOVO: A i C (B odbačen) |
| **Svaki član** ispuni online prijavni obrazac (naziv tima, izazov, kontakt mentora, privola za video) | svi 4 | 23.10. | GOTOVO (prijavljeni na A i C) |
| Pregledati prijavni obrazac prije ispunjavanja: što traži (JMBAG? OIB? potvrda?) | korisnik | 23.10. | OTVORENO |
| Provjeriti je li GitHub repo privatan i smije li kod javno na GitHub (sigurnosna politika). Utječe na predaju koda | korisnik | prije 9.11. | OTVORENO |
| Pročitati Javni poziv i Pravila | asistent | 22.10. | GOTOVO (vidi `context/competition.md`) |
| Pregledati data.gov.hr datasetove i API-je (data-scout A/B/C) | asistent | 22.10. | GOTOVO (vidi `context/scout/`) |
| Odlučiti koje ideje stvarno gradimo (A, C ili oba) nakon jeftinih testova | tim | ~27.10. | OTVORENO |
| Brainstorm ideje (`product-brainstorming`) pa `jury-simulator`, zaključati ideju | tim + asistent | ~27.10. | U TIJEKU (ideje u `work/ideas.md`) |
| Test za "Spojnik" | | | ODBAČENO: ideja uklonjena (vidi `work/ideas.md`) |
| Test podataka za 27 DoktorGdje: postoje li liste čekanja HZZO strojno čitljivo (portal ili hzzo.hr), s kojom granulacijom i svježinom | asistent | 24.10. | PRIJEDLOG, čeka potvrdu korisnika |
| Test podataka za 25 KrizniŠtab OS: jesu li poplavne zone Hrvatskih voda upotrebljive (WFS/geometrija), imaju li ustanove socijalne skrbi koordinate | asistent | 24.10. | PRIJEDLOG, čeka potvrdu korisnika |
| Jeftini testovi: Radar nabave (udio EOJN redaka s `DurationTo`, ponavljaju li se ugovori po naručitelj + CPV) i Pristupačnost (je li OSM pješačka mreža Zagreba dovoljno gusta) | Sime + Ivan | ~25.10. | PRIJEDLOG, čeka potvrdu korisnika |
| Podijeliti uloge (prijedlog u `team/team.md`): Sime data/ML, Ivan baza, Luka DevOps/crawler, Nino frontend | tim | 23.10. | OTVORENO |
| Restartati Claude Code sesiju da se učitaju skillovi iz `.claude/skills/` | korisnik | prije sljedeće sesije | GOTOVO (skillovi vidljivi u `/context`) |
| Provjera NN API-ja za 29: GOTOVO (radi, metapodaci preko API-ja, tekst HTML scraping, 3 zahtjeva/s). Otvoreno: licenca za ponovnu uporabu teksta, mapiranje propis→djelatnost, ima li smisla samostalni "Pravni štit" | asistent + tim | 24.10. | OTVORENO |
| Provjeriti licence vanjskih izvora (DHMZ, Open-Meteo, Eurostat, Autotrolej API) | asistent | prije zaključavanja ideje | OTVORENO |
| Pitati organizatora: obvezuje li prijava na predaju, može li se izazov mijenjati, pod koji se kriterij boduje OSS objava | korisnik | 23.10. | OTVORENO |
| Dogovoriti interni plan: prototip do ~6.11., video i dokumentacija do 9.11. | tim | nakon prijave | U TIJEKU (MVP breakdown ispod) |

## MVP: agregator natječaja i zakona (izazov C), plan od 26.10.
Osnova: branch `poc/natjecaji-scraper`. Odluke i obrazloženja u `work/decisions.md` (26.10.). Procjene su u danima rada, vlasnici još nisu dodijeljeni.

### MVP (nužno za demo i video)
| # | Zadatak | Procjena | Vlasnik | Status |
|---|---|---|---|---|
| M0 | Registrirati Sudreg ključ + odabrati pravi OIB za demo | 0,2 d | | OTVORENO |
| M1 | Postgres shema (natječaji, propisi) + seed snapshot iz POC SQLite-a, commitan s datumom ("stanje na dan") | 1,5 d | | OTVORENO |
| M2 | Spring API: `/funding-calls`, `/regulations`, `/match`, OpenAPI dokumentacija | 2 d | | OTVORENO |
| M3 | Java ingest za 6 izvora (udruge, FZOEU, fondovieu, HZZ, APPRRR, MZOM) + NN zakoni, provjera naspram POC-a | 3 d | | OTVORENO |
| M4 | React UI: onboarding profila (tip, županija, interesi ili OIB), kartice "zašto ti odgovara", detalj natječaja, tab zakoni s filtrima | 4 d | | OTVORENO |
| M5 | AI sloj: LLM sažetak uvjeta, "ispunjavam li uvjete?" (upitnik nad `uvjeti_sazetak`), sažetak zakona; cache u bazi, disclaimer + izvor | 2 d | | OTVORENO |
| M6 | Ručno čišćenje ~50-100 demo natječaja (skupine, status, duplikati) + ručna točnost na 15 zapisa (`eval/uzorak.csv`, cilj rok i iznos ≥ 85 %) | 1,5 d | | OTVORENO |
| M7 | Video do 3 min: scenarij odmah, snimanje, montaža | 2 d | | OTVORENO |
| M8 | Dokumentacija: arhitektura, popis skupova s izdavateljima i poveznicama, korišteni AI alati (obvezno) | 1 d | | OTVORENO |

### Nice-to-have (tek kad MVP stoji)
- Scheduler s pravom razlikom dvaju dohvata + stvarne obavijesti (mail).
- Poveznica zakon↔natječaj preko NN broja/ELI.
- Standard "Otvoreni podaci o natječajima" (jedna stranica + JSON primjer, DCAT-AP).
- eufondovi.gov.hr, Registar udruga po OIB-u, JPT popis, NKD 2025 preko data.gov.hr.

### Rizici za praćenje
- Javni GitHub vs sigurnosna politika (fallback: arhiva/link na SharePointu).
- Demo mora raditi bez ključeva (predračunani rezultati); Sudreg ključ je vanjska ovisnost.
- Zastarjeli natječaji u trenutku ocjenjivanja: prikaz "stanje na dan".
- "Ispunjavam li uvjete?" je pravni kontekst: citirati izvor, disclaimer.
- Pitanja organizatoru (open.data@mpudt.hr): obveza predaje, promjena izazova, kriterij za OSS, smije li runtime AI biti API (pravilo o OSS).
