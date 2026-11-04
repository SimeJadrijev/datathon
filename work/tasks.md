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
Detaljna razrada je u `work/mvp-task-specs.md` (v3, 26.10.): **8 radnih paketa** (jedan paket = jedan GitHub issue = jedan vlasnik), unutar njih 40 koraka s ID-evima (SETUP, DATA, API, INGEST, AI, WEB, QUALITY, DELIVERY), ~30 dana rada, na engleskom. Veličina PR-ova nije bitna, bitno je izbjeći merge konflikte (svaki vlasnik radi u svojim mapama). Stare oznake M0-M8 više ne vrijede. Status i vlasnici se prate na GitHub issue-ima koje otvara korisnik. Vlasnike dogovara tim interno, nisu upisani.

| Paket | Sadržaj | Dana | Vlasnik | Status |
|---|---|---|---|---|
| 1 Foundations (#3) | SETUP-1, DATA-1, DATA-2 | 2,2 | Luka | GOTOVO, PR #12 mergean, issue zatvoren |
| 2 Ingest: core and first sources (#4) | INGEST-1 do 4 | 3,3 | Luka | GOTOVO (PR #15 + admin ruta izravno na main) |
| 3 Ingest: other sources | INGEST-5 do 9 | 3,1 | | OTVORENO |
| 4 API (#6) | API-1 do 7 | 4,8 | Sime | GOTOVO, PR #16 mergean 28.10., issue zatvoren (primjeri u `work/api-examples.md`) |
| 5 AI (#7) | AI-1 do 4 | 3,2 | Nino | PR #13 mergean 27.10. Follow-up iz recenzije (seed s AI rezultatima, istekao rok, ručno provjereni pozivi, hrvatske poruke) gotov 3.11., nije commitan |
| 6 Web: foundation, profile, recommendations (#8) | WEB-1 do 4 | 3,2 | Ivan | GOTOVO 29.10.: svi kriteriji WEB-1 do 4 provjereni, PR za `feature/pkg-6-web` otvoren (`Closes #8`), čeka review. Screenshotovi u `work/screenshots/pkg-6/` |
| 7 Web: lists, details, laws, sources (#9) | WEB-5 do 9 | 3,9 | Ivan | GOTOVO 3.11.: PR #22 mergean, WEB-7 provjeren s pravim ključem (natječaj 78: ispunjavaš / ne ispunjavaš / nejasno, sve s citatima), screenshotovi u `work/screenshots/pkg-7/`, PR #24 zatvara #9 |
| 8 Quality and delivery | QUALITY-1 do 3, DELIVERY-1 do 5 | 6,2 | | OTVORENO |

**Pitanja za kolege u PR-u paketa 6, postaviti kao komentar:**
1. ~~Naziv aplikacije "Natječaji i zakoni" i logo "NZ" su radni izbor.~~ 29.10. promijenjeno u **OtvoreniRadar** na `refactor-ui` (vidi `work/decisions.md`).
2. Kad profil postoji, početna prvo pokazuje sažetak profila, a forma se otvara na "Promijeni profil". Spec (WEB-2) kaže "forma je popunjena i nudi Promijeni profil". Slažemo li se s odstupanjem?

**Otvoreno za web:** provjeriti u Firefoxu (na Ivanovom računalu nije instaliran, testirano samo u Chromiumu).

**~~Blocker~~ RIJEŠENO 3.11. (seed sad nosi AI rezultate, vidi `log.md`) za WEB-7, WEB-8 i demo bez ključa (28.10.):** AI rezultati (polja iz AI-2, sažeci AI-3, pitanja AI-4) nisu u seedu. Svježa baza ima 0 pitanja za prihvatljivost, 0 sažetaka propisa i 0 natječaja s `extractedByLlm`. Tko nema `ANTHROPIC_API_KEY` i ne pokrene admin batcheve, ne vidi panel "Ispunjavam li uvjete?" ni AI sažetke. Odluka AI paketa kaže "demo radi bez ključa", pa treba izvoz AI rezultata u seed (ili zaseban seed file). Vlasnik: AI paket (#7) ili DELIVERY. Javiti Ninu.

### Nice-to-have (#11, Luka, branch `feature/nice-11`, 3.11.)
- GOTOVO: novi izvori eKohezija (EU 2021.-2027.), 18 ministarstava, 32 grada i županije, agencije (HRZZ, AMPEU, ribarstvo), sveučilišta i studentski centri. Seed sada ima 1.192 natječaja, a 167 od 170 otvorenih ima AI polja.
- GOTOVO: Registar udruga, profil udruge iz OIB-a (42.703 aktivne udruge u seedu).
- GOTOVO: pravna osnova (navodi NN u tekstu poziva, veza s propisom; ciljani uvoz starijih citiranih propisa).
- GOTOVO: export u predloženom standardu i JSON Schema.
- GOTOVO: „novo” (firstSeenAt), brojanje novih i promijenjenih po izvoru, dnevni raspored, Atom feed umjesto maila.
- Nije rađeno: JPT popis davatelja, NKD 2025, preostali gradovi (popis u `work/sources-research.md`), automatska detekcija duplikata među izvorima (npr. APPRRR i Ministarstvo poljoprivrede objavljuju isti poziv).

### Rizici za praćenje
- Javni GitHub vs sigurnosna politika (fallback: arhiva/link na SharePointu).
- Demo mora raditi bez ključeva (predračunani rezultati); Sudreg ključ je vanjska ovisnost.
- Zastarjeli natječaji u trenutku ocjenjivanja: prikaz "stanje na dan".
- "Ispunjavam li uvjete?" je pravni kontekst: citirati izvor, disclaimer.
- Pitanja organizatoru (open.data@mpudt.hr): obveza predaje, promjena izazova, kriterij za OSS, smije li runtime AI biti API (pravilo o OSS).
