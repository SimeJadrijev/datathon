# Data scout: Izazov B) Okoliš i održivi prostor

_Izvedeno 2026-10-22. Samo GET upiti. Sve brojke dolaze iz stvarnih odgovora, osim oznaka **nepotvrđeno**. Uzorci su skinuti u privremeni direktorij izvan repoa._

## Glavni nalaz (pročitaj ovo prvo)

1. **Na portalu gotovo nema živih mjernih podataka za okoliš.** Od 33 dnevno i 18 tjedno osvježavanih skupova nijedan nije o zraku, vodi, moru, buci ni otpadu (to su registri stranaka, udruga, nekretnina i slično). `data_refresh_frequency` je u skupovima iz okoliša gotovo uvijek prazno. Skupovi o zraku, moru i hidrologiji na portalu su **popisi postaja, godišnja izvješća ili poveznice na vanjske servise**, ne tokovi mjerenja.
2. **Žive podatke treba uzeti izvan portala**, a data.gov.hr daje statični sloj (lokacije, granice, klasifikacije, povijest). To je zapravo dobra priča za kriterij "Povezivanje": portal + živi izvor.
3. **Većina skupova je lokalna (Zagreb, Rijeka).** Državnih skupova s mjerenjima ima malo i dostupni su kao WMS/WFS (lokacije), a ne kao tablice vrijednosti. Rješenje za cijelu Hrvatsku mora ili ići preko DHMZ/Open-Meteo/Copernicus ili ostati gradsko (Zagreb).
4. **DataStore API na portalu je nepouzdan.** `datastore_active=true` na resursu ne znači da je `datastore_search` dostupan. Pogodio sam oba slučaja (vidi blokove).

## Kandidat-skupovi

### 1. Kakvoća mora za kupanje u RH (IZOR)
- **Poveznica:** https://data.gov.hr/ckan/dataset/kakvoca-mora-u-republici-hrvatskoj-konacna-hr (ID `kakvoca-mora-u-republici-hrvatskoj-konacna-hr`)
- **Izdavatelj:** HAOP (u katalogu označen "NEAKTIVAN"), podatke objavljuje Institut za oceanografiju i ribarstvo (IOR/IZOR).
- **Resursi:** 6 CSV resursa u DataStoreu (po županijama, godina **2015**), WFS i WMS na `faust.izor.hr:8080/geoserver/plaze`.
- **Licenca:** nije navedena (`license_id` prazno). **Nepotvrđeno** smije li se koristiti; treba provjeriti uvjete izdavatelja.
- **Svježina:** katalog javlja izmjenu 2023-02-01. DataStore sadrži **ocjene za 2015.**, ne novije. Izvorni CSV na `baltazar.izor.hr/plazepub/daj_csv?...` vraća **404** za sve godine koje sam probao (2015, 2022, 2024-2026).
- **Ključ za spajanje:** koordinate (`Zem. sirina`, `Zem. duzina`), `Grad/Mjesto`, `Podrucje`. WFS ima `PLAZA_ID` (EPSG:3765, ne WGS84).
- **Provjereno na uzorku:** DataStore vratio **106 plaža, sve s koordinatama**. Ocjene 2015: izvrsno 97, dobro 6, zadovoljavajuće 3 (nema "loše", pa je varijacija mala). WFS `plaze_spat` ima **49 točaka** (`numberMatched=49`), znatno manje od 106, pa **ne pokrivaju iste plaže**.
- **Kvaliteta:** dijakritici izbačeni ("izvrsno", "zadovoljavajue"), stari podaci, rascjep između DataStorea (106) i WFS-a (49). WFS ne podržava `outputFormat=application/json`, radi samo GML 3.2.
- **Način dohvata:** DataStore (radi, ali stari podaci) ili WFS GML. **Ne** pravi live API.

