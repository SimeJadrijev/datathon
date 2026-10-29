package com.datathon.api.ingest.source;

import com.datathon.api.fundingcall.FundingCall;
import com.datathon.api.fundingcall.TargetGroup;
import com.datathon.api.ingest.RawItem;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.parser.Parser;
import tools.jackson.databind.JsonNode;

public final class ApprrrParser {

    public record FeedEntry(String id, String title) {
    }

    static final String PROVIDER = "Agencija za plaćanja u poljoprivredi, ribarstvu i ruralnom razvoju";
    static final String INTERVENTION_PAGE_KEY = "interventionPage";

    private static final String CALLS_CATEGORY = "natjecaji";
    private static final Pattern INTERVENTION_PAGE = Pattern.compile("^https://www\\.apprrr\\.hr/(intervencije|mjere)/.+");
    private static final Pattern FEED_POST_ID = Pattern.compile("[?&]p=(\\d+)");

    private ApprrrParser() {
    }

    public static List<RawItem> parsePosts(JsonNode posts) {
        List<RawItem> items = new ArrayList<>();
        for (JsonNode post : posts) {
            items.add(parsePost(post));
        }
        return items;
    }

    public static RawItem parsePost(JsonNode post) {
        String link = post.path("link").asString();
        Element content = Jsoup.parse(post.path("content").path("rendered").asString(), link).body();
        Map<String, String> extra = interventionPage(content)
                .map(page -> Map.of(INTERVENTION_PAGE_KEY, page))
                .orElse(Map.of());
        return RawItem.builder()
                .externalId(String.valueOf(post.path("id").intValue()))
                .url(link)
                .title(Jsoup.parse(post.path("title").path("rendered").asString()).text().strip())
                .provider(PROVIDER)
                .publishedAt(LocalDate.parse(post.path("date").asString().substring(0, 10)))
                .categories(List.of(CALLS_CATEGORY))
                .countiesHint(Set.of(FundingCall.ALL_COUNTIES))
                .targetGroupsHint(Set.of(TargetGroup.FARMER))
                .text(HtmlText.lines(content))
                .attachments(DocumentLinks.in(content))
                .extra(extra)
                .build();
    }

    public static List<FeedEntry> parseFeed(String xml) {
        Document feed = Jsoup.parse(xml, "", Parser.xmlParser());
        List<FeedEntry> entries = new ArrayList<>();
        for (Element item : feed.select("item")) {
            Element guid = item.selectFirst("guid");
            Matcher id = guid == null ? null : FEED_POST_ID.matcher(guid.text());
            Element title = item.selectFirst("title");
            if (id != null && id.find() && title != null) {
                entries.add(new FeedEntry(id.group(1), title.text().strip()));
            }
        }
        return entries;
    }

    public static Optional<String> interventionPage(RawItem item) {
        return Optional.ofNullable(item.extra().get(INTERVENTION_PAGE_KEY));
    }

    /** The post only announces the call; the documents are listed on the page of the intervention it links to. */
    public static RawItem applyInterventionPage(RawItem item, String html) {
        if (!item.attachments().isEmpty()) {
            return item;
        }
        Document page = Jsoup.parse(html, interventionPage(item).orElse(ApprrrFetcher.BASE_URL));
        Element root = page.selectFirst("main, article, .entry-content");
        return item.toBuilder().attachments(DocumentLinks.in(root == null ? page.body() : root)).build();
    }

    private static Optional<String> interventionPage(Element content) {
        for (Element link : content.select("a[href]")) {
            String url = link.absUrl("href");
            if (INTERVENTION_PAGE.matcher(url).matches()) {
                return Optional.of(url);
            }
        }
        return Optional.empty();
    }
}
