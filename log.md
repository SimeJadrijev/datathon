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