### 2. Geoportal Kvaliteta zraka (Grad Zagreb) + mjerenja po postajama 2023.
- **Poveznice:** `geoportal-kvaliteta-zraka` (postaje) i `podaci-o-kvaliteti-zraka-u-gradu-zagrebu-<postaja>-2023` (Ksaverska, Đorđićeva, Siget, Peščenica, Susedgrad; ukupno 11 pogodaka za upit).
- **Izdavatelj:** Grad Zagreb. **Licenca:** otvorena dozvola RH ili CC-BY (ovisno o skupu).
- **Resursi postaja:** GeoJSON/CSV/KML/SHP preko ArcGIS Hub-a (`opendata.arcgis.com`). **6 postaja** s koordinatama, tipom (prometna/pozadinska/industrijska) i popisom mjerenih tvari.
- **Mjerenja:** XLSX, **po mjesecima** (Ksaverska 2023 = 8 resursa), dnevne vrijednosti SO2, NO2, PM10, ozon, PM2,5, benzen, CO, metali i dr.
- **Svježina:** samo **2023.** Nije pronađena novija godina u naslovima. Nema tekućih podataka.
- **Ključ za spajanje:** datum (dnevno) + koordinate postaje.
- **Provjereno na uzorku:** Ksaverska siječanj 2023: 30 dana s PM10 (srednja 17,7 µg/m3). Vidi spoj 2.
- **Kvaliteta:** XLSX sa dva lista (grafikon + numerike), podaci nisu u DataStoreu (`datastore_search` vraća "Not Found" iako katalog javlja `datastore_active=True`), PM2,5 i benzen su mjestimice `None`. **Nepotvrđeno:** je li 2024/2025 objavljeno pod drugim nazivom.
- **Način dohvata:** izravan download XLSX (ne API).

### 3. Kvaliteta zraka u RH, INSPIRE (DGU/MZOE/AZO)
- **Poveznica:** `kvaliteta-zraka-u-republici-hrvatskoj-inspire`; vanjski portal http://iszz.azo.hr/iskzl/
- **Resursi:** WFS `servisi.azo.hr/inspire/geos1/ef/wfs` s tipovima **`ef1:EnvironmentalMonitoringFacility` i `ef1:ObservingCapability`**, plus WMS.
- **Licenca:** nije navedena u katalogu.
- **Što je u WFS-u:** INSPIRE-ov opis postaja i mogućnosti motrenja (lokacija, što se mjeri), **ne vrijednosti mjerenja**. `resultType=hits` nije vratio čitljiv odgovor, pa **broj postaja nepotvrđen**.
- **Mjerenja:** portal `iszz.azo.hr/iskzl/` ima stranice "Pregled podataka" i "Izvoz podataka u excel" (`podatak.htm`, `podatakexp.htm`), ali su dinamične (JavaScript). **Nepotvrđeno** postoji li strojno čitljiv endpoint za vrijednosti; nisam ga našao.
- **Način dohvata:** WFS za lokacije. Za vrijednosti vjerojatno scraping, što je rizično i ne računa se kao API.

### 4. Strateške karte buke (MZOE)
- **Poveznica:** `informacijski-sustav-strateskih-karata-buke-i-akcijskih-planova` (+ WFS, WMS, INSPIRE varijante).
- **Izdavatelj:** Ministarstvo zaštite okoliša i energetike. **Licenca:** nije navedena.
- **Resursi:** WMS i WFS (XML), plus HTML portal. 5 skupova u katalogu.
- **Svježina:** izmjena u katalogu 2026-04-15; razdoblje karata (nepotvrđeno) je vjerojatno 5-godišnji ciklus EU direktive o buci.
- **Ključ:** geometrija (zone/izolinije), nema šifre općine. **Sadržaj WFS-a nisam provjeravao** (nepotvrđeno: slojevi, razine dB).
- **Način dohvata:** WMS/WFS. Statični sloj, ne mjerenja.

