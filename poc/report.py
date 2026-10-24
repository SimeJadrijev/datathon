"""Generira IZVJESTAJ.md iz baze (+ data/opendata.json + docs/zakljucak.md)."""
import csv
import json
from datetime import date

import config
from common import db
from common.models import POLJA

OPENDATA_JSON = config.DATA / "opendata.json"

# Ručna procjena nakon izviđanja (vidi docs/izvori.md): (težina održavanja 1-5, problemi)
TEZINA = {
    "apprrr": (2, "RSS ima samo 10 objava; arhiva preko WP REST API-ja. Dokumenti su na zasebnoj stranici intervencije, a ne u objavi. Objave miješaju natječaje, izmjene i obavijesti o isplatama."),
    "fzoeu": (2, "Zadani URL /hr/natjecaji/7 preusmjerava; pravi popis je jedna accordion stranica. Status je u CSS klasi. Rok je samo u tekstu/PDF-u, često 'do utroška sredstava'."),
    "fondovieu": (3, "Nuxt SSR radi bez JS-a, ali filtar 'u najavi' nije pronađen; robots.txt zabranjuje /download pa prilozi nisu dostupni. Pokriva samo NPOO; pozivi 2021.-2027. su na eufondovi.gov.hr."),
    "hamag": (3, "WP bez REST API-ja; grid s kategorijama u CSS klasama. Dio stavki vodi na eufondovi.gov.hr. Puno vijesti (webinari, najave) među natječajima."),
    "hzz": (1, "Čist WP REST API s taksonomijama (godina, ciljna skupina). Mjere su kontinuirane: nema roka ni ukupnog iznosa u tekstu."),
    "udruge": (1, "Najbolji izvor: središnji katalog s filtrima i strukturiranim detaljem (županija, rok, ukupni/min/max iznos). Tekst natječaja je samo poveznica na stranicu davatelja."),
    "mzom": (2, "Isti CMS kao udruge.gov.hr; 'Rok:' u popisu. Popis miješa oglase za posao i obavijesti (filtrira se po naslovu)."),
    "ampeu": (2, "Statični HTML s paginacijom; 'Rok je prošao' kao oznaka statusa. Većina stavki su Erasmus+/CEEPUS pozivi institucijama i obavijesti o radionicama."),
    "rijeka": (4, "Natječaji za financiranje nisu na jednom mjestu: stranica 'Ostali natječaji' ima 1-2 stavke, ostalo su vijesti. Pretraga vijesti donosi i izvještaje o već dodijeljenom novcu."),
    "osijek": (2, "WP REST API s tipom 'natjecaji', ali tip miješa savjetovanja, oglase, prodaju zemljišta (~57 % se filtrira). Sadržaj je gotovo uvijek samo PDF/DOCX u ACF polju."),
    "zagreb": (5, "Nema jedinstvenog popisa; tematske stranice uređene ručno, vlastiti CMS bez semantičkih klasa, stare stavke (2024.) se ne uklanjaju. Za svaku temu treba poseban URL."),
}


def collect_opendata():
    from opendata import ckan, sudreg
    out = {"datum": date.today().isoformat()}
    print("Otvoreni podaci: CKAN...")
    try:
        out["ckan"] = ckan.run()
    except Exception as e:
        out["ckan"] = {"greska": f"{type(e).__name__}: {e}"}
    try:
        out["udruge_rijeka"] = ckan.udruge_rezultati_po_davatelju(r"rijek")
    except Exception as e:
        out["udruge_rijeka"] = {"greska": f"{type(e).__name__}: {e}"}
    print("Otvoreni podaci: Sudski registar...")
    try:
        out["sudreg_openapi"] = sudreg.openapi_info()
        out["sudreg_bez_kljuca"] = sudreg.probe_without_key()
    except Exception as e:
        out["sudreg_openapi"] = {"greska": f"{type(e).__name__}: {e}"}
    out["sudreg_ima_kljuc"] = sudreg.has_credentials()
    primjer = "92803032010" if sudreg.has_credentials() else "00000000001"   # stvarna tvrtka (KOESTLIN d.d.) / mock
    try:
        out["sudreg_primjer_profila"] = sudreg.profil_iz_oib(primjer)
        out["sudreg_primjer_odgovora"] = sudreg.detalji(primjer)
    except Exception as e:
        out["sudreg_primjer_profila"] = {"greska": f"{type(e).__name__}: {e}"}
    OPENDATA_JSON.write_text(json.dumps(out, ensure_ascii=False, indent=2), encoding="utf-8")


