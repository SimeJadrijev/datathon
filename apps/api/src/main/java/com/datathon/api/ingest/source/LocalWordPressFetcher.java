package com.datathon.api.ingest.source;

import com.datathon.api.ingest.RawItem;
import com.datathon.api.ingest.SourceFetcher;
import com.datathon.api.ingest.extract.CallTitleFilter;
import com.datathon.api.ingest.http.HttpFetchException;
import com.datathon.api.ingest.http.PoliteHttpClient;
import com.datathon.api.ingest.http.RobotsBlockedException;
import com.datathon.api.source.SourceCode;
import java.time.Clock;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import tools.jackson.databind.JsonNode;

@Slf4j
@Component
public class LocalWordPressFetcher implements SourceFetcher {

    public record WordPressSite(LocalSite site, String route) {
    }

    static final int DAYS_BACK = 120;
    static final int MAX_PAGES = 3;
    static final int PAGE_SIZE = 50;
    static final List<WordPressSite> SITES = List.of(
            wordPress("https://www.vusz.hr", "posts?categories=139,138", "Vukovarsko-srijemska županija", "Vukovarsko-srijemska"),
            wordPress("https://www.bjelovar.hr", "posts?categories=2702,307", "Grad Bjelovar", "Bjelovarsko-bilogorska"),
            wordPress("https://www.bjelovar.hr", "lsvr_document?lsvr_document_cat=2774", "Grad Bjelovar", "Bjelovarsko-bilogorska"),
            wordPress("https://www.karlovac.hr", "natjecaji_pozivi", "Grad Karlovac", "Karlovačka"),
            wordPress("https://gorica.hr", "posts?categories=6,81,9", "Grad Velika Gorica", "Zagrebačka"),
            wordPress("https://www.osijek.hr", "natjecaji", "Grad Osijek", "Osječko-baranjska"),
            wordPress("https://www.cakovec.hr", "posts?categories=69,89", "Grad Čakovec", "Međimurska"),
            wordPress("https://www.zapresic.hr", "lsvr_document?lsvr_document_cat=106,122", "Grad Zaprešić", "Zagrebačka"),
            wordPress("https://www.kzz.hr", "natjecaj", "Krapinsko-zagorska županija", "Krapinsko-zagorska"),
            wordPress("https://www.kastela.hr", "natjecaji?vrsta-natjecaja=76,77", "Grad Kaštela", "Splitsko-dalmatinska"),
            wordPress("https://www.dnz.hr", "posts?categories=184,170", "Dubrovačko-neretvanska županija", "Dubrovačko-neretvanska"),
            wordPress("https://www.virovitica.hr", "posts?categories=8", "Grad Virovitica", "Virovitičko-podravska"),
            wordPress("https://www.solin.hr", "posts?categories=177,54", "Grad Solin", "Splitsko-dalmatinska"),
            wordPress("https://www.sisak.hr", "posts?categories=29", "Grad Sisak", "Sisačko-moslavačka"),
            wordPress("https://www.vpz.hr", "posts?categories=6,7", "Virovitičko-podravska županija", "Virovitičko-podravska"),
            wordPress("https://www.rovinj-rovigno.hr", "posts?categories=81,83", "Grad Rovinj", "Istarska"),
            wordPress("https://www.sinj.hr", "posts?categories=73", "Grad Sinj", "Splitsko-dalmatinska"),
            wordPress("https://pgz.hr", "natjecaj?vrsta_natjecaja=116", "Primorsko-goranska županija", "Primorsko-goranska"));

    private final PoliteHttpClient http;
    private final CallTitleFilter callTitleFilter;
    private final List<WordPressSite> sites;
    private final Clock clock;

    @Autowired
    public LocalWordPressFetcher(PoliteHttpClient http, CallTitleFilter callTitleFilter) {
        this(http, callTitleFilter, SITES, Clock.systemDefaultZone());
    }

    LocalWordPressFetcher(PoliteHttpClient http, CallTitleFilter callTitleFilter, List<WordPressSite> sites, Clock clock) {
        this.http = http;
        this.callTitleFilter = callTitleFilter;
        this.sites = sites;
        this.clock = clock;
    }

    @Override
    public SourceCode code() {
        return SourceCode.LOCAL_GOVERNMENT;
    }

    @Override
    public boolean needsTitleFilter() {
        return false;
    }

    @Override
    public List<RawItem> fetchList() {
        List<RawItem> items = new ArrayList<>();
        for (WordPressSite site : sites) {
            try {
                items.addAll(fetchSite(site));
            } catch (HttpFetchException | RobotsBlockedException e) {
                log.warn("{}: list failed: {}", site.site().url(), e.getMessage());
            }
        }
        return items;
    }

    @Override
    public RawItem fetchDetail(RawItem item) {
        return LocalPages.needsDetail(item) ? LocalPages.withDetail(http.getText(item.url()), item) : item;
    }

    private List<RawItem> fetchSite(WordPressSite site) {
        String after = LocalDate.now(clock).minusDays(DAYS_BACK) + "T00:00:00";
        List<RawItem> items = new ArrayList<>();
        for (int page = 1; page <= MAX_PAGES; page++) {
            Optional<JsonNode> posts = WordPressPaging.readPage(page, number -> http.getJson(pageUrl(site, after, number)));
            if (posts.isEmpty()) {
                break;
            }
            parsePosts(posts.get(), site.site()).stream()
                    .filter(item -> callTitleFilter.isCall(item.title()))
                    .forEach(items::add);
            if (posts.get().size() < PAGE_SIZE) {
                break;
            }
        }
        return items;
    }

    static String pageUrl(WordPressSite site, String after, int page) {
        String separator = site.route().contains("?") ? "&" : "?";
        return site.site().url() + "/wp-json/wp/v2/" + site.route() + separator + "after=" + after
                + "&per_page=" + PAGE_SIZE + "&page=" + page;
    }

    static List<RawItem> parsePosts(JsonNode posts, LocalSite site) {
        List<RawItem> items = new ArrayList<>();
        for (JsonNode post : posts) {
            String link = post.path("link").asString("");
            Document content = Jsoup.parse(post.path("content").path("rendered").asString(""), link);
            items.add(site.item(String.valueOf(post.path("id").asLong()))
                    .url(link)
                    .title(Jsoup.parse(post.path("title").path("rendered").asString("")).text().strip())
                    .publishedAt(IsoDates.leading(post.path("date").asString("")))
                    .text(HtmlText.lines(content.body()))
                    .attachments(DocumentLinks.in(content.body()))
                    .build());
        }
        return items;
    }

    private static WordPressSite wordPress(String url, String route, String provider, String county) {
        return new WordPressSite(new LocalSite(url, provider, county), route);
    }
}
