# Scout: Izazov A) Društvo i javne usluge

_Snimka 2026-10-22. Sve brojke dolaze iz stvarnih API odgovora ili preuzetih uzoraka (privremeni direktorij izvan repoa). Što nije provjereno, piše **nepotvrđeno**. Baza: CKAN `https://data.gov.hr/ckan/api/3/action/`._

## Glavni zaključci (čitaj ovo prvo)
1. **Geo podaci s koordinatama postoje praktički samo za Grad Zagreb (ArcGIS "Geoportal") i vozni red Rijeke.** Nacionalni skupovi (škole, vrtići) imaju samo adresu, mjesto, poštanski broj i županiju, bez koordinata.
2. **Dnevno svježi su registri, ne usluge.** Od 33 dnevna skupa za temu A relevantni su Registar kulturnih dobara, Registar umjetničkih organizacija i Registar udruga. Svi geoportal skupovi (Zagreb) i MZOM popisi nemaju upisanu frekvenciju osvježavanja (polje `data_refresh_frequency` prazno), a zadnje izmjene resursa su 2022.–2025.
3. **`datastore_active=true` ne znači upotrebljiv API.** Tri provjerena slučaja vraćaju pokvarene ili nepotpune podatke (vidi "Zamke").
4. Prava "živa" usluga za građane ide kroz **Autotrolej (Rijeka)**: vozni red je svjež (izravni URL), a API s pozicijama autobusa traži token (pristup nepotvrđen).

## Kandidat-skupovi (12)

### 1. Osnovne i srednje škole (MZOM)
- Izdavatelj: Ministarstvo znanosti, obrazovanja i mladih (izvor e-Matica). Poveznice: `data.gov.hr/ckan/dataset/osnovne-skole`, `.../srednje-skole`
- Resurs: CSV/JSON/XML. Osnovne: CSV 253 KB, srednje: CSV 132 KB. Zadnja izmjena resursa 2025-05-19 (osnovne), 2025-04-15 (srednje).
- Licenca: otvorena dozvola RH. Svježina: `data_refresh_frequency` prazno, nepoznata.
- Stupci: `Naziv, Adresa, Mjesto, Šifra (npr. 01-073-001), Telefon, ePošta, Osnivač, Ravnatelj, TipUstanove, PoštanskiBroj` (+ `Župnija`/`Županija`). **Nema koordinata.**
- Ključ: `Šifra` (jedinstvena šifra ustanove, prefiks 01 = Zagrebačka županija), `Mjesto`, `PoštanskiBroj`. Za prostorni spoj treba geokodiranje adresa (nepotvrđeno koliko adresa se uspješno geokodira).
- Dohvat: izravni download CSV/JSON. `datastore_search` radi, ali **zaglavlja su pokvarena** (vidi zamke).

### 2. Dječji vrtići (MZOM)
- Poveznica: `data.gov.hr/ckan/dataset/djecji-vrtici`. CSV 118 KB, zadnja izmjena resursa 2025-05-19. Licenca: otvorena dozvola RH. Svježina: prazno.
- Stupci: `Naziv, Adresa, Mjesto, ePosta, Telefon, Zupanija, PostanskiBroj`. Nema koordinata, nema kapaciteta.
- Ključ: `Mjesto`, `PostanskiBroj`, `Zupanija` (vrijednosti imaju razmak na kraju, npr. `"Osječko-baranjska županija "`).
- Dohvat: izravni download. `datastore_active=false` za CSV.

### 3. Geoportal Zagreb: osnovne škole
- Izdavatelj: Grad Zagreb. `data.gov.hr/ckan/dataset/geoportal-osnovne-skole`. GeoJSON preko ArcGIS-a: `https://opendata.arcgis.com/api/v3/datasets/08ff18612cea48a1b1bdb921a83974bc_0/downloads/data?format=geojson&spatialRefId=4326&where=1%3D1`
- **Provjereno: 177 objekata, svih 177 s točkom (WGS84).** Licenca: otvorena dozvola RH. Svježina: prazno, resurs `last_modified` 2022-07/2023-02.
- Bogati stupci: `smjenski_rad` (123 x "2 SMJENE", 54 x "1 SMJENA"), `produzeni_boravak` (121 IMA, 46 NEMA), `br_uc`, `br_raz`, `god_izgradnje`, `kuhinja`, `sport_dvor`, `sport_teren`, `strani_jezik`, **`GRAD_CETVRT` (177/177 popunjeno, 19 različitih vrijednosti)**.
- Ključ: koordinate, `GRAD_CETVRT`. Kvaliteta: uključuje i ustanove socijalne skrbi i "izvan obuhvata grada" (npr. Područna škola Stupnik).
- Dohvat: izravni GeoJSON (nije pravi API). **`curl` na Windowsu pada na provjeri certifikata (schannel), Python `urllib` radi.**