def pct(n, d):
    return f"{100 * n / d:.0f} %" if d else "-"


def filled(v) -> bool:
    return v not in (None, "", [], {})


def completeness(recs: list[dict]) -> dict:
    return {f: sum(1 for r in recs if filled(r.get(f))) for f in POLJA}


def generate(con):
    recs = db.all_records(con)
    rows = {r["id"]: r for r in con.execute("SELECT * FROM natjecaji")}
    pravila = []
    for r in rows.values():
        pj = json.loads(r["pravila_json"]) if r["pravila_json"] else {}
        pravila.append(pj)
    logs = {}
    for l in con.execute("SELECT * FROM izvori_log ORDER BY run_id"):
        logs[l["izvor"]] = l              # zadnje pokretanje po izvoru
    od = json.loads(OPENDATA_JSON.read_text(encoding="utf-8")) if OPENDATA_JSON.exists() else {}
    L = []
    w = L.append

    w("# Izvještaj POC-a: agregator natječaja, poticaja i stipendija\n")
    w(f"_Generirano {date.today().isoformat()} skriptom `run_all.py` / `report.py`. Brojke su iz `data/natjecaji.sqlite`._\n")
    n = len(recs)
    otv = sum(1 for r in recs if r["status"] == "otvoren")
    w(f"**Ukupno u bazi: {n} natječaja/poziva iz {len(logs)} izvora; otvorenih: {otv}; najavljenih: "
      f"{sum(1 for r in recs if r['status'] == 'najavljen')}.** LLM izvlačenje: "
      f"{sum(1 for r in recs if r['izvuceno_llm'])} zapisa.\n")
    zak = config.ROOT / "docs" / "zakljucak.md"
    if zak.exists():
        w(zak.read_text(encoding="utf-8").strip() + "\n")

    # 1. tablica po izvoru
    w("## 1. Izvori\n")
    w("Svih 8 skupina izvora iz upute je pokušano (11 modula). „Dohvaćeno\" = stavke s popisa nakon filtriranja "
      "onoga što nije financiranje (oglasi za posao, nabava, prodaja nekretnina). Detalji i PDF-ovi su namjerno "
      f"ograničeni na {config.MAX_DETAILS_PER_SOURCE} detalja i {config.MAX_PDF_ITEMS_PER_SOURCE} natječaja s PDF-om po izvoru "
      "(1 zahtjev/s po domeni), pa „s rokom/iznosom\" za velike izvore podcjenjuje ono što je moguće.\n")
    w("| Skupina | Izvor | Metoda | Dohvaćeno | Otvorenih | S rokom | S iznosom | S PDF-om (preuzeto) | Vrijeme | HTTP (cache) | Težina 1-5 | Problemi |")
    w("|---|---|---|---:|---:|---:|---:|---:|---:|---:|:---:|---|")
    for izvor, l in sorted(logs.items(), key=lambda x: x[1]["kategorija"]):
        rr = [r for r in recs if r["izvor"] == izvor]
        s_rok = sum(1 for r in rr if r["rok_prijave"])
        s_izn = sum(1 for r in rr if r["iznos_ukupno_eur"] or r["iznos_po_korisniku_max_eur"])
        s_pdf = sum(1 for r in rr if any(p["url"].lower().split("?")[0].endswith(".pdf") for p in (r["prilozi"] or [])))
        s_pdf_dl = sum(1 for r in rr if rows[r["id"]]["pdf_preuzeto"])
        tez, prob = TEZINA.get(izvor, ("?", ""))
        nap = json.loads(l["napomena"] or "{}")
        extra = ""
        if nap.get("ukupno_na_izvoru"):
            extra = f" Na izvoru ukupno: {nap['ukupno_na_izvoru']}."
        greske = json.loads(l["greske"] or "[]")
        if greske:
            extra += f" Greške: {len(greske)} (npr. {greske[0][:80]})."
        w(f"| {l['kategorija']} | {izvor} | {l['metoda']} | {len(rr)} | {sum(1 for r in rr if r['status'] == 'otvoren')} | "
          f"{s_rok} | {s_izn} | {s_pdf} ({s_pdf_dl}) | {l['trajanje_s']:.0f} s | {l['http_zahtjeva']} ({l['cache_pogodaka']}) | {tez} | {prob}{extra} |")
    w("")
    from match import _kljuc
    po_nazivu: dict[str, set] = {}
    for r in recs:
        po_nazivu.setdefault(_kljuc(r["naziv"]), set()).add(r["izvor"])
    dup = sum(1 for v in po_nazivu.values() if len(v) > 1)
    w(f"Isti natječaj u više izvora (isti normalizirani naziv): **{dup}** (npr. MZOM i AMPEU objavljuju iste "
      "stipendije, Osijek i Ured za udruge iste pozive za udruge). Konačni proizvod treba deduplikaciju; "
      "`match.py` ih spaja po nazivu.\n")

    # 2. potpunost
    w("## 2. Potpunost polja\n")
    pre = completeness(pravila)
    post = completeness(recs)
    llm_n = sum(1 for r in recs if r["izvuceno_llm"])
    w(f"Cijeli skup ({n} zapisa). „Prije\" = samo pravila/regex i strukturirani podaci izvora; „poslije\" = nakon LLM-a "
      f"({llm_n} zapisa obrađeno LLM-om). Na otvorenim i detaljno dohvaćenim natječajima potpunost je bitno veća, vidi drugu tablicu.\n")
    det_ids = {i for i, r in rows.items() if r["detalji_dohvaceni"]}
    pre_det = completeness([p for p in pravila if p.get("id") in det_ids])
    post_det = completeness([r for r in recs if r["id"] in det_ids])
    nd = len(det_ids)
    w(f"| Polje | Prije LLM (svi) | Poslije LLM (svi) | Prije LLM (s detaljima, n={nd}) | Poslije LLM (s detaljima) |")
    w("|---|---:|---:|---:|---:|")
    for f in POLJA:
        if f in ("izvuceno_llm",):
            continue
        w(f"| {f} | {pct(pre[f], n)} | {pct(post[f], n)} | {pct(pre_det[f], nd)} | {pct(post_det[f], nd)} |")
    w("")

    # 3. točnost
    w("## 3. Točnost LLM-a na uzorku\n")
    uzorak = config.ROOT / "eval" / "uzorak.csv"
    filled_rows = []
    if uzorak.exists():
        with uzorak.open(encoding="utf-8-sig") as f:
            filled_rows = [r for r in csv.DictReader(f) if any((v or "").strip() for k, v in r.items() if k.startswith("tocno_"))]
    if filled_rows:
        from eval.usporedi import evaluate, format_table
        w(format_table(evaluate(filled_rows)))
    else:
        w("Tim još nije ručno ispunio `eval/uzorak.csv`, pa točnost nije izmjerena. Postupak:\n")
        w("1. `python eval/make_sample.py` (15 nasumičnih natječaja s tekstom; uz ključ pokreće i `claude-sonnet-5-5` na istom uzorku za usporedbu).")
        w("2. Za svaki redak otvoriti `url` i upisati točne vrijednosti u stupce `tocno_*` (rok, iznos max, iznos ukupno, ciljne skupine, područja). Prazno = polje ne postoji u izvoru.")
        w("3. `python eval/usporedi.py` ispisuje točnost po polju za pravila, Haiku i Sonnet; `python run_all.py --samo-izvjestaj` je upisuje ovdje.\n")
        with uzorak.open(encoding="utf-8-sig") as f:
            sample = list(csv.DictReader(f)) if uzorak.exists() else []
        if any(r.get("sonnet_rok_prijave") or r.get("sonnet_podrucja") for r in sample):
            from eval.usporedi import POLJA as EP, score_field
            w(f"**Privremena mjera (nije točnost): slaganje s `claude-sonnet-5-5` kao referencom** na istom uzorku "
              f"(n={len(sample)}, nasumično iz natječaja koje je Haiku obradio). Rok i iznosi: isti datum / iznos unutar 1 %; "
              "skupine i područja: prosječna Jaccard sličnost skupova.\n")
            w("| Polje | Pravila vs Sonnet | Haiku vs Sonnet |")
            w("|---|---:|---:|")
            for fld in EP:
                cells = []
                for m in ("pravila", "haiku"):
                    v = [score_field(fld, r.get(f"{m}_{fld}", ""), r.get(f"sonnet_{fld}", "")) for r in sample]
                    cells.append(f"{100 * sum(v) / len(v):.0f} %")
                w(f"| {fld} | {cells[0]} | {cells[1]} |")
            w("\nVisoko slaganje dvaju modela ne dokazuje točnost (mogu griješiti isto), ali pokazuje da jeftiniji Haiku "
              "daje praktički iste vrijednosti kao 20x skuplji Sonnet. Konačnu točnost daje tek ručna provjera.\n")

    # 4. trošak
    w("## 4. Trošak LLM-a\n")
    tot = con.execute("SELECT model, COUNT(*) n, SUM(input_tokens) i, SUM(output_tokens) o, SUM(cijena_usd) c FROM llm_log GROUP BY model").fetchall()
    avg_chars = con.execute("SELECT AVG(MIN(length(sirovi_tekst), 40000)) FROM natjecaji WHERE length(sirovi_tekst) > 300").fetchone()[0] or 0
    n_text = con.execute("SELECT COUNT(*) FROM natjecaji WHERE length(sirovi_tekst) > 300").fetchone()[0]
    if tot:
        w("| Model | Poziva | Ulazni tokeni | Izlazni tokeni | Trošak USD | USD po natječaju |")
        w("|---|---:|---:|---:|---:|---:|")
        for t in tot:
            w(f"| {t['model']} | {t['n']} | {t['i']} | {t['o']} | {t['c']:.4f} | {t['c'] / max(t['n'], 1):.5f} |")
        w("")
    else:
        w("LLM nije pozvan u ovom okruženju (nema `ANTHROPIC_API_KEY`). Procjena iz duljine prikupljenog teksta:\n")
    est_in = avg_chars / 3.2 + 900          # ~3,2 znaka po tokenu za hrvatski + system prompt i shema
    est_out = 450
    for model in (config.LLM_MODEL_BULK, config.LLM_MODEL_COMPARE):
        pin, pout = config.LLM_PRICES[model]
        per = est_in / 1e6 * pin + est_out / 1e6 * pout
        w(f"- **{model}**: ~{est_in:.0f} ulaznih + ~{est_out} izlaznih tokena po natječaju ≈ **{per:.5f} USD/natječaj**; "
          f"za {n_text} natječaja s tekstom ≈ {per * n_text:.2f} USD, za cijeli skup ({n}) ≈ {per * n:.2f} USD.")
    w(f"\nCijene: Haiku 5.5 {config.LLM_PRICES['claude-haiku-5-5']} USD, Sonnet 5.5 {config.LLM_PRICES['claude-sonnet-5-5']} USD po milijun tokena (ulaz, izlaz). "
      f"Budžet POC-a je ograničen u kodu na {config.LLM_BUDGET_USD} USD (`POC_LLM_BUDGET_USD`).\n")

    # 5. Sudski registar
    w("## 5. Sudski registar (OIB -> NKD + sjedište)\n")
    oa = od.get("sudreg_openapi", {})
    bk = od.get("sudreg_bez_kljuca", {})
    w(f"- OpenAPI dokumentacija: HTTP {oa.get('status')}, {oa.get('endpointa')} endpointa, sigurnost: {oa.get('security')}.")
    w(f"- **Registracija je potrebna, ali je besplatna i samostalna**: obrazac na sudreg-data.gov.hr (naziv, e-mail, tip "
      f"„Javni korisnik\"), nakon potvrde e-maila odmah se dobiju Client ID i Client Secret. Pozivi idu s OAuth2 "
      f"*client credentials* tokenom (`{oa.get('token_url')}`). Bez ključa `detalji_subjekta` vraća HTTP {bk.get('status')}.")
    w(f"- Ključ u ovom okruženju: {'DA, korišten pravi API' if od.get('sudreg_ima_kljuc') else 'NE, korišten mock s 3 izmišljene tvrtke (`opendata/sudreg_mock.json`)'}. "
      "Izmišljeni OIB-ovi iz mocka uvijek idu na mock, pa testni profili rade i bez ključa.")
    w("- **Dohvat po OIB-u radi** (provjereno na stvarnim tvrtkama): `detalji_subjekta?tip_identifikatora=oib&identifikator=<OIB>` vraća "
      "`tvrtka`, `sjediste` (županija, općina, naselje, adresa), `pravni_oblik.vrsta_pravnog_oblika.kratica` i djelatnost.")
    w("- **Djelatnost**: `pretezita_djelatnost` je za javne korisnike prazna, ali `glavna_djelatnost` sadrži **NKD 2007 šifru bez točke** "
      "(1072 -> 10.72 Proizvodnja dvopeka i keksa, 4120 -> 41.20, 150 -> 01.50). To NIJE ID iz šifrarnika `/nacionalna_klasifikacija_djelatnosti` "
      "(tamo je 1072 „Vađenje željeznih ruda\", NKD 1994). `predmeti_poslovanja` imaju šifre iz starijih NKD verzija (1994, 2002), pa ih POC ne koristi. "
      "NKD odjeljak se preslikava u područja interesa (`opendata/nkd.py`).")
    w("- Ograničenja: obrti nisu u Sudskom registru (vode se u Obrtnom registru, koji POC nije provjeravao), pa prijava OIB-om radi za "
      "trgovačka društva, zadruge i ustanove. Podaci se osvježavaju jednom dnevno radnim danom.\n")
    if od.get("sudreg_primjer_profila"):
        w(f"Primjer {'stvarnog' if od.get('sudreg_ima_kljuc') else 'mock'} odgovora (skraćeno) i profila koji iz njega nastaje:\n")
        w("```json\n" + json.dumps({k: v for k, v in od.get("sudreg_primjer_odgovora", {}).items()
                                     if k in ("potpuni_oib", "tvrtka", "sjediste", "pravni_oblik", "glavna_djelatnost", "_mock")},
                                    ensure_ascii=False, indent=2) + "\n```\n")
        w("```json\n" + json.dumps(od["sudreg_primjer_profila"], ensure_ascii=False, indent=2) + "\n```\n")

    # 6. rezultati natječaja
    w("## 6. Rezultati natječaja (tko je dobio novac) na data.gov.hr\n")
    ck = od.get("ckan", {})
    if ck.get("skupovi"):
        w("| Skup | Uloga | Izdavatelj | Ažurirano | Formati | Čitljivo | Redaka | OIB primatelja | Napomena |")
        w("|---|---|---|---|---|---|---:|---|---|")
        for s in ck["skupovi"]:
            if not s.get("postoji"):
                w(f"| `{s['id']}` | {s['uloga']} | - | - | - | ne postoji | | | |")
                continue
            p = s.get("profil", {})
            nap = s.get("napomena") or "; ".join(s.get("greske", [])[:1])
            if p.get("godine"):
                g = list(p["godine"])
                nap = (nap + "; " if nap else "") + f"godine {g[0]}-{g[-1]}"
            w(f"| {s['naslov'][:60]} | {s['uloga']} | {s.get('izdavatelj') or ''} | {s['azurirano']} | "
              f"{', '.join(f'{k} {v}' for k, v in s['formati'].items())} | "
              f"{'' if s.get('citljivo') is None else ('da' if s['citljivo'] else 'ne')} | {p.get('redaka', '')} | "
              f"{'da (' + str(p['razlicitih_oib']) + ' različitih)' if p.get('razlicitih_oib') else ''} | {nap} |")
        w("")
        w("Pretraga kataloga (broj skupova): " + ", ".join(f"„{q}\" {v['broj']}" for q, v in ck.get("pretrage", {}).items()) + ".\n")
    ur = od.get("udruge_rijeka", {})
    if ur.get("iznos_po_godini"):
        w(f"Primjer spoja: u skupu „Podaci o financiranju udruga\" (gradovi) Grad Rijeka ima {ur['zapisa']} isplata udrugama; "
          f"iznos po godinama: {ur['iznos_po_godini']}. Spaja se s natječajima po davatelju i godini (ne po ID-u natječaja, "
          "jer ga skup nema), a s Registrom udruga po OIB-u.\n")

    # 7. podudaranje
    w("## 7. Podudaranje: 3 testna profila\n")
    from match import PROFILI, load_profile, match
    for pf in PROFILI:
        try:
            p = load_profile(pf)
        except Exception as e:
            w(f"- {pf.name}: greška {e}")
            continue
        res = match(p, recs, top=8)
        w(f"### {p.get('naziv')}\n")
        w(f"Profil: tip `{p['tip']}`{' + ' + ', '.join(p.get('dodatni_tipovi', [])) if p.get('dodatni_tipovi') else ''}, "
          f"interesi {p.get('interesi')}, lokacija {p.get('lokacija')}"
          f"{', NKD ' + str(p.get('nkd')) + ' (iz Sudskog registra, ' + p.get('izvor_profila', '') + ')' if p.get('nkd') else ''}.\n")
        if not res:
            w("_Nema podudaranja._\n")
            continue
        w("| # | Natječaj | Izvor | Status | Rok | Zašto |")
        w("|---|---|---|---|---|---|")
        for i, r in enumerate(res, 1):
            w(f"| {i} | [{r['naziv'][:80]}]({r['url']}) | {r['izvor']} | {r['status'] or '?'} | {r['rok_prijave'] or '?'} | {'; '.join(r['razlozi'])} |")
        w("")

    # 8. zakoni i propisi
    pr = con.execute("SELECT COUNT(*), SUM(vrsta='zakon'), SUM(eli_meta), MIN(datum_objave), MAX(datum_objave) FROM propisi").fetchone()
    if pr[0]:
        w("## 8. Zakoni i propisi (Narodne novine, službeni dio)\n")
        w(f"{pr[0]} propisa ({pr[1]} zakona) objavljenih od {pr[3]} do {pr[4]}, s popisa službenog dijela NN-a; "
          f"službeni ELI metapodaci (datum donošenja, vrsta) dohvaćeni za {pr[2]} najnovijih zakona. "
          "Prikaz s filtrima: `web/zakoni.html`.\n")
        w("| Vrsta | Broj |")
        w("|---|---:|")
        for v in con.execute("SELECT vrsta, COUNT(*) n FROM propisi GROUP BY vrsta ORDER BY n DESC LIMIT 8"):
            w(f"| {v['vrsta']} | {v['n']} |")
        w("")

    (config.ROOT / "IZVJESTAJ.md").write_text("\n".join(L) + "\n", encoding="utf-8")
