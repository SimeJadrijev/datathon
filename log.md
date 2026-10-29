# Dnevnik

## 2026-10-22
- Dogovoren način rada: modularni md fileovi, asistent kritički savjetuje kao tech poduzetnik/softver inženjer. Postavljen kostur foldera.
- Primljen pozivni mail: Datathon Portala otvorenih podataka 2026 (MPUDT). Zapisano u `context/competition.md` i `context/timeline.md`. Uočeno: rok za prijavu je 23.10. u 24:00 (sutra).
- Folder je premješten u `Desktop/other/datathon` (klon repa github.com/SimeJadrijev/datathon). Prvi commit s md fileovima je napravljen i pushan na `origin/main` na izričit zahtjev korisnika. Vidljivost repa NIJE provjerena (`gh` nije instaliran).
- Pročitani Javni poziv i Pravila (PDF-ovi premješteni u `context/izvori/`). `competition.md`, `timeline.md`, `tasks.md` i `decisions.md` prepisani.
- **Ispravak:** prijavu ne podnosi samo mentor, nego svaki član tima ispunjava obrazac. Kriteriji: max 60 b, 9 kriterija, težište na izvedivosti, UI/UX-u i videu. Video do 3 min, obavezan, bez njega se rješenje ne ocjenjuje.
- Korisnik dodao skill `skills/product-brainstorming/SKILL.MD` (generički PM skill: modovi, HMW, JTBD, assumption testing). Nije u `.claude/skills/`, pa nije aktivan. Razgovor o tome koje još skillove dodati u fazi brainstorminga (prijedlog: custom skill za istraživanje data.gov.hr kataloga + custom "žiri" skill s rubrikom od 60 bodova).
- Skill premješten u `.claude/skills/product-brainstorming/SKILL.md` (aktivan). Napisana još 2 skilla: `jury-simulator` i `data-scout`. Provjereno curlom da data.gov.hr CKAN API radi na `/ckan/api/3/action/` (3956 skupova), upisano u `context/data.md`. Ključan nalaz: samo ~55 skupova se osvježava dnevno/tjedno/kontinuirano.
- Prijedlog (još ne odlučeno): ideja mora biti zaključana oko 27.10. da ostane ~10 dana za izradu. Brainstorm iz dva smjera (od podataka i od problema).
- Nepushani/necommitani: PDF-ovi u `context/izvori/`, `.claude/skills/` i izmjene md fileova nakon prvog commita.
- Commit `431ad70` pushan (pravila, skillovi, PDF-ovi). Pokrenuta 3 paralelna data-scouta (A, B, C), rezultati u `context/scout/izazov-*.md`, sažetak u `context/data.md`. Ključno: nema živih okolišnih mjerenja na portalu, geo podaci su gotovo samo Zagreb/Rijeka, C je zasićen. Preporuka izazova upisana u `work/decisions.md` (A ili B, ne C), čeka tim. Nalazi agenata nisu neovisno provjereni. U `/tmp/scout_scripts/` i `/tmp/tmp.DM15WB2E58` ostali privremeni file-ovi scouta (izvan repoa).
- Ispravljeno u `decisions.md`: tvrdnja da prijava ne obvezuje na predaju nije u Pravilima (nepotvrđeno).
- Korisnik (Sime) dao podatke o timu: Sime (data science, AI), Ivan (baze), Luka (DevOps), Nino (full-stack), svi frontend+backend. Upisano u `team/team.md`. Tim razmišlja o prijavi u više izazova.
- Pročitani cijeli scout fileovi, napisane 5 ideja u `work/ideas.md`. Revidirana preporuka (vidi `work/decisions.md`): glavni kandidat "Spojnik" (alat za otkrivanje/spajanje/zdravlje skupova, izazov C), rezerva "Profil četvrti Zagreb" (A). Raniju preporuku "ne C" ispravio sam (vrijedila je samo za dashboard javne nabave). Bodovi su moje grube procjene, `jury-simulator` nije formalno pokrenut. Skillovi iz `.claude/skills/` nisu učitani u ovoj sesiji (nema ih u `/context`), treba restart.
- Korisnik nije razumio ideju Spojnik i traži više ideja. Dodano pravilo u CLAUDE.md: na jednostavna pitanja odgovaraj kratko. Drugi krug ideja (6–12) upisan u `work/ideas.md`: AI za PDF/XLS tablice, zdravlje portala, graf javnog sektora, bicikl odluka Zagreb, feed iz dnevnih registara, karta kulturne praznine, chat nad podacima.
- Skillovi `video-script` (3 min) i UI/UX nisu napisani, treba ih tek u fazi izrade.