### 4. Geoportal Zagreb: dječji vrtići
- `data.gov.hr/ckan/dataset/geoportal-djecji-vrtici`. Licenca **CC BY** (potrebno navesti izvor). GeoJSON: `.../49bb07d4efc34bd5bacc1d330dc28a07_0/downloads/data?format=geojson...` (isti obrazac URL-a). Resurs `last_modified` 2022-06-23.
- **Provjereno: 320 objekata, svi s točkom.** Stupci: `naziv, adresa, grad_cetvr, vr_vrtica, tip_vrtica, kuhinja, dvorana, vdi, strani_jez`. Nema kapaciteta/upisa.
- Ključ: koordinate, `grad_cetvr`. **Zamka:** nazivi četvrti nisu isti formati kao kod škola (`GORNJI GRAD-MEDVEŠČAK` vs `GORNJI GRAD - MEDVEŠČAK`); bez normalizacije podudaraju se samo 10 naziva od 17 (vrtići) odnosno 19 (škole). Podudaranje nakon normalizacije nije provjereno do kraja (nepotvrđeno).

### 5. Geoportal Zagreb: stajališta ZET (autobus 1874, tramvaj 260)
- `data.gov.hr/ckan/dataset/geoportal-autobusna-stajalista-zet` i `...-tramvajska-stajalista-zet`. Licenca: otvorena dozvola RH. ArcGIS GeoJSON, resurs `last_modified` 2022–2023.
- **Provjereno: 1874 i 260 točaka.** Stupci: `Naziv_stajališta, Linije, Opis, Napomena`. Nema voznog reda ni dolazaka, samo lokacije i popis linija.
- Ključ: koordinate. GTFS/vozni red ZET-a na portalu nisam našao (nepotvrđeno da ne postoji).

### 6. Zagreb: domovi za starije (85), ljekarne (245), zdravstvene ustanove (47), zborna mjesta civilne zaštite (17)
- Domovi: `data.gov.hr/ckan/dataset/geoportal-domovi-za-starije-osobe`. GeoJSON na `services8.arcgis.com/.../FeatureServer/replicafilescache/...geojson`. **85 objekata s točkom**, stupci `vrsta, kapacitet, gradska_cetvrt, podruznica...`.
- Ljekarne: `geoportal-ljekarne`, 245 točaka, stupci `naziv, adresa, telefon, nadlezan` (nema radnog vremena ni dežurstava).
- Zdravstvene ustanove: `geoportal-zdravstvene-ustanove`, 47 točaka, bez kategorije.
- Zborna mjesta: `zborna-mjesta-civilne-zastite-grada-zagreba`, GeoJSON 3 KB, **samo 17 točaka** (`gradska_ce, zboriste`). Izmjena 2025-01-02. Nije jasno predstavlja li to sve lokacije (nepotvrđeno).
- Licenca: otvorena dozvola RH. Svježina: prazno.

### 7. Rijeka: Autotrolej vozni red i linije
- Izdavatelj: Grad Rijeka. `data.gov.hr/ckan/dataset/at-vozni-redovi` i `autobusne-linije`. Licenca **CC BY**.
- **Izravni URL je svjež:** `http://e-usluge2.rijeka.hr/OpenData/ATvoznired.csv` (HTTP `Last-Modified: 22 Oct 2026`). Provjereno: **36.842 redaka, 1062 stanice, 54 linije, 1918 polazaka, GPS (`GpsX`, `GpsY`) popunjen 100%**. Stupci: `StanicaId, LinVarId, Polazak, BrojLinije, Smjer, NazivVarijanteLinije, PodrucjePrometa, Naziv`. Separator `;`. Varijante za radni dan, subotu, nedjelju i "aktualni dan".
- **CKAN datastore kopija je zastarjela** (prvi zapis datiran 2024-12-11, ukupno 1750 redaka naspram 36.842 u izvoru). Koristiti samo izravni URL.
- Pozicije autobusa uživo: Swagger `https://api.autotrolej.hr/api/open/swagger/...` dohvaćen (9 GET ruta: `voznired/autobusi`, `polasci`, `stanice`...). **Rute traže `token` iz `token/login` (zaglavlja Username/Password). Vjerodajnice nemam, pristup nisam pokušavao: nepotvrđeno možemo li ga dobiti.**

