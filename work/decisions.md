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