## 2026-10-24
- Dodani skillovi za fazu izrade: `.claude/skills/spring-boot-api` (slojevi, DTO recordi, jedinstveni oblik greške `{statusCode, message, error}` usklađen s frontendom, klijenti za vanjske API-je, testovi) i `.claude/skills/react-web` (struktura s barrelima, API/Query hookovi, wouter rute, UI/UX pravila za 4 stanja ekrana). Grana `chore/coding-skills`. Uočeno: `vite.config.ts` još nema proxy `/api` → `localhost:8080`.
- Skillovi `spring-boot-api` i `react-web` prevedeni na engleski; odluka o jeziku u `work/decisions.md`.
- Ispravak: sav kod na engleskom (ne hrvatske domenske imenice). Skillovi ažurirani, dodan rječnik domenskih pojmova HR → EN u `spring-boot-api`.

## 2026-10-26
- Pregledan branch `origin/poc/natjecaji-scraper` bez checkouta (`git show`). POC: 11 scrapera, SQLite, ~1485 natječaja (~185 otvorenih), NN zakoni (3842), `match.py`, mini web, izvještaj. Sažetak i kritika dani korisniku.
- Subagent-recenzent je našao propuste (demo se ne može pokrenuti iz repoa, podaci slabiji nego u izvještaju, obavijesti su simulacija). Nalazi i odluke u `work/decisions.md`.
- Odluke korisnika: samo izazov C, Spring Boot fiksan (Python ingest samo ako je bolji, asistent preporučuje prepis u Javu), zakoni jednako važni, bez logina, Claude API s cacheom, Sudreg treba registraciju, A ostaje rezerva, podjela posla još nije dogovorena.
- MVP breakdown upisan u `work/tasks.md` (M0-M8 + nice-to-have). GitHub issueji NISU otvarani, čekaju korisnikovu potvrdu.
- Commit 1e336e5 (dokumentacija odluka i MVP plana na main, nije pushano). Korisnik potvrdio prepis scrapera u Javu. Napisan `work/mvp-task-specs.md`: zajednički kontekst + specifikacije M0-M8 + nice-to-have + kritični put. GitHub issue-e otvara korisnik. File još nije commitan.
- `work/mvp-task-specs.md` v2: korisnik traži manje taskove (jedan task = jedan PR, otprilike po featureu) i engleski jezik. Prepisano u 40 taskova s prefiksima SETUP/DATA/API/INGEST/AI/WEB/QUALITY/DELIVERY (~30 dana rada), uz pregled, ovisnosti, kritični put i prijedlog podjele na 4 osobe. Stare oznake M0-M8 više ne postoje (u `work/tasks.md` još jesu, treba uskladiti). Nije commitano, issue-e otvara korisnik.
- `work/mvp-task-specs.md` v3: korisnik odustao od zahtjeva za male PR-ove i traži 7-8 taskova. Preoblikovano u 8 radnih paketa (jedan paket = jedan issue = jedan vlasnik), 40 koraka zadržava ID-eve. Dodana pravila protiv merge konflikata: pom.xml i application.yaml se diraju samo u DATA-1, sve rute i placeholder stranice u WEB-1, FundingCallCard iz WEB-4 koristi WEB-5. Vlasnici (Sime 1+5+QUALITY, Ivan 2+3, Luka 4+7, Nino 6+DELIVERY) su prijedlog, nepotvrđeni. Sigurnosna kopija v2 je izvan repoa. Nije commitano.
- Korisnik traži da se iz specifikacija ukloni tko radi koji paket (dogovara tim interno). Imena i sekcija "Suggested owners" uklonjeni iz `work/mvp-task-specs.md` i `work/tasks.md`, redoslijed rada sada ide po paketima. Prijedlog podjele iz ranijeg zapisa više ne vrijedi.

## 2026-10-26 (Luka, večer)
- Luka preuzeo GitHub issue #3 (Foundations) i #4 (Ingest core). Branch `feature/pkg-1-foundations`.
- DATA-1 gotov (commit 95bf9b3): entiteti FundingCall, Regulation, IngestRun, SourceRunLog, LlmUsage, enumi, repozitoriji, ovisnosti (Jsoup, PDFBox, springdoc, anthropic-java) i config blokovi. Odluke u `work/decisions.md`.
- DATA-2 gotov (commit d94c901): POC ponovno pokrenut lokalno (1484 natječaja, 3860 propisa), `tools/export_poc_snapshot.py` izvezao 693 natječaja iz 6 MVP izvora + 3860 propisa (5,2 MB). `SeedLoader` puni praznu bazu pri startu (~25 s), drugi start bez duplikata.
- SETUP-1 otvoren: Sudreg ključ (Luka) je u `apps/api/.env` (gitignored). Token se dobije, ali svi API pozivi vraćaju 401, vjerojatno račun još nije aktiviran. Demo OIB-ovi i fixturei čekaju.
- Lokalno instaliran JDK 25 (`java-25-openjdk-devel`).
- SETUP-1 gotov: Sudreg API proradio (prvo je vraćao 401 unatoč tokenu). Izabrani demo OIB-ovi Microblink, Valamar Riviera, Kraš; fixturei spremljeni. Nalaz: `glavna_djelatnost` često prazna (vidi `work/decisions.md`).