### 8. Registar kulturnih dobara
- Izdavatelj: Ministarstvo kulture i medija. `data.gov.hr/ckan/dataset/kulturna-dobra`. **Svježina: dnevno** (resurs izmijenjen 2026-10-22). Licenca: otvorena dozvola RH.
- CSV 7,6 MB / JSON 10,2 MB / XML 16 MB. **Provjereno: 7950 zapisa.** Popunjenost: `Zupanija` 100%, `Opcina_grad` 99%, `Mjesto_smjestaja` 100%, **`Adresa_smjestaja` samo 27%**, nema koordinata. `Vrsta_kulturnog_dobra`: 5351 nepokretno-pojedinačno, 949 pokretno-pojedinačno, 751 zbirka, 535 kulturno-povijesna cjelina. `UNESCO_zastita`: 22 "da".
- Ključ: `Zupanija`, `Opcina_grad` (527 različitih), `Mjesto_smjestaja`. Za točke treba geokodiranje (27% adresa).
- Dohvat: izravni download (`datastore_active=false`).

### 9. Registar umjetničkih organizacija
- Izdavatelj: Ministarstvo kulture i medija. `data.gov.hr/ckan/dataset/registar-umjetnickih-organizacija`. **Dnevno** (izmijenjeno 2026-10-21). Licenca: otvorena dozvola RH.
- **JSON resurs ima 1177 zapisa; datastore kopija samo 250.** Stupci: `Grad, Adresa, Podrucje (glazbeno...), Zupanija, Naziv registra, Registarski broj, Datum upisa u registar`.
- Ključ: `Zupanija` (21 različita), `Grad`. Kvaliteta: kod 97% zapisa (1147/1177) `Grad` se podudara s `Opcina_grad` iz kulturnih dobara; ne podudara se npr. Pula, Rovinj, Umag, Bale, Fažana, Vodnjan, Oprtalj, Ližnjan (različiti oblici naziva) i jedan zapis s vrijednošću "karlovacka".
- Dohvat: izravni JSON (ne datastore).

### 10. Registar udruga RH
- Izdavatelj: MPUDT (`registri-npo-mpu.gov.hr`). `data.gov.hr/ckan/dataset/registar-udruga`. **Dnevno** (2026-10-21/22). Licenca: otvorena dozvola RH.
- Resursi: "CTS" CSV **103 MB**, "Djelatnosti" CSV 30 MB (`datastore_active=true`, **399.400 redaka**: `UDR_ID, DJELATNOSTI, PODRUCJA_DJELOVANJA`), "Osobe" CSV 13 MB.
- Provjereno na prvih 6 MB CTS CSV (71.322 redaka, **uzorak je nepotpun i može biti pristran**): `STATUS` AKTIVAN 43.186 / BRISAN 26.350 / PRESTANAK DJELOVANJA 1.786. `ZUPANIJA` popunjeno 71.321/71.322, `SJEDISTE` (slobodan tekst s adresom, npr. "Dunavski prilaz 2, Vukovar") 71.321. `CILJANE_SKUPINE` ima npr. OSOBE STARIJE ŽIVOTNE DOBI (2419), UMIROVLJENICI (2326), OSOBE S INVALIDITETOM (2164), DJECA U RIZIKU OD SOCIJALNE ISKLJUČENOSTI (1067), SPORTAŠI (10.068). `WEB_STRANICA` samo ~1500.
- Ključ: `ZUPANIJA`, adresa iz `SJEDISTE` (potrebno geokodiranje), `UDR_ID`, `OIB`.
- **GDPR:** resurs "Osobe" sadrži podatke o osobama. Koristiti samo podatke na razini organizacije (CTS/Djelatnosti), a "Osobe" izbjegavati.

### 11. e-Savjetovanja
- Izdavatelj: Ured za zakonodavstvo. `data.gov.hr/ckan/dataset/esavjetovanja`. **Mjesečno** (resurs 2026-10-10). Licenca: otvorena dozvola RH. CSV/JSON/XML, datastore radi (869 zapisa).
- Stupci: `id, nazivTijela, vrstaPropisa, nazivSavjetovanja, statusSavjetovanja, pocetakSavjetovanja, zavrsetakSavjetovanja`. **Nema teme/područja i nema linka na savjetovanje.** 59 različitih tijela.
- **Provjereno:** raspon početaka 2025-09-10 do 2026-09-10 (nema novijih iako je resurs izmijenjen 2026-10-10). Status: 778 "Izvješća objavljena", 88 "Zatvoren", 1 "Otvoren". Brojač savjetovanja s `zavrsetakSavjetovanja` >= 2026-10-22 je 21, što se ne slaže s 1 "Otvoren" (nepotvrđeno koji je točan).
- Ključ: `nazivTijela`, datumi. Nema prostornog ključa.

