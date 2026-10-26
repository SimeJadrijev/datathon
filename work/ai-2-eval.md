# AI-2: izvlačenje polja, provjera točnosti

Uzorak: 21 poziv (kontrolni FZOEU 275 + do 4 otvorena po izvoru), model claude-haiku-5-5, seed snapshot od 24.10.2026. Trošak jednog mjerenja: 0.030684 USD (oko 0,0015 USD po pozivu).
Stupci s vrijednostima: vrijednost iz pravila, a kad je LLM vratio drugu, strelica i **LLM vrijednost**.

Provjera: svaku vrijednost koju je LLM izvukao tražio sam doslovno u izvornom tekstu poziva (skriptom), a sporne i nul-slučajeve čitao sam u tekstu. "DA" znači da sam pročitao i potvrdio. "U tekstu" znači da broj ili datum postoji u tekstu, ali nisam potvrdio da je to pravo značenje (npr. je li to baš najveći iznos). Prije predaje to treba pogledati još jednom za redove bez "DA".

| Poziv | Naslov | Rok | Ukupno | Min | Max | Provjera |
|---|---|---|---|---|---|---|
| FZOEU 275 | Javni poziv za sufinanciranje energetski učinkovitih vo | 2028-03-31 → **null** | 20000000 | null | 2500 → **9000** | DA. Kontrolni slučaj: 9.000 EUR po vozilu, rok null (31.3.2028. je kraj nabave) |
| APPRRR 43738 | Objavljen Treći natječaj za intervenciju 73.02 Obnova p | 2026-11-02 → **2027-01-04** | 10000000 | null → **1000** | 1000 → **1000000** | rok i iznosi su u tekstu, značenje min/max nije čitano |
| APPRRR 43887 | Četvrte izmjene drugog Natječaja za provedbu intervenci | 2026-12-01 | null | null | null | DA. Rok 1.12.2026.; iznosi "15.000-250.000 EUR SO" nisu iznos potpore, pa null |
| APPRRR 44156 | Objavljen novi Javni poziv za provedbu intervencije 78. | 2026-11-12 | 20162329 | 200 | 42000 | rok i iznosi su u tekstu |
| APPRRR 44166 | Podnošenje zahtjeva za potporu iz Programa kompenzacijs | 2026-11-14 | null | null | null | rok je u tekstu |
| EU_FUNDS_NPOO 10 | Financiranje provedbe već započetih investicijskih proj | 2023-06-13 | 106178246.73 | null | 106178246.73 | DA. U tekstu piše 13.06.23 (dvoznamenkasta godina) |
| EU_FUNDS_NPOO 113 | Izgradnja, rekonstrukcija i opremanje osnovnih škola za | 2026-12-31 | 1622773604 | 50000 | 30000000 | rok i iznosi su u tekstu |
| EU_FUNDS_NPOO 114 | Izgradnja, rekonstrukcija i opremanje osnovnih škola za | 2026-12-31 | 200877596 | 50000 | 6000000 | rok i iznosi su u tekstu |
| EU_FUNDS_NPOO 216 | Nastavak financiranja projekata smanjenja rizika od kat | 2026-06-30 | 39479942 | null | 39479942 | DA. U tekstu piše 30.06.26 |
| FZOEU 247 | Javni poziv za odabir obrađivača reciklabilnog komunaln | null | null | null | null | DA. Rok ne postoji u tekstu |
| FZOEU 257 | Izmjena Javnog poziva za odabir obrađivača reciklabilno | null | null | null | null | rok ne postoji u tekstu (570 znakova) |
| FZOEU 268 | Dekarbonizacija i modernizacija toplinskih sustava za g | 2026-11-09 | 80000000 | null | 30000000 | rok i iznosi su u tekstu |
| FZOEU 270 | Javnoi poziv Fonda za neposredno sufinanciranje uklanja | null | null | null | null | DA. Piše samo početak prijava (3.6.2026.), rok ne postoji |
| HZZ 4347 | Potpore za zapošljavanje | null | null | null → **340** | 2420 → **1130** | DA. Tablica potpore: 340 do 1.130 EUR mjesečno |
| HZZ 4360 | Potpore za zapošljavanje – zeleno/digitalno | null | null | null → **340** | 2420 → **1130** | isti obrazac tablice kao HZZ 4347 |
| HZZ 4365 | Potpore za zapošljavanje osoba uključenih u program POS | null | null | null → **340** | 2420 → **1130** | isti obrazac tablice kao HZZ 4347 |
| HZZ 4372 | Potpore za pripravništvo i mlade | null | null | null → **20** | 590 → **890** | NESIGURNO. Tablica ima 590, 740, 690, 870; rezultat varira između pokretanja |
| MZOM 7987 | Natječaj za stipendije Slobodne Države Bavarske za dipl | 2026-12-01 | null | null → **992** | 992 | iznos je u tekstu |
| MZOM 7990 | Natječaj za stipendije Švicarske Konfederacije u akadem | 2026-11-30 | null | null | null | DA. Iznos je u CHF, pa je max ispravno null |
| MZOM 8027 | Poziv na dostavu projektnih prijedloga u sklopu FOREST  | 2026-12-02 | 40000000 | null | null | DA. U tekstu "približno 40 milijuna eura" |
| MZOM 8068 | Javni natječaj za dodjelu državnih stipendija za studen | 2026-10-31 | null | null → **292** | null → **360** | DA. Stipendija 360 EUR (uredno) ili 292 EUR (zaostajanje) |

