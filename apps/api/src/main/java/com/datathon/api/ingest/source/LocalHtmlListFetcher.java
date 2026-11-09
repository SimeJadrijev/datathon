package com.datathon.api.ingest.source;

import com.datathon.api.ingest.RawAttachment;
import com.datathon.api.ingest.RawItem;
import com.datathon.api.ingest.SourceFetcher;
import com.datathon.api.ingest.extract.CallTitleFilter;
import com.datathon.api.ingest.extract.CroatianDates;
import com.datathon.api.ingest.http.HttpFetchException;
import com.datathon.api.ingest.http.PoliteHttpClient;
import com.datathon.api.ingest.http.RobotsBlockedException;
import com.datathon.api.source.SourceCode;
import java.time.DateTimeException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
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
public class LocalHtmlListFetcher implements SourceFetcher {

    public record HtmlListSite(LocalSite site, String itemSelector, String linkSelector, String titleSelector) {
    }

    static final int MAX_ITEMS_PER_SITE = 30;
    static final List<HtmlListSite> SITES = List.of(
            list("https://www.dubrovnik.hr/natjecaji", "Grad Dubrovnik", "Dubrovačko-neretvanska",
                    "div.news-item.post-item", "a[href]"),
            list("https://www.porec.hr/hr-hr/natjecaji", "Grad Poreč", "Istarska", "div.list-item-wrapper", "a[href]"),
            list("https://www.grad-zadar.hr/javni-natjecaji-93/", "Grad Zadar", "Zadarska", "div.col-sm-4", "h2 a[href]"),
            list("https://www.koprivnica.hr/javni-pozivi-i-natjecaji/", "Grad Koprivnica", "Koprivničko-križevačka",
                    "article.articles__listing_item", "a.card__link[href]"),
            list("https://www.bbz.hr/javni-pozivi-natjecaji/javni-pozivi", "Bjelovarsko-bilogorska županija",
                    "Bjelovarsko-bilogorska", "article.post", "h4 a[href]"),
            list("https://www.licko-senjska.hr/natjecaji-i-pozivi/zupanija/javni-pozivi-lsz", "Ličko-senjska županija",
                    "Ličko-senjska", "h2.entry-title-index", "a[href]"),
            list("https://www.istra-istria.hr/hr/natjecaji/zupanijske-potpore-i-dodjela-bespovratnih-sredstava/",
                    "Istarska županija", "Istarska", "div.entry", "h2 a[href]"),
            list("https://www.zadarska-zupanija.hr/natje%C4%8Daji/natje%C4%8Daji-u-tijeku", "Zadarska županija", "Zadarska",
                    "div.catItemView", "h3.catItemTitle a[href]"),
            list("https://www.bpz.hr/natje%C4%8Daji-i-javni-pozivi", "Brodsko-posavska županija", "Brodsko-posavska",
                    "div.catItemView", "h3.catItemTitle a[href]"),
            list("https://www.smz.hr/natj/javni", "Sisačko-moslavačka županija", "Sisačko-moslavačka",
                    "ul.mod-articlescategory li", "a.mod-articles-category-title[href]"));

    private static final Pattern LEADING_DATE = Pattern.compile(
            "^\\s*(\\d{4}-\\d{2}-\\d{2}|\\d{1,2}\\.\\s?\\d{1,2}\\.\\s?\\d{2,4}\\.?)\\s*");
    private static final Pattern READ_MORE = Pattern.compile("\\s*(pročitaj|saznaj|opširnije|više)( više)?\\s*$",
            Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);
    private static final Pattern ISO_DATE = Pattern.compile("\\b(\\d{4}-\\d{2}-\\d{2})\\b");
    private static final Pattern SHORT_MONTH_DATE = Pattern.compile(
            "\\b(\\d{1,2})\\.?\\s+(sij|velj|ožu|tra|svi|lip|srp|kol|ruj|lis|stu|pro)\\.?\\s+(\\d{4})\\b",
            Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);
    private static final List<String> SHORT_MONTHS = List.of("sij", "velj", "ožu", "tra", "svi", "lip", "srp", "kol",
            "ruj", "lis", "stu", "pro");
    private static final Pattern ID = Pattern.compile("/(\\d+)(?:[-/]|$)|/([^/?#]+?)(?:\\.html?|\\.aspx)?/?$");