### 5. Državna mreža hidroloških postaja (DGU INSPIRE) + DHMZ
- **Poveznica:** `dravna-mrea-hidrolokih-postaja-povrinskih-voda-inspire`.
- **Resursi:** WFS/WMS/ATOM na `transformiraj.nipp.hr`; poveznica na `meteo.hr`. WFS nudi samo **`ef:EnvironmentalMonitoringFacility`** (lokacije postaja).
- **Licenca:** nije navedena.
- **Mjerenja vodostaja/protoka na portalu NEMA.** Provjereni DHMZ endpointi `vrijeme.hr/hidro_n.xml` i slični daju 404; `hidro.dhz.hr` je HTML stranica bez uočenog API-ja; `hidro.voda.hr` se ne razrješava. **Nepotvrđeno** postoji li javni API za vodostaje.
- **Zaključak:** poplave kao tema nemaju pouzdan živ izvor koji sam uspio naći. Ne graditi na tome bez dodatnog istraživanja.

### 6. Reciklažna dvorišta + podzemni i polupodzemni spremnici (Grad Zagreb)
- **Poveznice:** `reciklazna-dvorista-grada-zagreba1`, `geoportal_podzemni_spremnik`, `polupodzemni_spremnik`.
- **Licenca:** otvorena dozvola RH. **Svježina:** katalog 2026-03/04, ali podaci iz 2023. (`last_modified`), bez osvježavanja.
- **Ključ:** koordinate (WGS84 Point). Reciklažna dvorišta imaju i **vrste otpada koje primaju** (papir, plastika, staklo, metal, baterije, bio, gume, električni, građevinski...), radno vrijeme, telefon, e-mail.
- **Provjereno:** 20 reciklažnih dvorišta, 67 podzemnih + 192 polupodzemna spremnika = 259 točaka. Sva geometrija je Point.
- **Kvaliteta:** podzemni/polupodzemni spremnici nemaju vrstu otpada ni popunjenost u svojstvima (samo status/adresa), pa su to lokacije, ne tokovi.
- **Način dohvata:** GeoJSON download (radi). **Nije** DataStore (`datastore_active=False`).

### 7. Zagreb mobilnost: biciklističke staze, parkirališta za bicikle, e-punionice
- **Poveznice:** `geoportal-biciklisticke-staze`, `geoportal-javna-parkiralista-za-bicikle`, `geoportal-elektricne-punionice`.
- **Licenca:** otvorena dozvola RH. Svježina: katalog 2026-04-15 (stvarna izmjena **nepotvrđena**).
- **Provjereno:** staze **2889** segmenata, MultiLineString, ukupno ~268,9 km, kategorije BP, BS, BT, CM, JS, PB, PO; parkirališta za bicikle **318** točaka (broj stalaka, broj bicikala, godina postavljanja); e-punionice **40** točaka (broj utičnica, tip).
- **Ključ:** koordinate, te `MJESNI_ODBOR` i `GRADSKA_CETVRT` (15 četvrti, prazno 0 od 2889).
- **Kvaliteta:** nazivi mjesnih odbora imaju **navodnike u vrijednosti** (`"Antun Mihanović"`), a staze i parkirališta dijele samo **70 mjesnih odbora** (108 vs 97 različitih). Za spoj treba normalizirati ili koristiti prostorni spoj.
- **Način dohvata:** GeoJSON (radi). CSV staza je u DataStoreu (nepotvrđeno čitanje, jer je isti obrazac pao na zagrebačkom zraku).

