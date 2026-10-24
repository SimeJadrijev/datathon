/* Mini web za pregled POC podataka. Bez biblioteka; podaci su u data/*.js (window.POC_*). */
(function () {
  "use strict";

  var PAGE = 30;
  var today = new Date(); today.setHours(0, 0, 0, 0);

  var SKUPINE = { gradjanin: "Građani", student: "Studenti i učenici", obrt: "Obrti", doo: "Tvrtke",
    udruga: "Udruge", jls: "Gradovi, općine, ustanove", poljoprivrednik: "Poljoprivrednici" };
  var PODRUCJA = { okolis: "Okoliš", energetika: "Energetika", promet: "Promet", poljoprivreda: "Poljoprivreda",
    obrazovanje: "Obrazovanje", znanost: "Znanost", kultura: "Kultura", sport: "Sport", socijala: "Socijala",
    zdravstvo: "Zdravstvo", zaposljavanje: "Zapošljavanje", poduzetnistvo: "Poduzetništvo", inovacije: "Inovacije",
    digitalizacija: "Digitalizacija", turizam: "Turizam", mladi: "Mladi", civilno_drustvo: "Civilno društvo" };
  var STATUSI = [["otvoren", "Otvoreni"], ["najavljen", "Najavljeni"], ["svi_aktivni", "Otvoreni + nepoznat status"],
    ["zatvoren", "Zatvoreni"], ["svi", "Svi"]];

  // --- pomoćne -------------------------------------------------------------
  function esc(s) {
    return String(s == null ? "" : s).replace(/[&<>"']/g, function (c) {
      return { "&": "&amp;", "<": "&lt;", ">": "&gt;", '"': "&quot;", "'": "&#39;" }[c];
    });
  }
  function safeUrl(u) { return /^https?:\/\//i.test(u || "") ? u : "#"; }
  function $(id) { return document.getElementById(id); }
  function dateHr(iso) {
    if (!iso) return null;
    var p = iso.slice(0, 10).split("-");
    return +p[2] + ". " + +p[1] + ". " + p[0] + ".";
  }
  function daysTo(iso) {
    if (!iso) return null;
    var d = new Date(iso.slice(0, 10) + "T00:00:00");
    return Math.round((d - today) / 86400000);
  }
  function eur(v) {
    if (v == null) return null;
    return new Intl.NumberFormat("hr-HR", { maximumFractionDigits: 0 }).format(v) + " €";
  }
  function norm(s) {
    return (s || "").toLowerCase().normalize("NFD").replace(/[̀-ͯ]/g, "").replace(/đ/g, "d");
  }
  function chips(container, items, state, key, counts) {
    container.innerHTML = "";
    items.forEach(function (it) {
      var b = document.createElement("button");
      b.className = "chip-btn";
      b.type = "button";
      b.setAttribute("aria-pressed", state[key].has(it[0]) ? "true" : "false");
      b.innerHTML = esc(it[1]) + (counts ? '<span class="n">' + (counts[it[0]] || 0) + "</span>" : "");
      b.onclick = function () {
        if (state[key].has(it[0])) state[key].delete(it[0]); else state[key].add(it[0]);
        state.shown = PAGE; render();
      };
      container.appendChild(b);
    });
  }
  function radio(container, items, state, key) {
    container.innerHTML = "";
    items.forEach(function (it) {
      var b = document.createElement("button");
      b.className = "chip-btn";
      b.type = "button";
      b.setAttribute("aria-pressed", state[key] === it[0] ? "true" : "false");
      b.textContent = it[1];
      b.onclick = function () { state[key] = it[0]; state.shown = PAGE; render(); };
      container.appendChild(b);
    });
  }
  function stat(n, label) { return '<div class="stat"><b>' + n + "</b><span>" + esc(label) + "</span></div>"; }
  function setupMobileToggle() {
    var t = $("filters-toggle"), f = $("filters");
    t.onclick = function () {
      var open = f.classList.toggle("open");
      t.setAttribute("aria-expanded", open ? "true" : "false");
      t.textContent = open ? "Sakrij filtre" : "Filtri";
    };
  }

  var render = function () {};

  // --- natječaji ------------------------------------------------------------
  function initNatjecaji() {
    var D = window.POC_NATJECAJI;
    if (!D) { $("list").innerHTML = '<p class="empty">Nema podataka: pokreni <code>python run_all.py</code> ili <code>python web/build.py</code>.</p>'; return; }
    var all = D.natjecaji;
    var izvorNaziv = {};
    D.izvori.forEach(function (i) { izvorNaziv[i.izvor] = i.naziv; });
    all.forEach(function (n) {
      n._q = norm([n.naziv, n.davatelj, n.uvjeti, (n.zupanije || []).join(" ")].join(" "));
      n._dana = daysTo(n.rok);
    });

    // pokrivenost
    var naIzvoru = D.izvori.reduce(function (s, i) { return s + (i.na_izvoru || i.u_bazi); }, 0);
    $("cov-summary").textContent = "· " + all.length + " u bazi iz " + D.izvori.length + " izvora (na izvorima ukupno ~" +
      new Intl.NumberFormat("hr-HR").format(naIzvoru) + ")";
    $("cov-table").innerHTML = "<tr><th>Izvor</th><th>Način dohvata</th><th class=num>U bazi</th><th class=num>Na izvoru</th>" +
      "<th class=num>S punim tekstom</th><th class=num>AI obrađeno</th><th>Napomena</th></tr>" +
      D.izvori.map(function (i) {
        return "<tr><td>" + esc(i.naziv) + "</td><td>" + esc(i.metoda || "") + "</td><td class=num>" + i.u_bazi +
          "</td><td class=num>" + (i.na_izvoru || "cijeli popis") + "</td><td class=num>" + i.s_detaljima +
          "</td><td class=num>" + i.llm + "</td><td>" + esc(i.problemi) + "</td></tr>";
      }).join("");

    var state = { q: "", status: "otvoren", skupine: new Set(), podrucja: new Set(), vrsta: new Set(),
      izvor: new Set(), zupanija: "", rok: false, iznos: false, sort: "rok", shown: PAGE };

    function matches(n, skip) {
      if (skip !== "status") {
        if (state.status === "svi_aktivni" && !(n.status === "otvoren" || n.status === "najavljen" || n.status == null)) return false;
        if (state.status === "otvoren" || state.status === "najavljen" || state.status === "zatvoren") {
          if (n.status !== state.status) return false;
        }
        // "otvoren" s rokom u prošlosti nije otvoren (izvor nije ažuriran)
        if ((state.status === "otvoren" || state.status === "svi_aktivni") && n._dana != null && n._dana < 0) return false;
      }
      if (state.q && n._q.indexOf(norm(state.q)) < 0) return false;
      if (skip !== "skupine" && state.skupine.size && !n.skupine.some(function (s) { return state.skupine.has(s); })) return false;
      if (skip !== "podrucja" && state.podrucja.size && !n.podrucja.some(function (s) { return state.podrucja.has(s); })) return false;
      if (skip !== "vrsta" && state.vrsta.size && !state.vrsta.has(n.vrsta)) return false;
      if (skip !== "izvor" && state.izvor.size && !state.izvor.has(n.izvor)) return false;
      if (state.zupanija && !(n.zupanije.indexOf(state.zupanija) >= 0 || n.zupanije.indexOf("sve") >= 0)) return false;
      if (state.rok && !n.rok) return false;
      if (state.iznos && n.max == null && n.ukupno == null) return false;
      return true;
    }
    function countBy(skip, field) {
      var c = {};
      all.forEach(function (n) {
        if (!matches(n, skip)) return;
        var v = n[field];
        (Array.isArray(v) ? v : [v]).forEach(function (x) { if (x != null) c[x] = (c[x] || 0) + 1; });
      });
      return c;
    }

    // statične opcije filtara
    var zup = {};
    all.forEach(function (n) { n.zupanije.forEach(function (z) { if (z !== "sve") zup[z] = 1; }); });
    $("f-zupanija").innerHTML = '<option value="">Sve (uz nacionalne pozive)</option>' +
      Object.keys(zup).sort().map(function (z) { return "<option>" + esc(z) + "</option>"; }).join("");
    $("f-zupanija").onchange = function (e) { state.zupanija = e.target.value; state.shown = PAGE; render(); };
    $("q").oninput = function (e) { state.q = e.target.value.trim(); state.shown = PAGE; render(); };
    $("f-rok").onchange = function (e) { state.rok = e.target.checked; state.shown = PAGE; render(); };
    $("f-iznos").onchange = function (e) { state.iznos = e.target.checked; state.shown = PAGE; render(); };
    $("sort").onchange = function (e) { state.sort = e.target.value; render(); };
    $("more").onclick = function () { state.shown += PAGE; render(); };
    $("reset").onclick = function () {
      state.q = ""; state.status = "otvoren"; state.zupanija = ""; state.rok = state.iznos = false;
      ["skupine", "podrucja", "vrsta", "izvor"].forEach(function (k) { state[k].clear(); });
      $("q").value = ""; $("f-zupanija").value = ""; $("f-rok").checked = $("f-iznos").checked = false;
      state.shown = PAGE; render();
    };
    setupMobileToggle();

    // brojke na vrhu (ne ovise o filtrima)
    var otvoreni = all.filter(function (n) { return n.status === "otvoren" && !(n._dana != null && n._dana < 0); });
    var uskoro = otvoreni.filter(function (n) { return n._dana != null && n._dana <= 14; }).length;
    var tjedan = all.filter(function (n) { var d = daysTo(n.objava); return d != null && d >= -7 && d <= 0; }).length;
    $("stats").innerHTML = stat(otvoreni.length, "otvorenih natječaja") + stat(uskoro, "otvorenih s rokom u 14 dana") +
      stat(tjedan, "objavljeno u zadnjih 7 dana") + stat(all.filter(function (n) { return n.llm; }).length, "s AI sažetkom uvjeta");
    $("foot").textContent = "Podaci generirani " + D.generirano.replace("T", " u ") + ". POC za Datathon Portala otvorenih podataka 2026; nije službena usluga.";

    function sortFn(a, b) {
      if (state.sort === "novo") return (b.objava || "").localeCompare(a.objava || "");
      if (state.sort === "iznos") return (b.max || b.ukupno || 0) - (a.max || a.ukupno || 0);
      var ra = a._dana != null && a._dana >= 0 ? a._dana : 1e6, rb = b._dana != null && b._dana >= 0 ? b._dana : 1e6;
      return ra - rb || (b.objava || "").localeCompare(a.objava || "");
    }

    function card(n) {
      var st = n.status || "nepoznat";
      var rok = "";
      if (n.rok) {
        var d = n._dana;
        var rel = d == null ? "" : d < 0 ? " (prošao)" : d === 0 ? " (danas!)" : " (još " + d + (d === 1 ? " dan" : " dana") + ")";
        rok = '<span><span class="k">Rok:</span> <span class="' + (d != null && d >= 0 && d <= 14 ? "soon" : "") + '">' + dateHr(n.rok) + rel + "</span></span>";
      } else {
        rok = '<span><span class="k">Rok:</span> nije naveden</span>';
      }
      var iznos = "";
      if (n.max != null) iznos += '<span><span class="k">Po korisniku:</span> ' + (n.min != null ? eur(n.min) + " – " : "do ") + eur(n.max) + "</span>";
      if (n.ukupno != null) iznos += '<span><span class="k">Ukupno:</span> ' + eur(n.ukupno) + "</span>";
      var tags = n.skupine.map(function (s) { return '<span class="tag grp">' + esc(SKUPINE[s] || s) + "</span>"; })
        .concat(n.podrucja.slice(0, 4).map(function (s) { return '<span class="tag">' + esc(PODRUCJA[s] || s) + "</span>"; }));
      var zup = n.zupanije.filter(function (z) { return z !== "sve"; });
      var more = "";
      if (n.uvjeti || n.dokumenti.length || n.prilozi.length) {
        more = '<details class="more"><summary>Uvjeti, dokumenti i prilozi</summary>' +
          (n.uvjeti ? '<h4>Tko se može prijaviti <span class="badge ai">AI</span></h4><p style="margin:0">' + esc(n.uvjeti) + "</p>" : "") +
          (n.dokumenti.length ? '<h4>Potrebni dokumenti <span class="badge ai">AI</span></h4><ul>' +
            n.dokumenti.map(function (d) { return "<li>" + esc(d) + "</li>"; }).join("") + "</ul>" : "") +
          (n.prilozi.length ? "<h4>Prilozi na izvoru</h4><ul>" + n.prilozi.map(function (p) {
            return '<li><a href="' + esc(safeUrl(p.url)) + '" target="_blank" rel="noopener">' + esc(p.naziv) + "</a></li>"; }).join("") + "</ul>" : "") +
          (n.llm ? '<p class="ai-note">Rok, iznosi i skupine dopunjeni AI-jem iz teksta i PDF-a natječaja; provjerite na izvoru.</p>' : "") +
          "</details>";
      }
      return '<article class="card"><div class="card-head"><span class="badge ' + esc(st) + '">' + esc(st === "nepoznat" ? "status nepoznat" : st) +
        '</span><span class="src">' + esc(izvorNaziv[n.izvor] || n.izvor) + "</span>" + (n.vrsta ? "<span>· " + esc(n.vrsta) + "</span>" : "") +
        (n.objava ? "<span>· objavljeno " + dateHr(n.objava) + "</span>" : "") + (n.llm ? '<span class="badge ai">AI</span>' : "") + "</div>" +
        '<h2><a href="' + esc(safeUrl(n.url)) + '" target="_blank" rel="noopener">' + esc(n.naziv) + "</a></h2>" +
        '<div class="who">' + esc(n.davatelj || "") + (zup.length ? " · " + esc(zup.join(", ")) : "") + "</div>" +
        '<div class="facts">' + rok + iznos + "</div>" + (tags.length ? '<div class="tags">' + tags.join("") + "</div>" : "") +
        more + "</article>";
    }

    render = function () {
      radio($("f-status"), STATUSI, state, "status");
      chips($("f-skupine"), Object.keys(SKUPINE).map(function (k) { return [k, SKUPINE[k]]; }), state, "skupine", countBy("skupine", "skupine"));
      var pc = countBy("podrucja", "podrucja");
      chips($("f-podrucja"), Object.keys(PODRUCJA).filter(function (k) { return pc[k] || state.podrucja.has(k); })
        .map(function (k) { return [k, PODRUCJA[k]]; }), state, "podrucja", pc);
      var vc = countBy("vrsta", "vrsta");
      chips($("f-vrsta"), Object.keys(vc).sort().map(function (k) { return [k, k]; }), state, "vrsta", vc);
      var ic = countBy("izvor", "izvor");
      var fi = $("f-izvor");
      fi.innerHTML = "";
      D.izvori.forEach(function (i) {
        var l = document.createElement("label");
        l.className = "check";
        l.innerHTML = '<input type="checkbox"' + (state.izvor.has(i.izvor) ? " checked" : "") + "> " + esc(i.naziv) +
          '<span class="n">' + (ic[i.izvor] || 0) + "</span>";
        l.querySelector("input").onchange = function (e) {
          if (e.target.checked) state.izvor.add(i.izvor); else state.izvor.delete(i.izvor);
          state.shown = PAGE; render();
        };
        fi.appendChild(l);
      });

      var res = all.filter(function (n) { return matches(n); }).sort(sortFn);
      $("count").textContent = res.length + (res.length === 1 ? " natječaj" : " natječaja");
      $("list").innerHTML = res.length ? res.slice(0, state.shown).map(card).join("") :
        '<p class="empty">Nema natječaja za odabrane filtre. Probajte status „Svi" ili maknite neki filtar.</p>';
      $("more").hidden = res.length <= state.shown;
      $("more").textContent = "Prikaži još (" + (res.length - state.shown) + ")";
    };
    render();
  }

  // --- zakoni i propisi -----------------------------------------------------
  function initPropisi() {
    var D = window.POC_PROPISI;
    if (!D || !D.propisi.length) { $("list").innerHTML = '<p class="empty">Nema podataka: pokreni <code>python run_all.py</code>.</p>'; return; }
    var all = D.propisi;
    all.forEach(function (p) { p._q = norm(p.naziv + " " + p.nn); p._d = daysTo(p.objava); });
    var state = { q: "", vrsta: new Set(["zakon"]), podrucja: new Set(), razdoblje: "30", shown: PAGE };
    var VRSTE_RED = ["zakon", "uredba", "pravilnik", "odluka", "naredba", "uputa", "rješenje", "presuda", "statut", "ostalo"];

    function matches(p, skip) {
      if (state.q && p._q.indexOf(norm(state.q)) < 0) return false;
      if (skip !== "vrsta" && state.vrsta.size && !state.vrsta.has(p.vrsta)) return false;
      if (skip !== "podrucja" && state.podrucja.size && !p.podrucja.some(function (s) { return state.podrucja.has(s); })) return false;
      var r = +state.razdoblje;
      if (r && (p._d == null || -p._d > r)) return false;
      return true;
    }
    function countBy(skip, field) {
      var c = {};
      all.forEach(function (p) {
        if (!matches(p, skip)) return;
        var v = p[field];
        (Array.isArray(v) ? v : [v]).forEach(function (x) { if (x) c[x] = (c[x] || 0) + 1; });
      });
      return c;
    }
    var godine = {};
    all.forEach(function (p) { if (p.objava) godine[p.objava.slice(0, 4)] = 1; });
    $("cov-summary").textContent = "· " + all.length + " propisa iz " + Object.keys(godine).sort().join(" i ") + ". godine";
    var zakoni = all.filter(function (p) { return p.vrsta === "zakon"; });
    $("stats").innerHTML = stat(zakoni.filter(function (p) { return p._d != null && p._d >= -30; }).length, "novih zakona u 30 dana") +
      stat(all.filter(function (p) { return p._d != null && p._d >= -30; }).length, "svih propisa u 30 dana") +
      stat(zakoni.length, "zakona u bazi") + stat(all.length, "propisa u bazi");
    $("foot").textContent = "Podaci generirani " + D.generirano.replace("T", " u ") + ". Izvor: Narodne novine, službeni dio.";

    $("q").oninput = function (e) { state.q = e.target.value.trim(); state.shown = PAGE; render(); };
    $("f-razdoblje").onchange = function (e) { state.razdoblje = e.target.value; state.shown = PAGE; render(); };
    $("more").onclick = function () { state.shown += PAGE; render(); };
    $("reset").onclick = function () {
      state.q = ""; state.vrsta = new Set(["zakon"]); state.podrucja.clear(); state.razdoblje = "30";
      $("q").value = ""; $("f-razdoblje").value = "30"; state.shown = PAGE; render();
    };
    setupMobileToggle();

    function card(p) {
      return '<article class="card"><div class="card-head"><span class="badge ' + (p.vrsta === "zakon" ? "otvoren" : "zatvoren") + '">' + esc(p.vrsta) +
        "</span><span class=src>NN " + esc(p.nn) + "</span><span>· objavljeno " + (dateHr(p.objava) || "?") + "</span>" +
        (p.doneseno ? "<span>· doneseno " + dateHr(p.doneseno) + "</span>" : "") + "</div>" +
        '<h2><a href="' + esc(safeUrl(p.url)) + '" target="_blank" rel="noopener">' + esc(p.naziv) + "</a></h2>" +
        '<div class="facts"><a href="' + esc(safeUrl(p.pdf)) + '" target="_blank" rel="noopener">PDF</a>' +
        '<a href="' + esc(safeUrl(p.eli)) + '" target="_blank" rel="noopener">ELI identifikator</a></div>' +
        (p.podrucja.length ? '<div class="tags">' + p.podrucja.map(function (s) { return '<span class="tag">' + esc(PODRUCJA[s] || s) + "</span>"; }).join("") + "</div>" : "") +
        "</article>";
    }

    render = function () {
      var vc = countBy("vrsta", "vrsta");
      var vrste = VRSTE_RED.filter(function (v) { return vc[v] || state.vrsta.has(v); })
        .concat(Object.keys(vc).filter(function (v) { return VRSTE_RED.indexOf(v) < 0; }).sort());
      chips($("f-vrsta"), vrste.map(function (v) { return [v, v.charAt(0).toUpperCase() + v.slice(1)]; }), state, "vrsta", vc);
      var pc = countBy("podrucja", "podrucja");
      chips($("f-podrucja"), Object.keys(PODRUCJA).filter(function (k) { return pc[k] || state.podrucja.has(k); })
        .map(function (k) { return [k, PODRUCJA[k]]; }), state, "podrucja", pc);
      var res = all.filter(function (p) { return matches(p); });
      $("count").textContent = res.length + " propisa, najnoviji prvi";
      $("list").innerHTML = res.length ? res.slice(0, state.shown).map(card).join("") :
        '<p class="empty">Nema propisa za odabrane filtre. Probajte duže razdoblje ili drugu vrstu.</p>';
      $("more").hidden = res.length <= state.shown;
      $("more").textContent = "Prikaži još (" + (res.length - state.shown) + ")";
    };
    render();
  }

  if (document.body.dataset.page === "natjecaji") initNatjecaji(); else initPropisi();
})();