## Što je mjerenje otkrilo (i ispravljeno u promptu)
- FZOEU 275: prva verzija dala je max 62.500 (najviša cijena vozila iz tablice) umjesto 9.000 (iznos potpore po vozilu).
- HZZ: druga verzija dala je max 1.494,75 (najniža bruto plaća, ne potpora). Ispravljeno: max je najviša potpora, nikad plaća ili cijena.
- MZOM 7990: iznos 2.450 CHF zapisan je kao EUR. Ispravljeno: iznosi u drugim valutama su null.
- MZOM 8068: jednom se vratio sažetak "x". Sažetak kraći od 20 znakova se odbacuje.
- Model je nesiguran na HZZ 4372 (tekst od 80.000 znakova, šaljemo prvih 40.000). Rezultat se mijenja od poziva do poziva, a temperatura se u ovom SDK-u ne može postaviti.

## Usporedba s POC-om (isti postupak kao `poc/IZVJESTAJ.md`, 26.10.)
Slaganje Haiku i Sonnet 5.5 na istom uzorku od 21 poziva (rok: isti datum; iznosi: unutar 1 %; skupine i područja: Jaccard):

| Polje | POC (15 poziva) | Java implementacija (21 poziv) |
|---|---:|---:|
| rok_prijave | 100 % | 100 % |
| iznos_po_korisniku_max | 87 % | 90 % |
| iznos_ukupno | 100 % | 100 % |
| ciljne_skupine | 65 % | 82 % |
| podrucja | 81 % | 85 % |

Trošak po pozivu: Haiku 0,0014 USD (POC prosječno 0,00084, uzorak je ovdje duži jer su samo otvoreni pozivi), Sonnet 0,027 USD (POC 0,017). Kontrolni slučaj FZOEU 275 daje max 9.000 i rok null, isto kao u POC-u. Ovo je slaganje dva modela, ne dokaz točnosti (POC također nije imao ručni uzorak); ručna provjera je gore.

Što je u Java verziji preuzeto iz POC-a nakon usporedbe koda:
- strukturirani izlaz (JSON shema) i `effort: low` umjesto tool use-a, `max_tokens` 4000, retry do 3 puta
- odrezan (`max_tokens`) ili odbijen (`refusal`) odgovor ne upisuje se kao "model nije našao ništa"
- tekstovi kraći od 300 znakova se ne šalju modelu
- davatelj i datum objave iz izvora ne pregaziti, model samo popunjava praznine
