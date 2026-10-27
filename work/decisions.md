# Odluke

_Format: datum, odluka, zašto, alternative koje smo odbacili._

## 2026-10-22: Ispravak: prijavu ispunjava svaki član, ne samo mentor
- Ranija bilješka (iz pozivnog maila) govorila je da prijavu podnosi mentor. Pravila (VI) i Javni poziv (V) kažu: **svaki član tima ispunjava prijavni obrazac** i navodi naziv tima + kontakt mentora. Mentor je voditelj tima i komunicira s organizatorom.
- Posljedica: svi 4 člana moraju stići ispuniti obrazac do 23.10. 24:00, a mentor mora biti dogovoren da bi se njegovi podaci mogli upisati.

## 2026-10-22: Preporuka: prijaviti jedan izazov, ne sva tri (prijedlog, čeka potvrdu tima)
- Pravila zahtijevaju **zasebno rješenje po izazovu**. Za 4 osobe u ~17 dana to znači 3 plitka rješenja umjesto jednog dobrog.
- Izvedivost, UI/UX i video nose 30 od 60 bodova. To se dobiva dubinom, ne širinom.
- Napomena: prijava je besplatna. Pravila NE kažu obvezuje li prijava na predaju rješenja (nepotvrđeno), pa bi "prijava u rezervi" za drugi izazov trebala potvrdu organizatora (open.data@mpudt.hr) prije oslanjanja na to.
- Odbačeno: prijava na sva tri izazova s ciljem tri nagrade. Realno se ne stigne.

## 2026-10-22: Bodovni profil određuje strategiju
- Max 60 b. Pragovi: nagrada se ne mora dodijeliti ispod **50 b**, a izjednačenje se rješava po "Izvedivost", pa "Upotreba otvorenih podataka".
- Težište: radni prototip (10), UI/UX (10), video (10). Ostalih šest kriterija po 5.
- Podaci izravno preko **API-ja** (kriterij "Upotreba otvorenih podataka") i **povezivanje više izvora** (kriterij "Povezivanje") jeftini su bodovi ako se planiraju od početka.
- Razrađeni koncept je dopušten, ali bez prototipa gubimo većinu od 10 b za izvedivost.

## 2026-10-22: Izbor izazova: PREPORUKA nakon scouta (čeka odluku tima, rok 23.10.)
Podaci scouta (vidi `context/data.md`) govore:
- **C je slab SAMO ako se gradi "dashboard javne nabave".** Najviše ljudi će posegnuti za EOJN + registrom ugovora, izvoz je odsječen na 20 000 redaka, ključni izvori (RGFI, Sudreg) su zatvoreni. _Ispravak (kasnije istog dana): izazov C sam po sebi nije loš, vidi sljedeći zapis._
- **B ima najjaču priču za kriterij "Povezivanje" i "Inovativnost":** portal daje statične slojeve, a živo dolazi izvana, pa je spoj prirodan i AI/model ima smisla. Slabost: podaci za zrak su tanki (jedan mjesec, 6 postaja, 2023.), plaže zastarjele. Rizik izvedivosti je stvaran.
- **A ima najbolju upotrebljivost za demo (karte, adrese, korisnik razumije u 10 s)**, ali geo podaci postoje samo za Zagreb i Rijeku, pa se "pokrivenost cijele RH" ne može tvrditi. Treba odlučiti je li "Zagreb-first s planom proširenja" prihvatljiv za Relevantnost/Utjecaj.
- **Preporuka: B ili A, ne C.** Ako tim želi jednu odluku sad: A, jer je izvedivost i UI/UX (20 b) lakše dobiti na Zagrebu nego dohvatiti žive podatke za B. Ovo je mišljenje, ne dokaz. Prije zaključavanja ideje (cilj ~27.10.) prvo brainstorm + `jury-simulator`.
- Za obrazac nije nužno znati ideju, samo izazov. Izazov se ne mijenja lako nakon prijave (nepotvrđeno, pitati organizatora).