### 8. Rijeka: slobodna parkirna mjesta (UŽIVO)
- **Poveznica:** `slobodna-parkirna-mjesta-trenutni-uvid` (CC-BY 4.0).
- **Resursi:** CSV `rijeka-plus.hr/parking/parking.csv` i **JSON REST** `rijeka-plus.hr/wp-json/restAPI/v1/parkingAPI/`.
- **Svježina:** polje `parking_data.last_update_date/time` (živo).
- **Provjereno:** JSON API vraća HTTP odgovor, ali **prvi elementi su obavijesti (npr. čestitka), ne parkirališta** (`slobodno`, `kapacitet`, `lokacija` su `null`). Nepotvrđeno koliko je stvarnih parkirališta i jesu li brojila popunjena.
- **Relevantnost za B:** mobilnost/okolišno prihvatljiv transport, ali samo za Rijeku.
- **Način dohvata:** pravi REST API (jedan od rijetkih živih na portalu).

### 9. DZS: baza "Okoliš" (PxWeb)
- **Poveznica:** `okoli` (DZS), API `https://web.dzs.hr/PXWeb/api/v1/hr/Okolis/`.
- **Licenca:** otvorena dozvola RH. **Svježina:** izmjena skupa 2026-10-15; tablice javne odvodnje `updated` 2026-07-10.
- **Provjereno:** PxWeb **JSON API radi** i izlistava tablice (npr. `JO11.px` podrijetlo, pročišćavanje i ispuštanje otpadnih voda; `JO13.px` mreža javne odvodnje i uređaji), plus statistika voda i ekonomski računi za okoliš (računi emisija u zrak, porezi i naknade).
- **Ključ:** godina, razina (država/županija/sliv, nepotvrđeno po tablici). **Nema šifre općine** u pregledanim tablicama.
- **Način dohvata:** pravi API (PxWeb), strojno čitljiv. Godišnja frekvencija.

### 10. Zaštićena područja i Natura 2000
- **Poveznice:** `zatiena-podruja-republike-hrvatske-inspire` (DGU), `ekoloka-mrea-natura-2000-republike-hrvatske-inspire`, `registar-zasticenih-podrucja-podrcuja-posebne-zastite-voda` (Hrvatske vode), `geoportal-pgz-zasticena-podrucja`.
- **Resursi:** uglavnom WMS/WFS; Hrvatske vode daju WMS na `servisi.voda.hr/zasticena_podrucja/wms`.
- **Licenca:** nije navedena (osim PGZ: otvorena dozvola RH).
- **Ključ:** poligon (prostorni spoj). **Sadržaj nisam provjeravao** (nepotvrđeno slojevi, atributi, broj poligona).
- **Način dohvata:** OGC servisi, statični. Dobar kontekstni sloj za "zaštita prirode", ne živ.

## Vanjski izvori (izvan portala)

### A. DHMZ otvoreni XML feedovi (provjereno)
- `https://vrijeme.hr/hrvatska_n.xml`: **60 postaja** s koordinatama (50 automatskih), temperatura, vlaga, tlak, vjetar, vrijeme; osvježava se po terminima (u uzorku termin 19, datum 22.10.2026.).
- `https://vrijeme.hr/more_n.xml`: **temperatura mora po postajama** (npr. Dubrovnik 20,3, Mali Lošinj 20,9), više termina dnevno; neka polja prazna ili `-` (Crikvenica).
- **Licenca:** **nepotvrđeno** (feed ima samo `Copyright DHMZ - meteo.hr`); prije korištenja pročitati uvjete na meteo.hr.
- **Ostali feedovi** koje sam pogodio (`hidro_n.xml`, `uvi_n.xml`, `temp_n.xml`): 404. Ne pogađati nazive.
- **Oprez:** UTF-8 s kvarnim dijakriticima u terminalu (prikazni problem); koordinate imaju razmak (`17.200 `).

### B. Open-Meteo (Air Quality + Archive API, provjereno)
- Satni PM10, PM2,5 i europski AQI iz **Copernicus CAMS modela** za proizvoljnu koordinatu; povijest i prognoza. Test za Zagreb: bez nedostajućih vrijednosti, 8760 sati za 2023.
- Archive API: dnevna temperatura, oborine, vjetar za 2023. (365 dana, bez praznina).
- **Rezolucija je gruba** (zahtjev za 45,81077 N vratio je mrežnu točku 45,8 N, 16,0 E), pa ne razlikuje postaje unutar grada.
- **Licenca:** **nepotvrđeno** (CC-BY 4.0 je moje sjećanje, nije provjereno ovdje); provjeriti uvjete i ograničenja za nekomercijalnu uporabu prije oslanjanja. Prvi upit je jednom pao s prazanim odgovorom (kod HTTP 000), drugi put radi, pa računaj na ponovne pokušaje.

