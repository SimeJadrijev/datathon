package com.datathon.api.ingest.source;

import com.datathon.api.ingest.RawItem;
import com.datathon.api.ingest.extract.CroatianDates;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

public final class GovHrNewsParser {

    public record Site(String baseUrl, String provider) {
    }

    static final String SEARCH_PATH = "/rezultati-pretrazivanja/49";
    static final int PAGE_SIZE = 10;

    private static final String STATIC_PAGE = "Stranica";
    private static final Pattern NEWS_ID = Pattern.compile("/(\\d+)/?$");

    private GovHrNewsParser() {
    }

    public static List<RawItem> parseSearchPage(String html, Site site) {
        Document document = Jsoup.parse(html, site.baseUrl());
        List<RawItem> items = new ArrayList<>();
        for (Element news : document.select(".news_item")) {
            Element link = news.selectFirst("a[href]");
            Element title = news.selectFirst(".h3");
            if (link == null || title == null) {
                continue;
            }
            String url = link.absUrl("href");
            Element date = news.selectFirst(".date");
            if (date != null && date.text().contains(STATIC_PAGE)) {
                continue;
            }
            items.add(RawItem.builder()
                    .externalId(externalId(site, url))
                    .url(url)
                    .title(title.text().strip())
                    .provider(site.provider())
                    .publishedAt(date == null ? null : CroatianDates.findFirst(date.text()).orElse(null))
                    .build());
        }
        return items;
    }

    public static boolean hasNextPage(String html, int page) {
        return html.contains("page=" + (page + 1));
    }

    public static RawItem parseDetail(String html, RawItem item) {
        Document document = Jsoup.parse(html, item.url());
        Element content = document.selectFirst(".page_content");
        if (content == null) {
            return item;
        }
        content.select("s, strike, del, script, style, .article_photo").remove();
        return item.toBuilder()
                .text(HtmlText.lines(content))
                .attachments(DocumentLinks.in(content))
                .build();
    }

    private static String externalId(Site site, String url) {
        var matcher = NEWS_ID.matcher(url);
        String id = matcher.find() ? matcher.group(1) : url;
        return site.baseUrl().replaceFirst("^https?://", "").replaceFirst("/$", "") + ":" + id;
    }
}