## 2026-10-22: Revidirana preporuka: glavni kandidat je "Spojnik" (izazov C), A je rezervni (prijedlog, čeka tim)
- Nakon čitanja cijelih scout fileova: ranija preporuka "A ili B, ne C" pretpostavljala je da je C = dashboard javne nabave. Izazov C doslovno traži "povezivanje lokalnih, prostornih, gospodarskih i administrativnih podataka" i "smanjenje vremena i troška pronalaženja i obrade informacija", što pogađa meta-alat za otkrivanje i spajanje skupova (ideja 1 u `work/ideas.md`).
- Zašto: najmanja šansa da ga gradi drugi tim, scoutovi nalazi o kvaliteti portala su gotov dokaz problema, gotovo sve ide preko API-ja, jasan integracijski put prema portalu, a žiri je vlasnik portala.
- **B se odbacuje** za sada: nema živih okolišnih podataka na portalu, najbolji spoj (zrak) je tanak (jedan mjesec, 2023.).
- **A (Profil četvrti, Zagreb)** je rezerva: viši grubi zbroj bodova (47 vs 43) zbog UI/UX-a, ali najvjerojatnije ga gradi više timova i Zagreb-only je.
- Prijava u OBA izazova (C i A) ima smisla kao **osiguranje za pivot** ako test ideje 1 padne, ne kao dva proizvoda. Pravila traže zasebno rješenje po izazovu, a 2 videa + 2 dokumentacije + 2 UI/UX za 4 osobe u 17 dana nisu realni. Obvezuje li prijava na predaju i smije li se promijeniti izazov: nepotvrđeno, pitati open.data@mpudt.hr.
- Odluka o glavnoj ideji se zaključava tek nakon testa od 1 dan (profilirati ~100 resursa, vidjeti jesu li top spojevi zanimljivi ili trivijalni). Ako padne, pivot na A.
- Opcije za runtime AI su ograničene pravilom "softver otvorenog koda": preferirati open-weights/embeddings, LLM API samo opcionalno.

## 2026-10-23: Prijava predana na A i C, izazov B odbačen (odluka tima)
- Zašto B otpada: na portalu nema živih okolišnih podataka, najbolji spoj (zrak) je tanak. Odluka tima, ne samo asistenta.
- Prijavljeni smo na A i C, što su dva odvojena projekta (Pravila: zasebno rješenje, dokumentacija i video po izazovu). Obvezuje li prijava na predaju u oba, nepotvrđeno.
- Sime želi AI/data science temu iskoristivu za diplomski. Želja, ne pravilo: ne smije pokvariti kriterije ocjenjivanja (Izvedivost, UI/UX, video = 30 od 60 b).
- Savjet asistenta: jedan glavni projekt s cijelim timom, drugi samo ako prvi stoji do ~1.11. Razlog: 17 dana, 4 osobe, dva puta video + dokumentacija + UI/UX.
- Raniju preporuku "Spojnik kao glavni kandidat" (ideja 1) zamjenjuje treći krug ideja (13-18): korisnik nije razumio Spojnik i traži konkretnije probleme s AI/DS dubinom.

## 2026-10-22: Korištenje AI alata je dopušteno
- Pravila VIII dopuštaju AI alate ako ne krše licence i ako se **navedu u dokumentaciji**. Rješenje mora ostati "izvorno djelo tima". Zato u dokumentaciju ide odjeljak "Korišteni alati i biblioteke" uključujući AI.

## 2026-10-22: Predaja koda na javni repozitorij: OTVORENO, čeka sigurnosnu provjeru
- Pravila traže "izvorni kod ili poveznicu na javni repozitorij", a objava pod OSS licencom se boduje. Organizacijska sigurnosna politika zabranjuje objavu sadržaja na javne platforme.
- Do jasnog odgovora kod se **ne objavljuje javno**. Opcije: (a) pitati security/AppSec smije li se osobni projekt za natjecanje objaviti, (b) predati kod kao arhivu ili link za preuzimanje (Pravila to dopuštaju za prevelike privitke), ali link mora trajati do objave rezultata i mora biti na odobrenom mjestu (npr. SharePoint).
- Odluka ovisi o korisniku jer je pitanje politike organizacije, ne tehničko.

