package com.datathon.api.ingest.source;

import com.datathon.api.fundingcall.FundingCall;
import com.datathon.api.fundingcall.FundingCallType;
import com.datathon.api.fundingcall.TargetGroup;
import com.datathon.api.ingest.RawItem;
import com.datathon.api.ingest.extract.CroatianDates;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

public final class MzomParser {

    static final String BASE_URL = "https://mzom.gov.hr";
    static final String PROVIDER = "Ministarstvo znanosti, obrazovanja i mladih";

    private static final Pattern ITEM_ID = Pattern.compile("/(\\d+)/?$");
    private static final Pattern DEADLINE_LABEL = Pattern.compile("^Rok:", Pattern.CASE_INSENSITIVE);
    private static final List<String> JOB_RELATED = List.of("zapošljavanj", "oglas", "nabav");
    private static final String SCHOLARSHIP = "stipendij";

    private MzomParser() {
    }

    public static List<RawItem> parseListPage(String html) {
        Document document = Jsoup.parse(html, BASE_URL);
        List<RawItem> items = new ArrayList<>();
        for (Element entry : document.select("div.news_item")) {
            Element link = entry.selectFirst("a[href]");
            Matcher id = link == null ? null : ITEM_ID.matcher(link.attr("href"));
            if (id != null && id.find()) {
                items.add(listItem(entry, link, id.group(1)));
            }
        }
        return items;
    }

    public static RawItem applyDetail(RawItem item, String html) {
        Document document = Jsoup.parse(html, BASE_URL);
        Element content = document.selectFirst(".page_content");
        Element article = document.selectFirst(".article_content");
        if (content == null || article == null) {
            return item;
        }
        RawItem.RawItemBuilder builder = item.toBuilder()
                .text(HtmlText.lines(content))
                .attachments(DocumentLinks.in(article));
        if (item.deadlineHint() == null) {
            Element heading = content.selectFirst("h3");
            String deadlineText = heading == null ? "" : heading.text().replace('\u00a0', ' ').strip();
            if (DEADLINE_LABEL.matcher(deadlineText).find()) {
                CroatianDates.findFirst(deadlineText).ifPresent(builder::deadlineHint);
            }
        }
        return builder.build();
    }

    public static boolean isJobRelated(RawItem item) {
        return item.categories().stream()
                .map(category -> category.toLowerCase(Locale.ROOT))
                .anyMatch(category -> JOB_RELATED.stream().anyMatch(category::contains));
    }

    private static RawItem listItem(Element entry, Element link, String id) {
        Element dateSpan = entry.selectFirst("span.date");
        List<String> parts = dateSpan == null ? List.of()
                : Arrays.stream(dateSpan.text().split("\\|")).map(String::strip).toList();
        String title = link.text().strip();
        boolean isScholarship = title.toLowerCase(Locale.ROOT).contains(SCHOLARSHIP)
                || parts.stream().anyMatch(part -> part.toLowerCase(Locale.ROOT).contains(SCHOLARSHIP));
        RawItem.RawItemBuilder builder = RawItem.builder()
                .externalId(id)
                .url(link.absUrl("href"))
                .title(title)
                .provider(PROVIDER)
                .publishedAt(parts.isEmpty() ? null : CroatianDates.findFirst(parts.getFirst()).orElse(null))
                .deadlineHint(deadline(entry))
                .categories(parts.size() > 1 ? parts.subList(1, parts.size()) : List.of())
                .countiesHint(Set.of(FundingCall.ALL_COUNTIES));
        if (isScholarship) {
            builder.typeHint(FundingCallType.SCHOLARSHIP).targetGroupsHint(Set.of(TargetGroup.STUDENT));
        }
        return builder.build();
    }

    private static LocalDate deadline(Element entry) {
        String text = entry.ownText().replace('\u00a0', ' ').strip();
        return DEADLINE_LABEL.matcher(text).find() ? CroatianDates.findFirst(text).orElse(null) : null;
    }
}