### C. OpenStreetMap Overpass (provjereno)
- Radi uz `User-Agent` (bez njega HTTP 406). Primjer: **745 čvorova `amenity=recycling`** u pravokutniku oko Zagreba.
- **Licenca:** ODbL (navedeno u odgovoru). Zahtijeva navođenje izvora.
- Korisno za sve što portal nema (kontejneri za otpad, javni prijevoz, zelene površine) i kao ključ za spoj s gradskim skupovima.

### D. Copernicus ADS/CAMS i EEA
- `ads.atmosphere.copernicus.eu` je dostupan (HTML), ali za podatke treba **registracija i API ključ** (nisam testirao). EEA download servis koji sam pogodio vraća 404. **Oba nepotvrđena**; Open-Meteo je jednostavniji put do CAMS-a.

## Neobični spojevi (provjereni na uzorku)

### Spoj 1: izmjereni zrak (Zagreb) ↔ CAMS model (Open-Meteo), po datumu
- **Ključ:** dan (+ koordinata postaje Ksaverska).
- **Provjera:** siječanj 2023., dnevni PM10: **30 dana, korelacija r = 0,75**; srednje izmjereno 17,7, model 22,0 µg/m3 (model precjenjuje oko 24 posto).
- **Ideja:** kalibrirani/"lokalizirani" model zraka: za bilo koju točku u Zagrebu procijeniti PM10 koristeći CAMS, ispravljen prema stvarnim postajama. Za građane bez postaje u blizini.
- **Pokrivenost:** 6 postaja, samo 2023., jedan mjesec provjeren. **Nepotvrđeno** za ostale mjesece i postaje. Za živu verziju treba novije podatke s postaja, a njih na portalu nema.
- **Rizik:** r = 0,75 je skroman, na jednoj postaji i jednom mjesecu (zimi). Ne obećavati točnost.

### Spoj 2: plaže (IZOR) ↔ DHMZ postaje (temperatura mora i meteo)
- **Ključ:** koordinate (najbliža postaja).
- **Provjera:** 106 plaža iz DataStorea prema 60 DHMZ postaja: **medijan 14,1 km, p90 30,0 km, max 36,8 km**; unutar 10 km 43 posto, unutar 25 km 80 posto.
- **Ideja:** "dnevni vodič za plažu": ocjena kakvoće + temperatura mora + vjetar/valovi za svaku plažu.
- **Ozbiljan problem:** ocjene kakvoće su iz **2015.**, a izvorni CSV nije dostupan (404). Temperaturu mora daje samo dio postaja. **Bez svježih ocjena kakvoće ideja gubi smisao**, a nisam našao strojno čitljiv izvor svježih ocjena. Nepotvrđeno postoji li ga u aplikaciji IZOR-a.
- **Verdikt:** zanimljivo, ali podatkovno najslabije tamo gdje je najvažnije.

### Spoj 3: reciklažna dvorišta ↔ podzemni/polupodzemni spremnici (Zagreb), prostorni
- **Ključ:** koordinate.
- **Provjera:** 20 reciklažnih dvorišta, 259 spremnika; najbliži spremnik od dvorišta: **medijan 1,18 km, max 11,4 km**.
- **Ideja:** karta "pokrivenosti odvojenim prikupljanjem" po četvrtima i otkrivanje bijelih mrlja.
- **Zamka:** spremnici nemaju vrstu otpada ni popunjenost, pa se zaključak svodi na udaljenost, što je plitko. Spoj postaje zanimljiv tek s OSM `amenity=recycling` (745 čvorova u okolici) i stanovništvom po četvrti (nepotvrđeno gdje: DZS popis po naseljima).

