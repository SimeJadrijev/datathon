# Podaci

_Datasetovi, izvori, shema, veličina, licence i ograničenja. Obvezni dio dokumentacije predaje: popis svih skupova s izdavateljima i poveznicama._

## Pristup portalu (provjereno 2026-10-22)
- **CKAN API radi:** `https://data.gov.hr/ckan/api/3/action/<akcija>` (package_search, package_show, organization_list, datastore_search). JSON, bez autentikacije.
- `https://data.gov.hr/api/3/...` (bez `/ckan/`) i WebFetch vraćaju samo SPA HTML. Ne koristiti.
- Katalog: **3956 skupova**. Detalji i naredbe u `.claude/skills/data-scout/SKILL.md`.

## Snimka kataloga (2026-10-22)
- **Formati:** XLSX 1014, PDF 955, CSV 818, XLS 479, HTML 555, JSON 188, XML 175, GeoJSON 108, SHP 102, WMS 87, KML 79, WFS 56.
- **Licence:** otvorena dozvola RH 3084, CC-BY 312, ostalo malo. Korištenje uz navođenje izvora.
- **Svježina:** godišnje 654, mjesečno 144, dnevno 33, tjedno 18, kontinuirano 4. **Većina podataka nije "živa"**, pa ideje koje obećavaju real-time moraju koristiti ~55 svježih skupova ili vanjske API-je.
- **Najveći izdavači:** Grad Zagreb 210, Grad Rijeka 181, MUP 114, DZS 105, DGU 87, Grad Rovinj 74, Min. zaštite okoliša i energetike 61, Min. poljoprivrede 53.
- Postoji polje `hvd_category` (EU skupovi visoke vrijednosti) i oznaka `datastore_active` po resursu. Prvi uzorak: `datastore_active=false`, pa dostupnost kroz API treba provjeriti po resursu.

## Rezultati scouta (2026-10-22)
Detalji po izazovu: `context/scout/izazov-A.md`, `izazov-B.md`, `izazov-C.md` (kandidat-skupovi u blokovima + spojevi). Napomena: nalazi agenata nisu neovisno provjereni, a licence vanjskih izvora (DHMZ, Open-Meteo, Eurostat, Autotrolej API) su **nepotvrđene**.

**Prekogranični nalazi**
- **Na portalu nema živih mjerenja okoliša.** Od 33 dnevnih i 18 tjednih skupova nijedan nije zrak, more, voda, buka ni otpad. Živo mora doći izvana (DHMZ, Open-Meteo/CAMS).
- **Geo podaci (koordinate) postoje gotovo samo za Grad Zagreb** (škole, vrtići, domovi za starije, ljekarne, ZET stajališta, reciklažna dvorišta, bicikl infrastruktura) i vozni red Rijeke. Nacionalni skupovi o školama/vrtićima nemaju koordinate.
- `datastore_active=true` ne znači da `datastore_search` radi. Provjeravati po resursu. Često je pouzdaniji izravan download.
- Pretraga s dijakriticima (`požar`) vraća prazno, ASCII (`pozar`) radi.
- Windows: `curl` ne dohvaća ArcGIS (provjera certifikata), Python `urllib` radi. Portal povremeno vraća 502.

**A) Društvo:** najbolji spojevi su kulturna dobra x umjetničke organizacije po županiji (ključ se podudara u svih 21 županije, npr. Šibensko-kninska 308 dobara / 2 organizacije), te domovi za starije x zborna mjesta civilne zaštite (medijan 1,9 km, ali samo 17 točaka zbornih mjesta). Škole x ZET stajališta: medijan 157 m, slab nalaz. Autotrolej vozni red je svjež (36 842 retka), API s pozicijama traži token.

**B) Okoliš:** najjači spoj je Zagreb zrak (mjerenja, 6 postaja) x Open-Meteo/CAMS model po datumu (PM10 r=0,75, model precjenjuje ~24%), no podaci su slabi: jedan mjesec, mjerenja samo za 2023. Plaže x DHMZ: medijan 14 km do postaje, ocjene kakvoće iz 2015., izvorni IZOR CSV 404. Poplave/vodostaji: nije nađen strojno čitljiv izvor.

**C) Gospodarstvo:** EOJN registar ugovora x JPT registar po OIB-u naručitelja (spojeno 73% naručitelja, 67% ugovora, 55% vrijednosti; nespojeni su bolnice i HEP). EOJN izvoz je točno 20 000 redaka, vjerojatno odsječen, bez broja ponuda (nema "jedan ponuditelj"). Fina RGFI traži registraciju, Sudreg API vraća 401. Agent procjenjuje izazov kao zasićen.

## Narodne novine API (provjereno 2026-10-24, izvan portala)
- Dokumentacija: https://narodne-novine.nn.hr/nn_api_hr.aspx (verzija 1.0, 18.4.2023.). Bez ključa.
- Endpointi: `GET /api/index` (godine, vraća 2015-2026), `POST /api/editions` {part,year} (brojevi izdanja, 2026: 121), `POST /api/acts` {part,year,number} (popis akata u izdanju), `POST /api/act` {part,year,number,act_num,format} (metapodaci akta u JSON-LD ili RDF/XML). `part` je "SL" ili "MU".
- Metapodaci: naslov, vrsta dokumenta, datumi, institucija, EuroVoc teme, pravno područje, poveznice na HTML/PDF. **Nema punog teksta u API-ju**, a "ne radi s dijelom OG (oglasi)".
- Puni tekst: HTML na `https://narodne-novine.nn.hr/eli/sluzbeni/{godina}/{izdanje}/{akt}/hrv/html` vraća 200 (jedna uredba ~9 100 znakova teksta). To je scraping, ne API.
- **Ograničenje: 3 zahtjeva u sekundi.** Svi navedeni endpointi rade. Na Windowsu `curl` pada (provjera opoziva certifikata), Python `urllib` radi.
- **Licenca nepotvrđena:** uvjeti kažu da su sadržaji zaštićeni autorskim pravom i da se smiju koristiti "pod uvjetima propisanim Zakonom". Treba provjeriti smije li se tekst propisa ponovno koristiti (Pravila traže da licenca dopušta uporabu za Datathon).
- **Pravno područje (provjereno 2026-10-24):** vokabular `https://narodne-novine.nn.hr/eli/vocabularies/nn-legal-area` ima samo 16 vrlo širokih područja: međunarodno, građansko, kazneno i kazneni postupak, prekršajno, financijsko, građansko procesno, trgovačko i pravo društava, obiteljsko, ustavno, informacijsko i industrijsko vlasništvo, radno i socijalno, pomorsko i općeprometno, upravno, zaštita okoliša, europsko javno, ostala. (Par oznaka i ID-a (16-31) iščitan iz XML-a, pretpostavljen redoslijed oznaka pa ID.) **Prekrupno za mapiranje na djelatnost** (npr. "radno pravo" vrijedi za sve poslodavce). Pojedinačni zapisi vokabulara vraćaju 404, vokabular se dohvaća kao jedan XML.
- Nije provjereno: finiji vokabular `nn-index-terms` (ID-ovi poput 288, 2152 u metapodacima) i dubina EuroVoc tema, postoji li NKD mapiranje (NE vidim ga u metapodacima), koliko je tekstova propisa ELI-označeno prije 2015.