### 12. e-Građani: statistika
- Izdavatelj: MPUDT. `data.gov.hr/ckan/dataset/e-gradani-statistika`. **Mjesečno** (2026-10-06). Licenca: otvorena dozvola RH.
- Resursi (CSV/XLSX): broj korisnika, korisnici po županijama (21 županija + redovi "inozemstvo" i "ukupno"), korištenje e-usluga, korištenje vjerodajnica. Primjer: Grad Zagreb 543.067, Splitsko-dalmatinska 257.848, ukupno 2.384.925 korisnika.
- Ključ: naziv županije (21 od 21 podudaranje s Registrom udruga nakon normalizacije; redove "inozemstvo" i "ukupno" treba izbaciti). Granulacija samo županija.

### Ostalo (nije u detaljnom popisu)
- **Središnji registar državne imovine** (nekretnine/pokretnine/prava/financijska imovina): `continuous`, ima javni API (`https://srdi.gov.hr/api/docs`, izvoz `.../api/nekretnine/export?format=CSV|JSON`). **Sadržaj i veličinu nisam dohvaćao: nepotvrđeno.** Pripada više izazovu C.
- **DGU Adrese INSPIRE / Upravne jedinice INSPIRE:** tjedno, samo WMS i ATOM (nema JSON). Mogli bi poslužiti kao geokoder i granice općina. Sadržaj nepotvrđen.

## Vanjski izvori izvan portala
| Izvor | Provjera | Napomena |
|---|---|---|
| **DHMZ trenutno vrijeme** `https://vrijeme.hr/hrvatska_n.xml` | HTTP 200, XML, termin 22.10.2026 19:00 po gradovima | Licenca **nepotvrđena** (na naslovnici nisam našao tekst o uvjetima korištenja). Stranica upozorenja `meteo.hr/upozorenja/` vraća 403 (bot-blok). |
| **Eurostat API** (npr. `demo_pjan`, stanovništvo po dobi) | HTTP 200, JSON-stat, `updated 2026-09-25` | URL stranice s autorskim pravima daje 404. Uvjete ponovne uporabe treba potvrditi prije navođenja u dokumentaciji (**nepotvrđeno**). Razina NUTS/država, ne općine. |
| **Autotrolej API pozicija autobusa** | Swagger dostupan, rute traže token | Vidi blok 7: pristup i uvjeti nepotvrđeni. |
| OSM Overpass | HTTP 502 u testu | **Nepotvrđeno** (privremena greška ili preopterećen poslužitelj). |

## Neobični spojevi (provjereno na uzorku)

### S1. "Pristupačnost škola javnim prijevozom": Zagreb škole x ZET stajališta x ljekarne
- Ključ: koordinate (WGS84), udaljenost po haversineu. Pokrivenost: **177/177 škola i 2134 stajališta imaju točku (100%)**.
- Izmjereno: medijan najbližeg stajališta **157 m**, ≤150 m kod 85/177, ≤300 m kod 161/177, ≤500 m kod 174/177. Najdalje: Područna škola Stupnik 1936 m, OŠ Kreativan razvoj (Podsljeme) 629 m, OŠ Većeslava Holjevca 516 m. Najbliža ljekarna: medijan 247 m, ≤500 m kod 141/177.
- **Kritika:** gotovo sve škole su blizu stajališta, pa je "problem" mali i rezultat nije iznenađujući. Jedini skup bez varijance. Vrijednost moraju dodati stupci `smjenski_rad` (123 škole dvije smjene) i `produzeni_boravak`, ali bez voznog reda ZET-a ne znamo učestalost linija (nepotvrđeno postoji li GTFS). Samo Zagreb.

### S2. "Spremnost starijih za krizu": domovi za starije x zborna mjesta civilne zaštite
- Ključ: koordinate, `gradska_cetvrt`/`gradska_ce`. Pokrivenost: 85/85 domova s točkom.
- Izmjereno: medijan udaljenosti doma do najbližeg zbornog mjesta **1879 m**, max 9499 m, samo 22/85 domova unutar 1 km.
- **Kritika (važno):** zborna mjesta imaju samo 17 točaka, po jedna po gradskoj četvrti. Udaljenost vjerojatno odražava grubost skupa, a ne stvarni rizik. Dok ne potvrdimo je li skup potpun, ovo nije nalaz nego hipoteza. Tema (javne usluge u kriznim situacijama, starije osobe) izravno pogađa izazov A, ali podaci su preslabi.

