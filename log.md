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

## 2026-10-27 (Ivan)
- Ivan preuzeo paket 6 (issue #8). Paketi 3 (#5) i 8 (#10) još nemaju vlasnika; API kolega se treba assignati na #6.
- WEB-1 napravljen na `feature/pkg-6-web` (nije commitano): Vite proxy `/api` → 8080, `global.css` (CSS varijable, reset), `AppLayout` sa sticky headerom, navigacijom (mobilni izbornik), skip linkom i footerom; rute za sve stranice + placeholder stranice; komponente `Button`/`ButtonLink`, `Card`, `Badge`, `ExternalLink`, `PageHeader`, `LoadingState`, `ErrorState`, `EmptyState`; `utils` (`formatDate`, `formatAmount`, `formatDeadline`, `isDeadlineUrgent`, `classNames`). ErrorPage i NotFoundPage na hrvatskom, axios greške imaju hrvatsku zamjensku poruku. `yarn lint` i `yarn build` prolaze, provjereno u pregledniku na 1280 px i mobitelu.
- Radni naziv aplikacije "Natječaji i zakoni" (`constants/app.ts`), tim ga može promijeniti na jednom mjestu.
- Nalaz: projekt traži Node 22.13+ (`@eslint/js`), README ispravljen. Na Ivanovom računalu je Node 22.12, Java 11 (treba JDK 25) i Docker nije pokrenut.
- WEB-1 commitan (`9a150b3`). Dogovoreno: paket 6 ide u dva PR-a (vidi `work/decisions.md`).
- WEB-2 gotov: forma profila na početnoj (tip kao kartice, područja kao čipovi, županija, ključne riječi), `useProfile` sprema u `localStorage` (ključ `datathon.profile.v1`) i odbacuje neispravne spremljene podatke, sažetak profila s "Promijeni profil" i "Obriši profil", panel "Kako radi". Tipovi `TargetGroup`, `Area`, `Profile` (= `ProfileRequest` iz API-6). Provjereno u pregledniku: validacija i fokus, spremanje, osvježavanje, uređivanje, brisanje, mobitel, tipkovnica.
- Nalaz za API/QUALITY: u seedu `counties` miješa `ALL` i "Područje cijele Hrvatske" (8 zapisa); županije su u kratkom obliku ("Primorsko-goranska"), forma šalje isti oblik.
- Ivanove primjedbe nakon lokalnog pregleda ispravljene: tekst uz županiju, nova komponenta `Select` (strelica s razmakom; u Chromeu/Edgeu 135+ stiliziran popis opcija preko `appearance: base-select`, ostali preglednici imaju nativni popis), odabrani tip se može odznačiti ponovnim klikom, kompaktniji razmak u sažetku profila. Pitanja za kolege u PR 1 zapisana u `work/tasks.md`. Firefox nije instaliran, testirano samo u Chromiumu.
- Provjereno u specu: više profila ni `additionalTypes` u web koracima nisu traženi, profil ostaje s jednim tipom.
- Proxy provjeren pravim pozivom: s pokrenutim API-jem `curl.exe -i http://localhost:5173/api/meta` vraća Springov JSON 404. Nalaz za API-1: zbog devtoolsa Spring u grešci vraća cijeli stack trace.
- Branch `feature/pkg-6-web` pushan. `gh` CLI nije instaliran, pa PR 1 otvara Ivan ručno na GitHubu s pripremljenim opisom (engleski, `Refs #8`, pitanja za kolege i napomene za API i paket 7).

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

## 2026-10-28 (Ivan, paket 6)
- `origin/main` (API #16, AI #13, ingest #15) spojen u `feature/pkg-6-web`; konflikti samo u `work/tasks.md`, `work/decisions.md` i `log.md`. PR #14 je zatvoren bez mergea, paket ide u jedan PR.
- WEB-3 gotov: OIB polje iznad forme, provjera kontrolne znamenke u pregledniku, poruke za 400/404/502, popunjavanje forme iz Sudrega (tip, područja, županija, NKD). Provjereno na Valamaru (Istarska, turizam), neispravnom OIB-u i valjanom OIB-u koji nije u registru. 502 nije provjeren uživo.
- WEB-4 gotov: `/preporuke` sa `FundingCallCard`, "Zašto ti odgovara", upozorenjem za nepoznat status i sažetkom. Tri testna profila (student Rijeka 20, građanin e-auto 9 s e-auto pozivom prvim, tvrtka 8) daju smislene liste. Provjerena sva četiri stanja, 1280 px i 375 px. `yarn lint` i `yarn build` prolaze.
- Nalazi: (1) svježa baza nema nikakve AI rezultate, blocker za WEB-7 i demo bez ključa (u `work/tasks.md`); (2) za studenta je prvi rezultat stipendija za romsku manjinu, matching ne zna za uže uvjete (za QUALITY); (3) `formatDate` daje "03. 11. 2026." s vodećom nulom.

## 2026-10-29 (Ivan, paket 6)
- Dorade kartica nakon pregleda: naslov u 3 retka, "provjeri na izvoru" bez roka, razlozi bez ponavljanja oznaka, manji naslov na mobitelu, izvor kao oznaka. Županije abecedno. OIB s razmacima više ne gubi znamenku.
- Provjera svih kriterija paketa 6 iz `work/mvp-task-specs.md`: sve ispunjeno, odstupanja zapisana u PR-u. 502 za OIB provjeren lažnim serverom, forma i preporuke prođene samo tipkovnicom.
- Nalaz: istekli OPEN natječaji ne ulaze u preporuke (matching ih izbacuje), ali su na listi `/natjecaji` (10 na prvoj stranici), bitno za WEB-5.
- Nalaz: kartica e-auto poziva (EnU-2/26) pokazuje rok 31. 3. 2028. i 2.500 €, što je pogrešno; AI-2 to ispravlja, ali AI rezultati nisu u seedu.
- Sudreg ključevi od Luke ne rade ništa dok ne postoji INGEST-9; Spring ne čita `.env` sam.

## 2026-10-29 (Luka)
- Na `main` mergani PR #13 (AI), #16 (API) i #15 (ingest); svih 208 testova prolazi. Web PR #14 zatvoren bez mergea (kod na `feature/pkg-6-web`).
- Admin ruta `POST /api/admin/ingest` dodana izravno na `main` (AdminKeyGuard + BatchRunner): 202/401/400/409 provjereni curlom, FZOEU run 29 zapisa. README dopunjen uputom. #4 zatvoren.


## 2026-10-29 (Sime)
- Branch `sime/ingest-other-sources`, paket 3 (ostali izvori). INGEST-5 (fondovieu NPOO) gotov: parser + fetcher, 10 testova na spremljenim stranicama (`fixtures/eu-funds-npoo/`, uključujući pravi `robots.txt`), živi run 142 poziva s rokom za sve, 0 grešaka. Usporedba i odluke u `work/decisions.md`. Sljedeće: INGEST-6 (HZZ i APPRRR).


## 2026-10-30 (Sime)
- Paket 3: INGEST-6 (HZZ, APPRRR), INGEST-7 (MZOM), INGEST-8 (Narodne novine s ELI) i INGEST-9 (Sudreg klijent, NKD mapper) napisani i commitani po koracima. 288 testova prolazi. Zivi run samo za HZZ (97 mjera). APPRRR, MZOM, NN i pravi Sudreg nisu provjereni zivo; APPRRR zahtijeva povjerenje u interni Infobip CA. Detalji u `work/decisions.md`.

## 2026-10-29 (Nino, redizajn)
- `refactor-ui` fast-forwardan na `origin/feature/pkg-6-web` (na `main` je web bio prazan kostur). Redizajn radi na Ivanovom kodu paketa 6, PR za paket 6 još nije mergean, pa redoslijed mergea treba dogovoriti.
- Novi tokeni (`global.css`), Inter, nove komponente `Icon`, `TextInput`, `Notice`, `ListSkeleton`, prerađeni `Button` (4 varijante, 2 veličine), `Badge` (tonovi `primary`, `info`, `ai`), `Card`, `PageHeader` (eyebrow), state paneli, `FundingCallCard` (fakti desno, izvor u podnožju), layout i navigacija, Početna (+ `BrowseLinks`), Moji natječaji (skeleton, razlozi s kvačicama, `Notice`).
- `DESIGN_SYSTEM.md` u rootu; pravilo dodano u `CLAUDE.md` i skill `react-web`. Odluke u `work/decisions.md`.
- Provjera: `yarn lint` i `yarn build` prolaze. Headless Chrome snimke: početna (1280 i 600 px), preporuke s lažnim podacima (mock server izvan repoa), greška kad API ne radi, prazno stanje bez profila. Pravi backend nije provjeren (Docker nije radio).
- `components/` preslagan: svaka komponenta u svojoj mapi (`.tsx` + `.module.css` + `index.ts`), `components/index.ts` izvozi mape; EmptyState/ErrorState/LoadingState su u `StatePanel/` jer dijele CSS. Opisano u `DESIGN_SYSTEM.md` §7 i skillu `react-web`. Lint i build prolaze.
- `api/` i `hooks/` preslagani po domeni, jedan poziv/hook po fileu: `api/profiles/getProfileMatches.ts` (bio `matchProfile`), `getProfileFromOib.ts`; `hooks/profiles/useGetProfileMatches.ts` (bio `useRecommendations`), `useGetProfileFromOib.ts` (bio `useProfileFromOib`), `useProfile.ts`, `profileKeys.ts` (ključ `match` → `matches`). Pravilo imenovanja (`<action><Value>` za API, `use<Action><Value>` za hook, akcije get/create/update/delete) u skillu `react-web`.
- `constants/` grupiran: `app/` (naziv, locale), `codeLists/` (područja, ciljne skupine, županije, dijele ih profil i natječaji), `fundingCalls/` (`fundingCallLabels.ts`, bio `fundingCalls.ts`). Importi `@/constants` nepromijenjeni.
- `types/` po domeni (`api/`, `codeLists/`, `fundingCalls/`, `profiles/`, `sources/`), `utils/` po namjeni (`formatting/`, `validation/`, `styling/`). Importi `@/types` i `@/utils` nepromijenjeni. Pravilo u skillu `react-web`.

## 2026-10-29 (Ivan, prijedlog "manje AI izgleda" na PR #18)
- Recenzija PR-a #18: lint i build prolaze, nema blokirajućih bugova. Nalazi: AI oznaka iste boje kao status "Najavljen", tekst 12-15 px premalen za video, score podudaranja se ne vidi, OIB blok prije koraka 1, BrowseLinks ponavlja navigaciju, commit "wip" bez opisa.
- Lokalni branch `design/de-ai-prototype` (iz `origin/refactor-ui`, nije pushan): Source Serif 4 za naslove + IBM Plex Sans umjesto Intera, veći tipografski raspon (16 px tijelo), AI vlastita boja i oznaka na samoj vrijednosti, `FundingCallRow` + `FundingCallList` (jedan okvir s razdjelnicima, datum kao kalendarski blok) umjesto kartica, razlozi kao jedan redak teksta, početna s pravim brojevima iz `GET /api/meta` i listom rokova iz `GET /api/funding-calls`, OIB kao `<details>`, maknuti eyebrow, "Kako radi", BrowseLinks i ikone ispred oznaka. `DESIGN_SYSTEM.md` ažuriran.
- Score se i dalje ne prikazuje: backend vraća bodove bez gornje granice, postotak bi bio izmišljen. Treba normalizirati u API-ju (0-100) ako ga želimo pokazati.
- Provjera: lint i build prolaze, 1280 i 375 px s mock API-jem (izvan repoa). Pravi backend nije provjeren.
- Nakon povratne informacije: OIB je opet otvoren (zeleni panel na vrhu obrasca) s razdjelnikom "ili" u zelenom krugu prema ručnim koracima. Datum u "Rokovi" bez okvira ("30 / lis"). Naslovi se više ne skraćuju (bez line-clamp i trotočke).
- Provjereno na pravom API-ju (lokalno, 8080): 14 od 46 natječaja sa statusom OPEN i rokom u sljedeća 2 tjedna ima rok u prošlosti (npr. 13. 6. 2023.). Web ih je filtrirao u `ClosingSoon` (kasnije maknut, preporuke ih ne vraćaju), ali filter `status=OPEN` u API-ju treba isključiti istekle rokove. Dio naslova i davatelja je VELIKIM SLOVIMA iz izvora.
- Bočni panel na početnoj ovisi o profilu: bez profila "Kako radi" (3 koraka, serifni brojevi) + "Ili pregledaj bez profila" (tekstualni linkovi); s profilom "Sljedeći rokovi za tebe": 3 najbliža buduća roka među preporukama za profil (isti upit kao "Moji natječaji"), "Rok uskoro" za rokove do 7 dana. Opći panel "Rokovi u sljedeća dva tjedna" maknut. Provjereno na pravom API-ju (npr. Tvrtka + zapošljavanje: rok 31. 10. s oznakom "Rok uskoro").
- Ograničenje: API vraća najviše 20 preporuka poredanih po bodovima, pa "sljedeći rokovi" biraju samo iz njih. Mnoge preporuke nemaju rok, pa panel često pokaže 1-2 natječaja umjesto 3.
- Dodan skill `web-design-guidelines` (Vercel, MIT) u `.claude/skills/`, s lokalnom kopijom pravila (`guidelines.md`, commit `434b7f9`). Revizija weba po njemu, popravljeno: `...` u `…` u tekstovima učitavanja; "Obriši profil" sad nudi "Vrati profil" (undo umjesto potvrde); hijerarhija naslova na "Moji natječaji" (sažetak je h2 iznad h3 redaka); `name`, `autoComplete`, `spellCheck` i primjeri u placeholderima na poljima; eksplicitna boja na `<select>`; `theme-color` kao bijelo zaglavlje; `touch-action: manipulation` i boja tapa; `translate="no"` na nazivu aplikacije; `overflow-wrap: break-word` za duge kodove u naslovima.
- Namjerno nije primijenjeno: Title Case (hrvatski koristi sentence case), "izbjegavaj prvo lice" (glas "mi" je prirodan u hrvatskom), upozorenje o nespremljenim promjenama (obrazac ima 3 pitanja), filteri u URL-u (lista s filterima još ne postoji), preload fontova (Google Fonts CSS već koristi `display=swap`).
- Rad prebačen na Ninovu novu strukturu (`547a857`, komponente u mapama, `useGetProfileMatches`, domene u `api/`, `hooks/`, `types/`, `utils/formatting/`): nove datoteke su `components/{DeadlineDate,FundingCallList,FundingCallRow}/`, `api/{meta,fundingCalls}/`, `hooks/{meta,fundingCalls}/useGet*.ts`, `types/meta/`, `types/api/Page.type.ts`, `types/fundingCalls/FundingCallFilter.type.ts`, `utils/formatting/pluralize.ts`. Branch `design/ui-polish` na `refactor-ui`, PR ide u `refactor-ui` (PR #18).

## 2026-10-30 (Ivan, popravci nakon recenzije PR-a #19)
- Branch `fix/ui-polish-review` na `design/ui-polish`, PR ide u `design/ui-polish`.
- API: `ExpiredFundingCallCloser` prebacuje OPEN natječaje s rokom u prošlosti u CLOSED (pri pokretanju nakon seeda, nakon svakog ingesta i svaku noć u 00:05, `@EnableScheduling`). Zašto u bazi, a ne samo u filteru: tako se lista, facet statusa, broj na početnoj (`/api/meta`) i detalj slažu, a postojeći testovi s fiksnim datumima ne pucaju.
- Web: `DeadlineDate` ima stil za istekao rok (siv, precrtan dan), a hitan rok koji je pročitao AI zadržava narančasti okvir (crtkani). Obavijest "Vrati profil" nestaje nakon 10 s. Badge "Zatvoren" je neutralan. AI napomena u retku spominje samo prikazane vrijednosti (rok i/ili iznos). Maknut dvostruki naziv `aside` + `section`. Sažetak preporuka je `<p>` sa skrivenim kratkim `h2`. Maknut neiskorišteni kod za listu natječaja (`getFundingCalls`, `useGetFundingCalls`, `FundingCallFilter`, `Page`), vratit će se s pravom stranicom liste.

## 2026-11-02 (Ivan, paket 7: liste, detalji, zakoni, izvori, issue #9)
- Branch `feature/pkg-7-web-browse` iz `main`. Nije commitano.
- WEB-5 `/natjecaji`: pretraga (300 ms), status kao čipovi s brojevima, 5 filtera s brojevima iz facets, sortiranje, paginacija kao linkovi. Svi filtri u URL-u (`page` od 1, zadane vrijednosti se ne pišu), tipkanje zamjenjuje unos u povijesti. Na mobitelu filtri iza "Filtri (N)", pretraga ostaje vidljiva. Kad facets padne, filtri rade bez brojeva.
- WEB-6 `/natjecaji/:id`: činjenice (`FactList`, AI vrijednosti podcrtane crtkano), jedna obavijest o statusu (istekao rok / zatvoren / najavljen / nepotvrđen), sažetak uvjeta, dokumenti i prilozi samo kad postoje, sticky desni stupac (rok, iznos, "Otvori službenu objavu", izvor i datum dohvata). Povratak na listu s istim filtrima preko `history.state`. 404 i neispravan id daju poruku s linkom na listu.
- WEB-7 panel "Ispunjavam li uvjete?": skriven bez pitanja; da/ne, broj (hrvatski zapis "1.500,50"), izbor; "Ne znam" na svakom pitanju; profil se šalje uz odgovore. Rezultat: presuda riječima + ikona, razlozi s citatom, "Što još trebaš provjeriti", napomena i link uvijek. Odluka: rezervni odgovor bez modela (razlozi bez citata) nije označen kao AI, jer nije AI.
- WEB-8 `/zakoni` i `/zakoni/:id`: vrsta, razdoblje (`period=30d|90d|thisYear|lastYear`, URL drži razdoblje, ne datume), područje; novi propisi (30 dana) s tihim "novo" ispod datuma umjesto bedža (na prvoj stranici je skoro svaki redak nov). Detalj: činjenice, panel "Službeni tekst" (NN, PDF, ELI) prije AI sažetka.
- WEB-9 `/izvori`: tri kratka objašnjenja (API, čitanje stranica, snimka stanja) i tablica izvora (na mobitelu blokovi). Podnožje: "Stanje podataka na dan 26. 10. 2026." iz `/api/meta`, tiho nestaje kad meta padne.
- Nove zajedničke komponente: `ChoiceChip` (izdvojen iz profila), `SearchField`, `FilterPanel`/`FilterSelect`/`FilterGrid`/`toFilterOptions`, `Pagination`, `BackLink`, `FactList`, `AiBlock`, `ExternalButtonLink`; ikone `arrowLeft`, `chevronLeft/Right`, `filter`, `help`. Opisano u `DESIGN_SYSTEM.md`.
- Provjera: `yarn build` i `yarn lint` prolaze. Pravi API lokalno: lista, filtri u URL-u, reload, povratak s filtrima, prazno stanje, greška (API zaustavljen), 404, rijetki natječaj bez "null", 1280 i 375 px. WEB-7 s privremenim testnim pitanjima u lokalnoj bazi (vraćeno): pravi rezervni odgovor UNCLEAR; presude "ispunjavaš/ne ispunjavaš" i 429 samo lažnim odgovorom u pregledniku (nema ključa za model).
- Otvoreno: AI rezultati nisu u seedu (blocker u `work/tasks.md`), pa bez ključa nema panela ni sažetaka u demu.

## 2026-11-03 (Ivan, popravci nakon recenzije PR-a #22, Nino)
- API: broj uz županiju u facetu sad uključuje natječaje za cijelu Hrvatsku (isto kao filter), bez dvostrukog brojanja natječaja koji imaju i "ALL" i županiju. "ALL" broji svaki natječaj jednom (prije je natječaj s dva zapisa cijele Hrvatske brojao dvaput). Web dodaje županije bez vlastitih natječaja s brojem za cijelu Hrvatsku, a vrijednosti iz faceta i URL-a svodi na kratko ime, pa se županija ne pojavljuje dvaput.
- Web: `?page=99` daje "Stranica ne postoji" (`PageNotFoundState`) s linkom na prvu stranicu, i kod zakona. Rečenica sa sažetkom ostaje uz stari filtar dok se novi rezultati učitavaju (`useSettledValue`). Ikona u "Prilozima" je opet u istom redu. Izlazak iz polja pretrage odmah predaje tekst, pa "Poništi" unutar 300 ms više ne vraća pretragu. "Ne znam" više ne dijeli key s ponuđenim opcijama.
- Konvencije: `*FilterPatch`, `RegulationPeriod`, `RegulationFacets` u zasebnim `types/` datotekama, `*_PAGE_SIZE` u `constants/`, `useCurrentPath` i `returnState` odvojeni od `useReturnPath`.
- Provjera: `yarn build`, `yarn lint`, `mvnw test-compile` prolaze. `FundingCallServiceTest` nije pokrenut (Docker nije radio), preglednik nije provjeren s pravim API-jem.
- Nije u ovom PR-u (napomene iz recenzije): ton "vi" u backend poruci, presude VUS-a na prvoj stranici zakona, WEB-7 s pravim ključem.

## 2026-11-03 (Nino, AI follow-up iz recenzije PR-a #13, issue #7)
- Riješene sve 4 točke recenzije (odluke u `work/decisions.md`, 3.11.):
  1. **AI rezultati u seedu.** Nove admin rute `GET /api/admin/seed/funding-calls|regulations` izvoze bazu u seed format; seed proširen s `eligibilityQuestions`, `deadlineNote`, `fieldsReviewed`, `hidden`, `hiddenReason` i `Regulation.summary`. Upute u `README.md` ("AI rezultati i osvježavanje seeda").
  2. **Istekao rok:** provjera ne zove model i vraća `LIKELY_NOT_ELIGIBLE` s razlogom o roku; pitanja se ne generiraju za zatvorene/istekle pozive, a GET pitanja za njih vraća praznu listu.
  3. **Ručno provjereni pozivi:** AI-2 im mijenja samo sažetak uvjeta i dokumente.
  4. **Hrvatske poruke:** `NotFoundException`/`InvalidRequestException` imaju `userMessage`; exception poruke ostaju engleske, handler šalje hrvatsku.
- Batchevi pokrenuti s ključem (Haiku): AI-2 110 poziva (0,092 USD, 0 grešaka), AI-4 pitanja 87 poziva (67 s pitanjima), AI-3 60 propisa (0,057 USD). Ukupno u `llm_usage` 0,28 USD od budžeta 3 USD. Kontrolni slučaj FZOEU 275: 9.000 EUR, rok null.
- Seed (`resources/seed/*.json`) prepisan izvozom: 693 natječaja (113 s AI poljima, 67 s pitanjima), 3.860 propisa (62 sa sažetkom). Provjereno na praznoj bazi bez `ANTHROPIC_API_KEY`: AI polja, pitanja, sažeci i rezervna provjera (UNCLEAR + napomena) rade.
- `mvnw test`: 322 testa prolaze (novi: istekao rok, hrvatske poruke, pregledani poziv u mergeru, seed round-trip).
- Otvoreno: 54 otvorena poziva s udruge.gov.hr nemaju AI polja jer u seedu imaju < 300 znakova teksta (POC je spremio samo isječak s popisa). Rješenje: ingest `ASSOCIATIONS_OFFICE` pa ponovno AI-2 + izvoz seeda. Postojeće lokalne baze ne dobivaju novi seed bez `docker compose down -v`.
- Napomena: lokalni `datathon-postgres` je usred batcha netko zaustavio (`docker stop`); ponovno pokrenut, podaci nisu izgubljeni.
- Nije commitano.

## 2026-11-03 (Ivan, lokalno pokretanje za test WEB-7 nakon Ninovog PR-a #23)
- Na Ivanovom Macu (macOS 14) nema Dockera, JDK 25 ni Nodea 22, a Homebrew sve kompajlira iz izvora (sporo). Zato su u `~/tools` gotovi binarni paketi: Temurin JDK 25, Node 22, Postgres 17 (EDB, `pg_ctl` na portu 5433, baza/korisnik/lozinka `datathon`). `mvnw` nema execute bit, pa se pokreće sa `sh ./mvnw`.
- Svježa baza iz novog seeda: 693 natječaja, 3.860 propisa; 33 otvorena natječaja s rokom u budućnosti imaju pitanja (npr. id 61, 78, 255).
- WEB-7 radi kraj-do-kraja bez ključa: panel se prikazuje, provjera vraća rezervni UNCLEAR s napomenom i linkom. `yarn lint` i `yarn build` prolaze. Prave presude s citatima i dalje traže `ANTHROPIC_API_KEY`.
- Uočeno (backend/AI, nije web): pitanja i rezervne poruke su na "vi" ("Ugovorili ste…", "Odgovorite na sva pitanja"), a web na "ti"; rezervna poruka traži "sva pitanja" i kad su sva odgovorena.
- Isti dan, s pravim ključem (API pokrenut s `LLM_BUDGET_USD=0.50`, `LLM_MAX_ATTEMPTS=1`, bez batcheva): 4 provjere kroz web, ukupno 0,084 USD (Sonnet, oko 14k ulaznih tokena po provjeri). Natječaj 78 (obnova poljoprivrednog potencijala): svi "Da" daju "Vjerojatno ispunjavaš" sa 4 citata; "Ne" na uvjet od 30 % daje "Vjerojatno ne ispunjavaš" s citatom tog uvjeta. Natječaj 61 (osiguranje): u oba scenarija UNCLEAR s citatima, jer je tekst poziva na izvoru kratak. Za video koristiti 78, ne 61.
- Screenshotovi za #9 (33 snimke, WEB-5 do WEB-9, 1280 px i 375 px) u `work/screenshots/pkg-7/`, snimljeni Playwright skriptom. Stanja učitavanja i greške izazvana presretanjem zahtjeva u pregledniku (lažni 503 i 429), presude su pravi odgovori modela. Ukupna potrošnja na ključu u lokalnoj bazi: 0,255 USD za 9 provjera (5 ručnih, 4 iz skripte, od toga 1 nepotrebna jer je zadržani zahtjev pušten pri zatvaranju preglednika).
- Popravak na webu: obavijest o isteklom roku imala je dvije točke ("Rok je bio 08. 02. 2021.."), jer `formatDate` već završava točkom.

## 2026-11-03/04 (Luka, #11 nice-to-have, branch `feature/nice-11`)
- Novi izvori:
  - eKohezija (svi EU pozivi 2021.-2027., javni JSON API);
  - 18 ministarstava (Vladin CMS, među njima i Datathon MPUDT-a);
  - 32 grada i županije (WordPress REST, Joomla RSS, DNN, HTML liste, Zagreb);
  - agencije (HRZZ, AMPEU, ribarstvo);
  - sveučilišta, studentski zborovi i centri.
  - Pregled izvora u `work/sources-research.md`, odluke u `work/decisions.md` (3.11.).
- Pravna osnova: navodi Narodnih novina iz teksta poziva, veza s propisom (91 % navoda s nazivom upareno) i ciljani uvoz starijih citiranih propisa.
- Export u predloženom standardu s JSON Schemom, Atom feed umjesto mail obavijesti, oznaka „novo”, brojanje novih i promijenjenih po izvoru, dnevni ingest.
- Registar udruga: profil udruge iz OIB-a.
- Bug: Hibernate CHECK constrainti na enum stupcima rušili su ingest na postojećim bazama kod svake nove vrijednosti izvora. Sad se brišu pri startu.
- Seed osvježen preko zasebne baze `datathon_seed` (radna lokalna baza nije imala AI rezultate) i provjeren na praznoj bazi bez ključeva. AI trošak: 0,54 USD (Haiku).
- `ANTHROPIC_API_KEY` je u `apps/api/.env` (nije u gitu). Ključ je bio zalijepljen u chat, pa ga treba rotirati.
- Testovi: 378 API testova, `yarn lint` i `yarn build` prolaze.
