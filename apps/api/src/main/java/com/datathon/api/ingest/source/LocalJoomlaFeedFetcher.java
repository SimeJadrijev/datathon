package com.datathon.api.ingest.source;

import com.datathon.api.ingest.RawItem;
import com.datathon.api.ingest.SourceFetcher;
import com.datathon.api.ingest.extract.CallTitleFilter;
import com.datathon.api.ingest.http.HttpFetchException;
import com.datathon.api.ingest.http.PoliteHttpClient;
import com.datathon.api.ingest.http.RobotsBlockedException;
import com.datathon.api.source.SourceCode;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.parser.Parser;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class LocalJoomlaFeedFetcher implements SourceFetcher {

    static final String FEED_QUERY = "?format=feed&type=rss";
    static final List<LocalSite> SITES = List.of(
            new LocalSite("https://www.kckzz.hr/hr/svi-javni-pozivi-i-natjecaji", "Koprivničko-križevačka županija",
                    "Koprivničko-križevačka"),
            new LocalSite("https://www.obz.hr/index.php/potpore/natjecaji-i-javni-pozivi-u-tijeku-potpore",
                    "Osječko-baranjska županija", "Osječko-baranjska"),
            new LocalSite("https://www.pszupanija.hr/natjecaji", "Požeško-slavonska županija", "Požeško-slavonska"),
            new LocalSite("https://www.slavonski-brod.hr/gradska-uprava/javni-pozivi-natjecaji", "Grad Slavonski Brod",
                    "Brodsko-posavska"),
            new LocalSite("https://www.vukovar.hr/pozivi", "Grad Vukovar", "Vukovarsko-srijemska"));

    private static final Pattern ARTICLE_ID = Pattern.compile("/(\\d+)[-/]|[?&]id=(\\d+)|/([^/?#]+?)(?:\\.html)?/?$");

    private final PoliteHttpClient http;
    private final CallTitleFilter callTitleFilter;

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
        for (LocalSite site : SITES) {
            try {
                parseFeed(http.getText(site.url() + FEED_QUERY), site).stream()
                        .filter(item -> callTitleFilter.isCall(item.title()))
                        .forEach(items::add);
            } catch (HttpFetchException | RobotsBlockedException e) {
                log.warn("{}: feed failed: {}", site.url(), e.getMessage());
            }
        }
        return items;
    }

    @Override
    public RawItem fetchDetail(RawItem item) {
        return LocalPages.needsDetail(item) ? LocalPages.withDetail(http.getText(item.url()), item) : item;
    }

    static List<RawItem> parseFeed(String xml, LocalSite site) {
        Document feed = Jsoup.parse(xml, "", Parser.xmlParser());
        List<RawItem> items = new ArrayList<>();
        for (Element entry : feed.select("item")) {
            String url = entry.selectFirst("link") == null ? "" : entry.selectFirst("link").text().strip();
            Document description = Jsoup.parse(text(entry, "description"), url);
            items.add(site.item(articleId(url))
                    .url(url)
                    .title(Jsoup.parse(text(entry, "title")).text().strip())
                    .publishedAt(publishedAt(text(entry, "pubDate")))
                    .text(HtmlText.lines(description.body()))
                    .attachments(DocumentLinks.in(description.body()))
                    .build());
        }
        return items;
    }

    private static String articleId(String url) {
        Matcher matcher = ARTICLE_ID.matcher(url);
        if (!matcher.find()) {
            return url;
        }
        for (int group = 1; group <= matcher.groupCount(); group++) {
            if (matcher.group(group) != null) {
                return matcher.group(group);
            }
        }
        return url;
    }

    private static String text(Element entry, String tag) {
        Element element = entry.selectFirst(tag);
        return element == null ? "" : element.text();
    }

    private static LocalDate publishedAt(String pubDate) {
        try {
            return ZonedDateTime.parse(pubDate.strip(), DateTimeFormatter.RFC_1123_DATE_TIME)
                    .withZoneSameInstant(ZoneId.of("Europe/Zagreb")).toLocalDate();
        } catch (DateTimeParseException e) {
            return null;
        }
    }
}