## 2026-10-27 (paket 4, API)
- Razrađen plan `work/api-plan.md` i doneseno 9 od 10 odluka (P4 Sudreg otvoren, treba tek u API-5/API-7). Odluke u `work/decisions.md`.
- API-1 napisan na branchu `sime/api`: `common/` (ErrorResponse, iznimke preuzete iz `feature/pkg-5-ai`, prošireni `GlobalExceptionHandler` s 502 i porukom "polje: razlog", `PageResponse`, `PageRequests` s limitom 100, `OpenApiConfig`), springdoc path u `application.yaml`. 13 testova u `GlobalExceptionHandlerTest` prolazi bez baze.
- Nije provjereno: Swagger UI u pregledniku i testovi koji traže Postgres na 5433 (Docker nije dostupan u ovoj sesiji, 5 postojećih testova pada na "Connection refused", ne zbog izmjena).

## 2026-10-28 (paket 4, API)
- API-2 gotov: lista i detalj natječaja s filterima, unaccent pretraga, N+1 provjeren (5 upita po listi). Istekli rokovi kod OPEN natječaja ostavljeni kako jesu, zapisano u QUALITY-1.
- API-3 gotov: `GET /api/funding-calls/facets` (6 `GROUP BY` upita, svaki facet ignorira vlastiti filter). Zajednički `FacetCounter` i `TextSearch` u `common/`.
- API-4 gotov: `GET /api/regulations`, `/{id}`, `/facets`. Ukupno 63 testa prolazi, provjereno curlom na stvarnim podacima, primjeri u `work/api-examples.md`.
- Otvoreno: odluka P4 (Sudreg) prije API-5. Provjere (code review, ručne provjere) po dogovoru idu nakon API-4.
- API-5 gotov: `GET /api/sources` i `GET /api/meta`, `SUDREG` dodan u `SourceCode` (odluka P4), katalog proširen opisom, metodom i licencom.
- API-6 gotov: `POST /api/profiles/match`, čista funkcija `MatchingService` (port `poc/match.py`, status nepoznat bez kazne), 15 unit testova, provjereno na tri POC profila.
- API-7 gotov: `POST /api/profiles/from-oib`, `@Oib` validator, `SudregClient` + `FixtureSudregClient` + NKD mapiranje, fixturei kopirani u `main/resources/sudreg-fixtures`. Nalaz: Spring je u WARN logu ispisivao odbijeni OIB (`rejected value`), ugašeno s `spring.mvc.log-resolved-exception: false`.

## 2026-10-28 (Luka)
- PR #12 (paket 1) mergan (Nino). Krenut #4 na `feature/pkg-2-ingest-core`.
- INGEST-1 do 4 gotovi: HTTP klijent (robots.txt s BOM-om, 1 req/s, cache, retry, cp1250), okvir (filtar, pravila za rok/iznose/skupine/područja, upsert po hashu, log po izvoru), udruge.gov.hr i FZOEU s PDF-ovima. 65 testova prolazi.
- Pravi run: udruge 141 zapisa (90 otvorenih, 72 s rokom; ostali nemaju rok na izvoru), FZOEU 29 (9 otvorenih). Prvi run je otkrio da filtar po naslovu izbacuje pravi FZOEU poziv; ispravljeno. Usporedba s POC-om i odluke u `work/decisions.md`.
- Admin ruta čeka AI PR #13 (`AdminKeyGuard`, `BatchRunner`); do tada `INGEST_ON_STARTUP=true`.

## 2026-10-29 (Luka)
- Na `main` mergani PR #13 (AI), #16 (API) i #15 (ingest); svih 208 testova prolazi. Web PR #14 zatvoren bez mergea (kod na `feature/pkg-6-web`).
- Admin ruta `POST /api/admin/ingest` dodana izravno na `main` (AdminKeyGuard + BatchRunner): 202/401/400/409 provjereni curlom, FZOEU run 29 zapisa. README dopunjen uputom. #4 zatvoren.


## 2026-10-29 (Sime)
- Branch `sime/ingest-other-sources`, paket 3 (ostali izvori). INGEST-5 (fondovieu NPOO) gotov: parser + fetcher, 10 testova na spremljenim stranicama (`fixtures/eu-funds-npoo/`, uključujući pravi `robots.txt`), živi run 142 poziva s rokom za sve, 0 grešaka. Usporedba i odluke u `work/decisions.md`. Sljedeće: INGEST-6 (HZZ i APPRRR).
