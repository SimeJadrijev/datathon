# Izvori podataka

Popis svih skupova podataka i izvora koje OtvoreniRadar koristi (obvezni dio predaje, Pravila čl. VIII). Licence su provjerene 8. 11. 2026. Gdje licencu nismo mogli potvrditi, piše **PROVJERITI**.

- **Način dohvata:** API = strojno čitljivo sučelje; RSS = feed; HTML = čitanje javne web stranice (poštujemo `robots.txt`, najviše 1 zahtjev u sekundi, izvore koji blokiraju AI crawlere preskačemo).
- **Datum preuzimanja:** podaci se dohvaćaju uživo (ingest, dnevno u 5:30). Snimka u repozitoriju (`apps/api/src/main/resources/seed/`) je od **4. 11. 2026.**
- Uz svaki natječaj i propis aplikacija prikazuje izvor i poveznicu na izvornu objavu.

## 1. Portal otvorenih podataka (data.gov.hr)

| Skup podataka | Izdavatelj | Poveznica | Način | Licenca |
|---|---|---|---|---|
| Registar udruga Republike Hrvatske | Ministarstvo pravosuđa, uprave i digitalne transformacije | https://data.gov.hr/ckan/dataset/registar-udruga | API (CKAN resurs, JSON) | Otvorena dozvola RH (https://data.gov.hr/id/licence/otvorena-dozvola-rh) |
| Sudski registar | Ministarstvo pravosuđa, uprave i digitalne transformacije | https://data.gov.hr/ckan/dataset/sudski-registar, API: https://sudreg-data.gov.hr | API (OAuth2, profil tvrtke iz OIB-a) | Otvorena dozvola RH. Uvjeti korištenja API-ja (registracija ključa): **PROVJERITI** |

## 2. API-ji i feedovi tijela javne vlasti

| Izvor | Izdavatelj | Poveznica | Način | Licenca |
|---|---|---|---|---|
| eKohezija, pozivi EU fondova 2021.-2027. | Ministarstvo regionalnoga razvoja i fondova EU | https://ekohezija.gov.hr/MISPublicApi/poziv/browse/ (javno: https://eufondovi.gov.hr) | API (JSON) | Nije izričito navedena, **PROVJERITI** |
| Narodne novine, službeni dio (propisi, ELI) | Narodne novine d.d. | https://narodne-novine.nn.hr, ELI: `/eli/sluzbeni/{godina}/{broj}` | HTML + ELI metapodaci | Službeni tekstovi propisa nisu predmet autorskog prava (Zakon o autorskom pravu i srodnim pravima). Uvjeti portala: **PROVJERITI**. Oglasni dio ne koristimo (zabranjen u `robots.txt`). |
| Mjere HZZ-a | Hrvatski zavod za zapošljavanje | https://mjere.hzz.hr (WordPress REST API) | API (JSON) | **PROVJERITI** |

## 3. Javne web stranice tijela javne vlasti (natječaji i javni pozivi)

Za sve izvore u ovom odjeljku licenca nije izričito navedena na stranici. Oslanjamo se na pravo na ponovnu uporabu informacija tijela javne vlasti (Zakon o pravu na pristup informacijama, NN 25/13, 85/15, 69/22). Preuzimamo samo javno objavljene natječaje, navodimo izvor i vodimo na izvornu objavu. Status: **PROVJERITI**.

| Izvor | Izdavatelj | Poveznica |
|---|---|---|
| Natječaji za udruge | Ured za udruge Vlade RH | https://udruge.gov.hr |
| NPOO pozivi | Vlada RH (Fondovi EU) | https://fondovieu.gov.hr |
| Javni pozivi i natječaji | Fond za zaštitu okoliša i energetsku učinkovitost | https://www.fzoeu.hr |
| Natječaji | Agencija za plaćanja u poljoprivredi, ribarstvu i ruralnom razvoju | https://www.apprrr.hr |
| Natječaji | Ministarstvo znanosti, obrazovanja i mladih | https://mzom.gov.hr |
| Vijesti o natječajima (18 tijela) | MPUDT, MFIN, MVEP, MUP, MINGO, MINT, MROSP, MZOZT, MMPI, MPGI, MRRFEU, Min. kulture i medija, Min. zdravstva, Min. hrvatskih branitelja, Min. demografije i useljeništva, Min. poljoprivrede, šumarstva i ribarstva, SDU za Hrvate izvan RH, Ured za ljudska prava | `*.gov.hr`, popis u `apps/api/.../ingest/source/GovHrNewsFetcher.java` |
| Agencije | Hrvatska zaklada za znanost, Agencija za mobilnost i programe EU, EU ribarstvo (MPŠR) | https://hrzz.hr, https://www.ampeu.hr, https://euribarstvo.hr |
| Gradovi i županije (36) | Grad Zagreb, Split, Slavonski Brod, Vukovar, Bjelovar, Čakovec, Karlovac, Kaštela, Koprivnica, Osijek, Poreč, Rovinj, Sinj, Sisak, Solin, Virovitica, Zaprešić, Zadar, Dubrovnik, Velika Gorica; Splitsko-dalmatinska, Šibensko-kninska, Koprivničko-križevačka, Osječko-baranjska, Požeško-slavonska, Dubrovačko-neretvanska, Krapinsko-zagorska, Ličko-senjska, Primorsko-goranska, Istarska, Zadarska, Brodsko-posavska, Bjelovarsko-bilogorska, Sisačko-moslavačka, Vukovarsko-srijemska, Virovitičko-podravska županija | popis u `LocalWordPressFetcher`, `LocalHtmlListFetcher`, `LocalJoomlaFeedFetcher`, `LocalEasyDnnFetcher`, `ZagrebFetcher` |
| Sveučilišta i studentski centri | Sveučilišta u Zagrebu, Splitu, Rijeci, Osijeku, Zadru, Puli, Dubrovniku i Slavonskom Brodu, Sveučilište Sjever, Hrvatsko katoličko sveučilište, Tehničko veleučilište u Zagrebu; studentski centri (Zagreb, Rijeka, Split, SKC Rijeka) i studentski zborovi (Zagreb, Zadar, Rijeka, Pula) | popis u `UniversityFetcher.java` |

## 4. Namjerno izostavljeni izvori

Samobor, Međimurska županija, Pula i HAMAG-BICRO: `robots.txt` blokira AI crawlere ili traži `ai-train=no`, a mi tekst šaljemo modelu.

## 5. Obrada umjetnom inteligencijom

Tekstovi natječaja i propisa šalju se Claude API-ju (Anthropic) radi izvlačenja polja, sažetaka i pitanja o uvjetima. Rezultati se spremaju u bazu i u aplikaciji su označeni kao „AI podaci”, odvojeno od službenih podataka izvora.
