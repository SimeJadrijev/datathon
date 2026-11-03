package com.datathon.api.ingest.source;

import com.datathon.api.ingest.RawItem;
import com.datathon.api.ingest.SourceFetcher;
import com.datathon.api.ingest.extract.CallTitleFilter;
import com.datathon.api.ingest.http.HttpFetchException;
import com.datathon.api.ingest.http.PoliteHttpClient;
import com.datathon.api.ingest.http.RobotsBlockedException;
import com.datathon.api.ingest.source.LocalHtmlListFetcher.HtmlListSite;
import com.datathon.api.ingest.source.LocalWordPressFetcher.WordPressSite;
import com.datathon.api.source.SourceCode;
import java.time.Clock;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class UniversityFetcher implements SourceFetcher {

    static final int DAYS_BACK = 365;

    private static final String ZAGREB = "Grad Zagreb";
    private static final String RIJEKA = "Primorsko-goranska";

    static final List<WordPressSite> WORD_PRESS_SITES = List.of(
            wordPress("https://uniri.hr", "vijesti?kat-vijesti=323,729", "Sveučilište u Rijeci", RIJEKA),
            wordPress("https://www.unios.hr", "posts?categories=108,433,295,192", "Sveučilište J. J. Strossmayera u Osijeku",
                    "Osječko-baranjska"),
            wordPress("https://www.unidu.hr", "posts?categories=119,172,477", "Sveučilište u Dubrovniku", "Dubrovačko-neretvanska"),
            wordPress("https://www.unisb.hr", "posts?categories=68,99", "Sveučilište u Slavonskom Brodu", "Brodsko-posavska"),
            wordPress("https://szzg.unizg.hr", "posts?categories=3", "Studentski zbor Sveučilišta u Zagrebu", ZAGREB),
            wordPress("https://unizd-zbor.hr", "posts?categories=4", "Studentski zbor Sveučilišta u Zadru", "Zadarska"),
            wordPress("https://www.sczg.unizg.hr", "obavijesti?categories=139", "Studentski centar u Zagrebu", ZAGREB),
            wordPress("https://www.scri.uniri.hr", "posts?categories=81", "Studentski centar Rijeka", RIJEKA),
            wordPress("https://skc.uniri.hr", "posts", "Studentski kulturni centar Rijeka", RIJEKA),
            wordPress("https://www.tvz.hr", "posts?categories=1", "Tehničko veleučilište u Zagrebu", ZAGREB));

    static final List<LocalSite> FEEDS = List.of(
            new LocalSite("https://www.unin.hr/category/erasmus-natjecaji/feed/", "Sveučilište Sjever", "Varaždinska"),
            new LocalSite("https://www.scst.unist.hr/novosti/opcenito?format=feed&type=rss", "Studentski centar Split",
                    "Splitsko-dalmatinska"),
            new LocalSite("https://www.unipu.hr/feed/rss.php?url=/medunarodna-suradnja/erasmus_program/mobilnost_studenata/"
                    + "natjecaji&portlet=news", "Sveučilište Jurja Dobrile u Puli", "Istarska"),
            new LocalSite("https://www.unipu.hr/feed/rss.php?url=/studenti/stipendije/obavijesti&portlet=news",
                    "Sveučilište Jurja Dobrile u Puli", "Istarska"),
            new LocalSite("https://www.unipu.hr/feed/rss.php?url=/medunarodna-suradnja/obavijesti&portlet=news",
                    "Sveučilište Jurja Dobrile u Puli", "Istarska"));

    static final List<HtmlListSite> HTML_LISTS = List.of(
            html("https://www.unizd.hr/obavijesti/view/category/za-studente", "Sveučilište u Zadru", "Zadarska",
                    "div.edn-item article.c-item", "a.c-item__link-title", null),
            html("https://international.unizd.hr/erasmus-natjecaji-za-studente", "Sveučilište u Zadru", "Zadarska",
                    "div.edn-item article.c-item", "a.c-item__link-title", null),
            html("https://www.unizg.hr/nc/suradnja/medunarodna-razmjena/razmjena-studenata/natjecaji-za-studente/",
                    "Sveučilište u Zagrebu", ZAGREB, "div.news-list-item", "a[title]", null),
            html("https://www.unist.hr/aktivni-natjecaji-15888/15888", "Sveučilište u Splitu", "Splitsko-dalmatinska",
                    "div.col-12 > a.news-item", "a[href]", "h3"),
            html("https://sz.uniri.hr/p/articles", "Studentski zbor Sveučilišta u Rijeci", RIJEKA, "div.article-box", "a[href]",
                    "h2.article-title"),
            html("https://www.unicath.hr/erasmus/natjecaji", "Hrvatsko katoličko sveučilište", ZAGREB,
                    ".views-field-nothing .field-content > div.mb-3", "a[href]", "h3"),
            html("https://www.unipu.hr/studenti/studentski_zbor/natjecaji_i_javni_pozivi", "Studentski zbor Sveučilišta u Puli",
                    "Istarska", "div.news_article", ".news_title a", null));

    private final PoliteHttpClient http;
    private final CallTitleFilter callTitleFilter;
    private final Clock clock;

    @Autowired
    public UniversityFetcher(PoliteHttpClient http, CallTitleFilter callTitleFilter) {
        this(http, callTitleFilter, Clock.systemDefaultZone());
    }

    UniversityFetcher(PoliteHttpClient http, CallTitleFilter callTitleFilter, Clock clock) {
        this.http = http;
        this.callTitleFilter = callTitleFilter;
        this.clock = clock;
    }

    @Override
    public SourceCode code() {
        return SourceCode.UNIVERSITIES;
    }

    @Override
    public boolean needsTitleFilter() {
        return false;
    }

    @Override
    public List<RawItem> fetchList() {
        List<RawItem> items = new ArrayList<>();
        String after = LocalDate.now(clock).minusDays(DAYS_BACK) + "T00:00:00";
        for (WordPressSite site : WORD_PRESS_SITES) {
            collect(site.site(), items, () -> WordPressPaging.readPage(1,
                            page -> http.getJson(LocalWordPressFetcher.pageUrl(site, after, page)))
                    .map(posts -> LocalWordPressFetcher.parsePosts(posts, site.site()))
                    .orElse(List.of()));
        }
        for (LocalSite feed : FEEDS) {
            collect(feed, items, () -> LocalJoomlaFeedFetcher.parseFeed(http.getText(feed.url()).strip(), feed));
        }
        for (HtmlListSite list : HTML_LISTS) {
            collect(list.site(), items, () -> LocalHtmlListFetcher.parseList(http.getText(list.site().url()), list));
        }
        return items;
    }

    @Override
    public RawItem fetchDetail(RawItem item) {
        return LocalPages.needsDetail(item) ? LocalPages.withDetail(http.getText(item.url()), item) : item;
    }

    private void collect(LocalSite site, List<RawItem> items, Supplier<List<RawItem>> candidates) {
        try {
            candidates.get().stream()
                    .filter(item -> callTitleFilter.isStudentCall(item.title()))
                    .filter(item -> items.stream().noneMatch(found -> found.externalId().equals(item.externalId())))
                    .forEach(items::add);
        } catch (HttpFetchException | RobotsBlockedException e) {
            log.warn("{}: list failed: {}", site.url(), e.getMessage());
        }
    }

    private static WordPressSite wordPress(String url, String route, String provider, String county) {
        return new WordPressSite(new LocalSite(url, provider, county), route);
    }

    private static HtmlListSite html(String url, String provider, String county, String itemSelector, String linkSelector,
            String titleSelector) {
        return new HtmlListSite(new LocalSite(url, provider, county), itemSelector, linkSelector, titleSelector);
    }
}