## 2026-10-26: Pregled POC-a natjecaji-scraper, odluke za MVP (korisnik)
- Branch `poc/natjecaji-scraper` (Python, SQLite, 11 scrapera, NN zakoni, match.py, mini web) je osnova. Pregled i review u razgovoru, plan u `work/tasks.md`.
- **Izazov: samo C** za ovaj projekt (natječaji i zakoni). A ostaje rezerva po ranijoj odluci.
- **Backend: Spring Boot (Java) je fiksan.** Korisnik: Python ingest ostaje samo ako je bolji od Java varijante, inače sve u jednom stacku. Asistent preporučuje **prepis u Javu samo za 6 MVP izvora + NN** (Jsoup, PDFBox, Anthropic Java SDK). Zašto: scraperi su mali (50-90 linija po izvoru), jedan stack = jedan Docker, jedna shema i manje mjesta za pad u demu. Python POC ostaje kao referenca: Java scraper se provjerava usporedbom broja dohvaćenih zapisa i polja s POC-ovim SQLite-om. Za razvoj frontenda odmah seed u Postgres iz POC SQLite-a. **POTVRĐENO (korisnik, 26.10.): prepis u Javu.** GitHub issue-e otvara korisnik, asistent piše specifikacije u `work/mvp-task-specs.md`.
- **LLM u runtimeu: Claude API s cacheom** (korisnik). Rezultati (sažetci, izvučena polja) se unaprijed izračunaju i spreme u bazu, pa demo radi bez ključa. Claude/Claude Code se navodi u dokumentaciji kao AI alat (Pravila VIII). Rizik: pravilo "softver otvorenog koda" se može čitati strogo, pa pitati organizatora zajedno s ostalim pitanjima.
- **Sudreg:** treba registracija za ključ (besplatna, ključ odmah). Za video treba pravi OIB, ne mock `00000000001`. Mock gubi bodove za živi API.
- **Izazov A ostaje rezerva**, ne radi se dok C ne stoji.
- **Podjela posla** još nije dogovorena (tim nije raspoređen).
- **Recenzija POC-a (subagent, 26.10.), ključni nalazi:** demo se ne može pokrenuti iz repoa (`data/` i `web/data/` u `.gitignore`, treba commitan snapshot s datumom); rok poznat za 33-41 %, status 55-69 %, NKD 2-5 % zapisa (lanac OIB→NKD→natječaj je gotovo prazan); LLM obradio samo 300 od 1485; ručna točnost nije mjerena; "zašto ti odgovara" su ručni bodovi, ne AI; obavijesti su simulacija po `datum_objave`; fondovieu zabranjuje `/download`, NN oglasni dio zabranjen; `.idea/` i `.claude/launch.json` su commitani u POC branchu.
- **Izbaciti iz MVP-a:** login, stvarni mailovi, "tko je dobio" (podaci završavaju 2016.), Zagreb, Rijeka, AMPEU, Osijek, HAMAG, eufondovi, NKD 2025 ako ne stignemo. Poveznica zakon↔natječaj je nice-to-have.
- **Zakoni su jednako važan dio** kao natječaji (ne samo drugi tab).
- **Bez računa/logina:** profil se unosi formom (ili OIB), bez registracije. Zašto: malo bodova u rubrici, puno posla.
- **Pristup: MVP prvo** (što treba za demo i video), nice-to-have poslije.

## 2026-10-24: Jezik skillova za kod i imena u kodu
- Skillovi `spring-boot-api` i `react-web` pisani su na **engleskom**: čisto su tehnički, učitavaju se pri svakom pisanju koda (hrvatski troši ~1,5-2x više tokena), a tehnički pojmovi su ionako engleski. Claude i dalje komunicira na hrvatskom (CLAUDE.md).
- `jury-simulator` i `data-scout` ostaju na hrvatskom (puni domenskih pojmova iz pravila i naziva skupova).
- ~~U kodu domenske imenice na hrvatskom~~ **ISPRAVAK (korisnik, isti dan): sav kod je na engleskom** (paketi, klase, metode, polja, tablice, JSON, URL-ovi). Hrvatski nazivi u `poc/` postoje samo zato što je to POC i ne prenose se. Domenski pojmovi se prevode po rječniku u skillu `spring-boot-api` (natječaj → `FundingCall`, propis → `Regulation`...). Na hrvatskom ostaju samo UI tekst i poruke grešaka koje API vraća korisniku.

## 2026-10-26: DATA-1 model podataka i ovisnosti (Luka, issue #3)
- **Ovisnosti dodane jednom, u DATA-1** (da kasniji koraci ne diraju `pom.xml`): Jsoup 1.23.2 (HTML parseri), PDFBox 3.0.8 (tekst iz PDF-ova poziva), springdoc-openapi 3.2.0 (izgrađen za Spring Boot 4.1.2, OpenAPI za kriterij Arhitektura), anthropic-java 3.3.0 (službeni Claude SDK). Alternativa bez ovisnosti (regex nad HTML-om, vlastiti PDF parser, ručni OpenAPI) je skuplja i krhkija.
- **`eligibilityQuestions` je TEXT sa sirovim JSON-om**, ne `@JdbcTypeCode(JSON)`. Zašto: Hibernate JSON mapiranje treba Jackson, a na classpathu su Jackson 3 (Boot 4) i Jackson 2 (Anthropic SDK). TEXT izbjegava taj rizik, a oblik pitanja ionako definira AI-4.
- **`SourceCode` je u paketu `source/`**, jer ga koriste `fundingcall`, `ingest` i budući `/api/sources` (API-5).
- `SourceRunLog` ima i `errorMessage` (nije bio u specifikaciji): INGEST-2 traži da se greška izvora vidi u logu izvora. `Regulation` ima `fetchedAt` za "podaci od" na stranici izvora.
- Test jedinstvenosti `source + externalId` radi nad Docker Postgresom (`@DataJpaTest` + `replace = NONE`), bez H2/Testcontainersa, pa testovi traže `docker compose up -d`.
- Lokalno: `mvnw` nema izvršni bit u gitu (tim radi na Windowsima), na Linuxu pokretati `sh mvnw ...`.