### Spoj 4: biciklističke staze ↔ parkirališta za bicikle ↔ e-punionice (Zagreb)
- **Ključ:** koordinate; `GRADSKA_CETVRT` (15 vrijednosti, 0 praznih) pouzdaniji od `MJESNI_ODBOR`.
- **Provjera:** staze 2889 segmenata (~268,9 km), parkirališta 318 točaka, punionice 40; početak najbliže staze od e-punionice medijan 0,13 km (grubi uzorak, samo prva točka).
- **Ideja:** indeks "bicikl-prijateljskosti" po četvrti, ili usmjeravanje s e-punionicama i stalcima.
- **Zamka:** statični podaci, nema prometa ni sigurnosti. Mjesni odbori ne podudaraju se između slojeva (samo 70 zajedničkih).

### Spoj 5 (hipoteza, samo djelomično provjerena): DZS statistika voda/emisija ↔ gradski skupovi
- **Provjereno:** DZS PxWeb API radi (tablice javne odvodnje, računi emisija u zrak).
- **Nepotvrđeno:** razina detalja (općina/županija), ključ za spoj s gradskim skupovima. Ne ocjenjivati prije nego se pregledaju stvarne tablice.

## Zamke i ograničenja ove sesije
- `datastore_active=True` nije jamstvo: `datastore_search` radi za kakvoću mora (106 redaka), ali **ne** za zagrebačke XLSX resurse zraka.
- Skupovi iz okoliša na portalu većinom nisu "živi". Prije obećanja "u realnom vremenu" treba izvana (DHMZ, Open-Meteo, Rijeka REST).
- Katalog ima **kvarne dijakritike u nekim poljima** (`prora�un`); ID-ovi su ASCII pa pretraga radi s ASCII riječima (`pozar`, `zasticena podrucja`), dok je upit s dijakriticima (`požar`) vratio prazan odgovor.
- Pretraga `hidrolo*`, `vodostaj`, `klimat`, `energetska` vratila 0 pogodaka. Poplave, požari i klimatski rizici nemaju strojno čitljive skupove koje sam našao (osim DUZS CSV vatrogasnih intervencija, vidi ispod).
- `analiza-vatrogasnih-intervencija` (DUZS): jedan CSV resurs je **samo poveznica na stranicu** `duzs.hr/o-nama/otvoreni-podaci/`, a izdavatelj je označen "NEAKTIVAN". Sadržaj nisam skidao (nepotvrđeno).
- TLS: Windows `curl` je na ArcGIS i `servisi.azo.hr` zahtijevao `--ssl-no-revoke`; `servisi.azo.hr` zatvara vezu bez `close_notify` (odgovor ipak stiže).
- Nisam provjeravao: sadržaj WFS slojeva buke i zaštićenih područja, dostupnost Copernicus API-ja, stvarne licence većine državnih skupova, novije godine zagrebačkih mjerenja.

## Sirovi pogledi za brainstorm
- Najčvršća kombinacija koju sam našao: **gradski statični slojevi (Zagreb) + DHMZ/Open-Meteo kao živi sloj**. Zagreb je jedini grad s bogatim geo skupovima za okoliš i mobilnost.
- Najslabija točka cijelog izazova B: **svježina**. Svaka ideja koja traži "danas" ovisi o vanjskom izvoru, a taj se ne boduje kao portal, ali se boduje kao "Povezivanje".
- Open-Meteo/CAMS je jedini izvor koji pokriva cijelu Hrvatsku satno za zrak, ali je model, ne mjerenje. Žiri to može vidjeti kao slabost ili kao kreativno rješenje, ovisno o tome kako se prikaže (provjeriti kroz `jury-simulator`).
