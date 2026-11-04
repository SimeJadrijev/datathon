# Datathon — tim i asistent

Tim: 4 osobe (korisnik + 3 prijatelja). Cilj: **pobijediti na natjecanju**. Jezik komunikacije: hrvatski.

## Uloga asistenta
- Djeluj kao iskusni tech poduzetnik + iskusni softver inženjer, ne kao pomoćnik koji samo potvrđuje.
- UVIJEK kritički sagledaj svaku ideju i prijedlog (naše i svoje): rizici, slabosti, što žiri vidi, alternative. Reci otvoreno ako je ideja loša ili preambiciozna.
- Daj konkretne tehničke savjete i preporuke, ne popise opcija. Optimiziraj za kriterije ocjenjivanja i ograničeno vrijeme, ne za savršenstvo.
- Na jednostavna pitanja odgovaraj kratko i jasno, bez objašnjavanja nepotrebnih detalja (korisnik tako lakše čita). Dulje i detaljnije samo kad je tema složena ili korisnik to traži.

## Pravila vođenja memorije (KRITIČNO)
- Kontekst se često puni, pa je istina u fileovima, ne u razgovoru.
- Kad korisnik da nešto bitno (pravila, rokovi, odluke, zadaci, podaci, ideje), ODMAH to zapiši u odgovarajući md file, prije nastavka razgovora.
- Fileovi su modularni i kratki. Jedna tema po fileu. Ako file naraste, razdvoji ga.
- Ovaj CLAUDE.md drži samo sažetak stanja + index. Detalji idu u module.
- Nakon značajne sesije dodaj zapis u `log.md`. Na početku nove sesije pročitaj "Trenutno stanje" ispod i po potrebi relevantne module.
- Strukturu slobodno mijenjaj ako treba, ali ažuriraj index ispod.
- Odluke zapisuj s obrazloženjem (zašto), da ih ne ponovno raspravljamo.

