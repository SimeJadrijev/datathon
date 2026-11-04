package com.datathon.api.ingest.source;

import com.datathon.api.ingest.RawItem;
import com.datathon.api.ingest.SourceFetcher;
import com.datathon.api.ingest.extract.CallTitleFilter;
import com.datathon.api.ingest.extract.CroatianDates;
import com.datathon.api.ingest.http.HttpFetchException;
import com.datathon.api.ingest.http.PoliteHttpClient;
import com.datathon.api.ingest.http.RobotsBlockedException;
import com.datathon.api.source.SourceCode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class LocalEasyDnnFetcher implements SourceFetcher {

    static final int MAX_PAGES = 3;
    static final List<LocalSite> SITES = List.of(
            new LocalSite("https://split.hr/natjecaji-i-oglasi", "Grad Split", "Splitsko-dalmatinska"),
            new LocalSite("https://www.dalmacija.hr/natjecaji/aktualni-natjecaji", "Splitsko-dalmatinska županija",
                    "Splitsko-dalmatinska"),
            new LocalSite("https://www.sibensko-kninska-zupanija.hr/pozivi-i-natjecaji", "Šibensko-kninska županija",
                    "Šibensko-kninska"));

    private static final Pattern EXCLUDED_CATEGORY = Pattern.compile("zapošljav|newsletter",
            Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);
    private static final Pattern ARTICLE_SLUG = Pattern.compile("/([^/?#]+)/?$");
    private static final Pattern NEXT_PAGE = Pattern.compile("/pgrid/(\\d+)/pageid/");

    private final PoliteHttpClient http;
    private final CallTitleFilter callTitleFilter;
    private final List<LocalSite> sites;

    @Autowired
    public LocalEasyDnnFetcher(PoliteHttpClient http, CallTitleFilter callTitleFilter) {
        this(http, callTitleFilter, SITES);
    }

    LocalEasyDnnFetcher(PoliteHttpClient http, CallTitleFilter callTitleFilter, List<LocalSite> sites) {
        this.http = http;
        this.callTitleFilter = callTitleFilter;
        this.sites = sites;
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
        for (LocalSite site : sites) {
            try {
                items.addAll(fetchSite(site));
            } catch (HttpFetchException | RobotsBlockedException e) {
                log.warn("{}: list failed: {}", site.url(), e.getMessage());
            }
        }
        return items;
    }

    @Override
    public RawItem fetchDetail(RawItem item) {
        return LocalPages.withDetail(http.getText(item.url()), item);
    }

    private List<RawItem> fetchSite(LocalSite site) {
        List<RawItem> items = new ArrayList<>();
        String html = http.getText(site.url());
        for (int page = 1; page <= MAX_PAGES; page++) {
            parseList(html, site).stream().filter(item -> callTitleFilter.isCall(item.title())).forEach(items::add);
            Optional<String> next = nextPageUrl(html, site, page + 1);
            if (next.isEmpty() || page == MAX_PAGES) {
                break;
            }
            html = http.getText(next.get());
        }
        return items;
    }

    static List<RawItem> parseList(String html, LocalSite site) {
        Document document = Jsoup.parse(html, site.url());
        List<RawItem> items = new ArrayList<>();
        for (Element article : document.select("article.l-item")) {
            Element link = article.selectFirst("a.o-link--overlay[href], .o-title a[href]");
            Element title = article.selectFirst(".o-title");
            List<String> categories = article.select(".o-category span").eachText();
            if (link == null || title == null || categories.stream().anyMatch(name -> EXCLUDED_CATEGORY.matcher(name).find())) {
                continue;
            }
            String url = link.absUrl("href");
            Matcher slug = ARTICLE_SLUG.matcher(url);
            items.add(site.item(slug.find() ? slug.group(1) : url)
                    .url(url)
                    .title(title.text().strip())
                    .publishedAt(publishedAt(article))
                    .categories(categories)
                    .text(String.join("\n", article.select(".prijave, .o-footer, .o-event, .o-button").eachText()))
                    .build());
        }
        return items;
    }

    static Optional<String> nextPageUrl(String html, LocalSite site, int page) {
        Document document = Jsoup.parse(html, site.url());
        for (Element link : document.select("a[href*=/pageid/" + page + "]")) {
            if (NEXT_PAGE.matcher(link.attr("href")).find()) {
                return Optional.of(link.absUrl("href"));
            }
        }
        return Optional.empty();
    }

    private static LocalDate publishedAt(Element article) {
        Element date = article.selectFirst(".o-date");
        if (date != null) {
            return CroatianDates.findFirst(date.text()).orElse(null);
        }
        List<String> parts = article.select(".c-item__left span").eachText();
        if (parts.size() == 3) {
            return CroatianDates.findFirst(parts.get(1) + " " + parts.get(0) + " " + parts.get(2)).orElse(null);
        }
        return null;
    }
}