### S3. "Karta kulturne praznine": nepokretna kulturna dobra x umjetničke organizacije po županiji
- Ključ: županija (21/21 se podudara nakon normalizacije dijakritika i crtica), općina/grad (97% zapisa umjetničkih organizacija se podudara). Oba izvora dnevno osvježena.
- Izmjereno (omjer nepokretnih dobara po umjetničkoj organizaciji, puni podaci): Šibensko-kninska **308 dobara / 2 organizacije**, Ličko-senjska 202/2, Požeško-slavonska 144/1, Virovitičko-podravska 71/0, Brodsko-posavska 92/2, naspram Grada Zagreba 602/799 i Međimurske 52/9.
- **Kritika:** broj registriranih organizacija nije kulturna ponuda (sjedište može biti u Zagrebu, a djelovati drugdje), pa je mjera gruba. Skup je najjači za "nešto što se vidi na karti", ali ne dokazuje prazninu. Uz dodatak udruga iz Registra udruga (ciljne skupine, djelatnost "kultura") bio bi puno uvjerljiviji.

### S4. "Tko pomaže starijima i osobama s invaliditetom u mojoj županiji": Registar udruga x e-Građani
- Ključ: `ZUPANIJA` (**21/21 se podudara** s e-Građanima, uz izbacivanje "inozemstvo" i "ukupno"), plus `SJEDISTE` (adresa) za geokodiranje.
- Izmjereno na nepotpunom uzorku od 6 MB (od 103 MB): 43.186 aktivnih udruga, **OSOBE STARIJE ŽIVOTNE DOBI 2419 i OSOBE S INVALIDITETOM 2164 udruge** kao ciljna skupina. Omjer udruga na 10.000 e-Građana u uzorku je 157–262, ali **uzorak nije reprezentativan, pa omjere ne treba koristiti**.
- **Kritika:** to je pretraživi imenik, a ne analiza. Vrijednost je u "proaktivnoj usluzi za građane" (preporuka najbliže udruge), ali ovisi o geokodiranju slobodnog teksta adrese i o tome što je udruga stvarno aktivna (status AKTIVAN ne znači aktivnost). Nema GDPR problema na razini udruga, ali OIB-ovi i web adrese osobnih podataka su rubni slučaj (nepotvrđeno).

## Zamke (stvarno nađene)
1. **MZOM `datastore_search` ima pokvarena zaglavlja:** za `osnovne-skole` imena polja su vrijednosti prvog retka podataka (npr. `"OSNOVNA SKOLA BOGUMILA TONIJA"`). Za srednje škole i vrtiće nisam testirao. Koristiti izravni CSV.
2. **Umjetničke organizacije:** datastore 250 od 1177 zapisa. Rijeka vozni red: datastore 1750 redaka (zastarjelo) naspram 36.842 u izvoru.
3. **`curl` na Windowsu ne može dohvatiti ArcGIS** (`CRYPT_E_NO_REVOCATION_CHECK`), Python `urllib` radi. `python -I` ne dodaje direktorij skripte u putanju, pa uvoz iz susjedne skripte ne radi.
4. **Polje `data_refresh_frequency` je prazno** za sve škole/vrtiće/Zagreb geoportal skupove. Svježinu treba provjeravati po resursu (`last_modified`) ili izravnim HTTP zaglavljem, ne po CKAN polju.
5. **Nazivi četvrti nisu ujednačeni** (razmaci oko crtice, "IZVAN OBUHVATA GRADA"). Spoj po nazivu traži normalizaciju.
6. **e-Savjetovanja:** raspon podataka završava 2026-09-10 iako je resurs osvježen 2026-10-10. Nema teme ni prostorne dimenzije.
7. **Zborna mjesta Zagreba imaju samo 17 točaka**, pa su svi zaključci o udaljenosti pretpostavke dok se ne potvrdi potpunost.
8. Većina "nacionalnih" skupova je bez koordinata. Ideja koja treba prostorni spoj na razini cijele države mora računati na geokodiranje adresa (točnost nepotvrđena).
9. Prikaz dijakritika u terminalu je pokvaren (znak �), no podaci su UTF-8 i ispravni.