## Trenutno stanje
- Natjecanje: Datathon Portala otvorenih podataka 2026 (MPUDT, otvoreni podaci javnog sektora, 3 izazova, online).
- **PRIJAVA PREDANA, prijavljeni smo na izazove A i C. Izazov B odbačen. HITNO: rješenje + video (do 3 min) mailom na open.data@mpudt.hr do 9.11.2026. 24:00. Cilj: zaključati ideju oko 27.10.**
- Faza: odabir ideje za izazov A i/ili C. Pravila i Javni poziv PROČITANI (PDF-ovi u `context/izvori/`), sažetak u `context/competition.md`. Sime želi zanimljivu AI/data science temu iskoristivu za diplomski (želja, NIJE obavezno pravilo).
- Ocjenjivanje: max 60 b. Najviše nose Izvedivost (10), UI/UX (10), video (10). Ostalih 6 kriterija po 5. Nagrada se ne dodjeljuje ispod 50 b.
- Radni prototip + API podaci + više izvora. Zasebno rješenje po izazovu je obvezno, pa su A i C dva odvojena projekta (asistent savjetuje jedan glavni, drugi samo ako prvi stoji do ~1.11.). Aktivne ideje u `work/ideas.md`: 13-16 (moje) i 19-28 (Gemini, korisniku draže, s mojom ocjenom). Korisnik preferira Geminijeve (konkretan problem + "wow" demo). Najbolji kandidati: 27 DoktorGdje i 25 KrizniŠtab OS, oba uvjetno ovisno o podacima (testovi u `work/tasks.md`).
- **26.10.: ODLUČENO graditi agregator natječaja + zakona (izazov C), osnova je branch `poc/natjecaji-scraper`.** MVP prvo, plan M0-M8 u `work/tasks.md`, odluke i recenzija POC-a u `work/decisions.md`. Bez logina, Claude API s cacheom, Java backend (prepis scrapera u Javu za 6 izvora čeka potvrdu). A je rezerva. Starije ideje (13-28) više nisu aktivne.
- Otvoreno: smije li se kod javno na GitHub (sigurnosna politika), vidi `work/decisions.md`.
- 29.10.: redizajn weba (OtvoreniRadar, zelena paleta) na branchu `refactor-ui` (PR #18, Nino), pravila u `DESIGN_SYSTEM.md`. Prijedlog "manje AI izgleda" (serif naslovi + IBM Plex Sans, lista umjesto kartica, pravi brojevi na početnoj) na branchu `design/ui-polish` (na vrhu `refactor-ui`, PR u `refactor-ui`), uz skill `web-design-guidelines` i popravke po njemu.
- 2.11.: paket 7 (issue #9, liste, detalji, zakoni, izvori, WEB-5 do 9) gotov na branchu `feature/pkg-7-web-browse`, PR prema `main` otvoren 2.11. Otvoreno: screenshotovi za kriterije i provjera WEB-7 s pravim AI-4 podacima. Detalji u `log.md`.
- 3.11.: AI follow-up (issue #7) mergean (PR #23): seed sad nosi AI rezultate (demo radi bez ključa), istekao rok, ručno provjereni pozivi, hrvatske poruke. Detalji u `log.md`.
- 3.11.: paket 7 gotov: WEB-7 provjeren s pravim AI-jem (natječaj 78 za video), screenshotovi u `work/screenshots/pkg-7/`, PR #24 zatvara issue #9.
- 3./4.11.: #11 (Luka) na branchu `feature/nice-11`: novi izvori (EU fondovi, ministarstva, gradovi i županije, sveučilišta), pravna osnova, export u standardu, RSS, Registar udruga, osvježen seed. Detalji u `log.md`.
- 4.11.: lov na bugove na branchu `fix/bug-hunt` (Luka): ispravci dohvata (status, rokovi, iznosi, šum), duplikati, preporuke, web; seed 4.11. Ne pokretati puni ingest nad seedom za stare izvore (vidi `work/decisions.md` 4.11.). Detalji u `log.md`.
- 4.11.: rokovi natječaja na branchu `fix/deadlines` (Luka): strukturirani rok izvora pobjeđuje AI, praćenje linka s udruge.gov.hr, Claude čita skenirane PDF-ove, seed osvježen. Detalji u `log.md`.
- Zadnje ažurirano: 2026-11-04

## Tehnički stack i konvencije
- Monorepo: `apps/web` (React 19, TS, Vite, wouter, TanStack Query, axios), `apps/api` (Spring Boot 4, Java 25, JPA, Postgres u Dockeru). Pokretanje: vidi `README.md`.
- Web: apsolutni importi preko aliasa `@/` (= `src/`). Svaka mapa u `src/` ima barrel `index.ts` (novu mapu uvijek dodaj s njim). Tipovi u `types/*.type.ts`, jedna mapa po stranici u `pages/`.
- Rute su wouter (ne React Router). Providere (ErrorBoundary, QueryClient) držimo u `App.tsx`, rute u `Router.tsx`.
- Prije predaje: `yarn build` i `yarn lint` u `apps/web` moraju proći.
- **Dizajn (OBAVEZNO):** prije stvaranja ili mijenjanja bilo koje stranice, komponente ili stilova u `apps/web` pročitaj `DESIGN_SYSTEM.md` i drži se njegovih tokena, komponenti, obrazaca rasporeda i vizualnih načela. Prvo iskoristi postojeće komponente iz `@/components`, tek onda uvodi nove. Svako namjerno odstupanje zapiši i obrazloži u `DESIGN_SYSTEM.md`. Ako se design system promijeni, ažuriraj `DESIGN_SYSTEM.md` u istoj promjeni.
- Pravila pisanja koda: skill `spring-boot-api` (za `apps/api`) i `react-web` (za `apps/web`). Prije pisanja koda u tim mapama učitaj odgovarajući skill.

## Index
| File | Sadržaj |
|---|---|
| `context/competition.md` | pravila, format, kriteriji ocjenjivanja, nagrade, IP, AI alati |
| `context/izvori/` | originalni PDF-ovi: `javni-poziv.pdf`, `pravila.pdf` |
| `.claude/skills/` | `product-brainstorming` (generički, od korisnika), `jury-simulator` (ocjena ideja po rubrici 60 b), `data-scout` (istraživanje data.gov.hr CKAN API-ja), `spring-boot-api` i `react-web` (konvencije koda za `apps/`), `web-design-guidelines` (Vercel, UX i pristupačnost, revizija UI-ja) |
| `context/timeline.md` | rokovi i raspored |
| `context/data.md` | datasetovi, izvori, ograničenja |
| `team/team.md` | članovi, uloge, jake strane |
| `work/ideas.md` | ideje i pristupi (s kritičkom ocjenom) |
| `work/decisions.md` | odluke + obrazloženja |
| `work/tasks.md` | tko radi što, status |
| `work/mvp-task-specs.md` | 8 radnih paketa (40 koraka s ID-evima) na engleskom, jedan paket = jedan GitHub issue = jedan vlasnik |
| `work/sources-research.md` | istraženi izvori natječaja (dodani, kandidati, odbačeni s razlogom) |
| `work/api-plan.md` | plan implementacije paketa 4 (API-1 do API-7), nalazi iz koda, odluke |
| `log.md` | kronološki dnevnik |
| `README.md` | opis projekta i pokretanje |
| `DESIGN_SYSTEM.md` | jedini izvor istine za izgled weba: tokeni, tipografija, komponente, obrasci, stanja, pristupačnost, hrvatski tekst, checklista |

## Sigurnosna pravila organizacije
Vrijede pravila iz organizacijskog CLAUDE.md-a (npr. ništa se ne objavljuje na javne platforme, ne pristupa se cookiejima). Podatke natjecanja i kod ne dijelimo javno bez provjere.