    private final PoliteHttpClient http;
    private final CallTitleFilter callTitleFilter;
    private final List<HtmlListSite> sites;

    @Autowired
    public LocalHtmlListFetcher(PoliteHttpClient http, CallTitleFilter callTitleFilter) {
        this(http, callTitleFilter, SITES);
    }

    LocalHtmlListFetcher(PoliteHttpClient http, CallTitleFilter callTitleFilter, List<HtmlListSite> sites) {
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
        for (HtmlListSite site : sites) {
            try {
                parseList(http.getText(site.site().url()), site).stream()
                        .filter(item -> callTitleFilter.isCall(item.title()))
                        .forEach(items::add);
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

    static List<RawItem> parseList(String html, HtmlListSite listSite) {
        Document document = Jsoup.parse(html, listSite.site().url());
        List<RawItem> items = new ArrayList<>();
        for (Element entry : document.select(listSite.itemSelector())) {
            Element link = entry.is("a[href]") ? entry : entry.selectFirst(listSite.linkSelector());
            if (link == null || items.size() >= MAX_ITEMS_PER_SITE) {
                continue;
            }
            String url = link.absUrl("href").replaceFirst("#.*$", "");
            Element titleElement = listSite.titleSelector() == null ? link : entry.selectFirst(listSite.titleSelector());
            String title = cleanTitle(titleElement == null || titleElement.text().isBlank() ? link.attr("title") : titleElement.text());
            boolean isListPage = url.equals(listSite.site().url());
            String id = isListPage ? title : idOf(url);
            if (title.isEmpty() || isDuplicate(items, url, title, isListPage)) {
                continue;
            }
            RawItem.RawItemBuilder item = listSite.site().item(id)
                    .url(url)
                    .title(title)
                    .publishedAt(publishedAt(entry.text()))
                    .text(entry.text().strip());
            if (DocumentLinks.isDocument(url)) {
                item.attachments(List.of(new RawAttachment(title, url)));
            }
            items.add(item.build());
        }
        return items;
    }

    private static boolean isDuplicate(List<RawItem> items, String url, String title, boolean isListPage) {
        return items.stream().anyMatch(item -> item.title().equals(title) || (!isListPage && item.url().equals(url)));
    }

    private static String cleanTitle(String text) {
        String withoutDate = LEADING_DATE.matcher(text).replaceFirst("");
        return READ_MORE.matcher(withoutDate).replaceFirst("").strip();
    }

    private static LocalDate publishedAt(String text) {
        Matcher iso = ISO_DATE.matcher(text);
        if (iso.find()) {
            return IsoDates.leading(iso.group(1));
        }
        return CroatianDates.findFirst(text).orElseGet(() -> shortMonthDate(text));
    }

    static LocalDate shortMonthDate(String text) {
        Matcher matcher = SHORT_MONTH_DATE.matcher(text);
        if (!matcher.find()) {
            return null;
        }
        int month = SHORT_MONTHS.indexOf(matcher.group(2).toLowerCase(Locale.ROOT)) + 1;
        try {
            return LocalDate.of(Integer.parseInt(matcher.group(3)), month, Integer.parseInt(matcher.group(1)));
        } catch (DateTimeException e) {
            return null;
        }
    }

    private static String idOf(String url) {
        int query = url.indexOf('?');
        if (query >= 0) {
            return url.substring(url.lastIndexOf('/', query) + 1);
        }
        Matcher matcher = ID.matcher(url);
        if (!matcher.find()) {
            return url;
        }
        return matcher.group(1) != null ? matcher.group(1) : matcher.group(2);
    }

    private static HtmlListSite list(String url, String provider, String county, String itemSelector, String linkSelector) {
        return new HtmlListSite(new LocalSite(url, provider, county), itemSelector, linkSelector, null);
    }
}