## 2026-10-27: Paket 4 (API), odluke za implementaciju (korisnik)
Detalji i obrazloženja u `work/api-plan.md`, odjeljak 3.
- `common/` iz `feature/pkg-5-ai` se preuzima doslovno i proširuje (izbjegava konflikt; handler dobiva 502, "polje: razlog", specifične hrvatske 404 poruke, ne logira odbijene vrijednosti jer bi to iscurilo OIB).
- N+1: `hibernate.default_batch_fetch_size`, ne `@EntityGraph` (kolekcije + paginacija = Hibernate paginira u memoriji).
- Natječaji bez statusa (49 u seedu): default lista samo `OPEN`, filter/facet vrijednost `UNKNOWN` za `status is null`.
- Županije: normalizacija u kodu (`CountyNames`), baza se ne dira.
- Pretraga `q` bez dijakritika: Postgres `unaccent` (init skripta u docker-compose + `CREATE EXTENSION IF NOT EXISTS` pri startu).
- `sort`: `deadline` (asc, prazni na kraju) ili `publishedAt` (desc), bez parametra smjera.
- Profil za matching: 400 ako nema `type` ili nema nijedne teme (područje, ključna riječ, NKD).
- Git: jedan branch i jedan PR za cijeli paket, commit po koraku; JSON primjeri u `work/api-examples.md`.
- OTVORENO: `SUDREG` u `SourceCode`, Sudreg fixturei u `main/resources`, `SudregClient` iz API-7 (preporuka: sve tri da).

## 2026-10-26: SETUP-1 Sudreg, demo OIB-ovi i nalazi o API-ju (Luka, issue #3)
- Ključ je registriran na Luku (aplikacija "Nazor", javni korisnik). Drži ga Luka u `apps/api/.env` (gitignored); ostali članovi ga dobivaju preko password managera, ne preko repoa ili chata. Prvih ~sat vremena nakon registracije API je vraćao 401 iako je token izdan, poslije je proradio sam.
- **Demo OIB-ovi** (javne tvrtke, fixturei u `apps/api/src/test/resources/fixtures/sudreg/`): Microblink d.o.o. 21173725829 (Zagreb, NKD 62.01, IT persona), Valamar Riviera d.d. 36201212847 (Poreč, Istarska, NKD 55.10, turizam), KRAŠ d.d. 94989605030 (Zagreb, NKD 10.82, proizvodnja).
- **`glavna_djelatnost` je često prazna** (Infobip, Podravka, Nanobit je nemaju). Posljedica za INGEST-9/API-7: lanac OIB → NKD → natječaji treba fallback (npr. pravni oblik → ciljna skupina, sjedište → županija) i UI mora reći kad NKD nije poznat.
- `/subjekti?tvrtka_naziv=` traži SQL LIKE uzorak: `KRAŠ%` radi brzo, `%KRAŠ%` zna isteći (timeout), bez `%` traži točan naziv. Odgovor nema naziv, samo OIB/MBS, pa naziv treba iz `detalji_subjekta`.
- `detalji_subjekta` za javne korisnike: najviše 6 zahtjeva u minuti (Upute v3.0.0). Token traje 6 h. Java klijent (INGEST-9) mora poštovati oboje i keširati odgovore.

## 2026-10-28: API-2, istekli rokovi i izvedba (korisnik)
- **Istekli rokovi u `status=OPEN`:** 10 od 141 vidljivih natječaja ima status OPEN, a rok u prošlosti (npr. id 99, rok 2023-06-13). API ih vraća kako jesu. Rješava se u podacima (QUALITY-1, točka 5), ne u API-ju. Zašto: specifikacija API-2 ne traži filtriranje po roku, a izvor ih još vodi kao otvorene. Alternativa za kasnije: `status=OPEN` isključuje istekle rokove.
- **`unaccent` se stvara pri startu aplikacije** (`db/extensions.sql` preko `spring.sql.init`), ne init skriptom u docker-compose. Zašto: init skripte rade samo na praznom volumenu, pa bi postojeće baze članova tima ostale bez ekstenzije. Ovako radi svima bez ručnog koraka.
- **`SourceInfo` i `SourceRef` su u `source/`**, ne u `fundingcall/dto/`, jer ih koristi i API-5. Lista vraća `source` kao `{code, name}`, detalj kao `{code, name, url}`.
