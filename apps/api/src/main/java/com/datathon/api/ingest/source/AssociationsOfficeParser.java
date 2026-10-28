package com.datathon.api.ingest.source;

import com.datathon.api.fundingcall.FundingCallStatus;
import com.datathon.api.fundingcall.TargetGroup;
import com.datathon.api.ingest.RawAttachment;
import com.datathon.api.ingest.RawItem;
import com.datathon.api.ingest.extract.AmountParser;
import com.datathon.api.ingest.extract.CroatianDates;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

public final class AssociationsOfficeParser {

    public record ListPage(List<RawItem> items, Integer totalOnSource) {
    }

    static final String BASE_URL = "https://udruge.gov.hr";
    static final String DETAIL_URL = BASE_URL + "/natjecaji-5503/5503?nid=";

    private static final Pattern NID = Pattern.compile("nid=(\\d+)");
    private static final Pattern TOTAL = Pattern.compile("Pronađeno (\\d+) rezultata");
    private static final String PROVIDER = "Institucija";
    private static final String COUNTY = "Područje provedbe aktivnosti (županija)";
    private static final String PUBLISHED = "Datum objave natječaja/poziva";
    private static final String CLOSING = "Datum zatvaranja natječaja/poziva";
    private static final String TOTAL_AMOUNT = "Ukupni iznos raspoloživih sredstava";
    private static final String MIN_AMOUNT = "Najniži iznos sredstava za financiranje programa/projekta";
    private static final String MAX_AMOUNT = "Najviši iznos sredstava za financiranje programa/projekta";
    private static final String ORIGINAL_LINK = "Poveznica na objavljeni natječaj/poziv";

    private AssociationsOfficeParser() {
    }

    public static ListPage parseListPage(String html) {
        Document document = Jsoup.parse(html, BASE_URL);
        List<RawItem> items = new ArrayList<>();
        for (Element entry : document.select("div.news_item")) {
            Element link = entry.selectFirst("a");
            Matcher nid = link == null ? null : NID.matcher(link.attr("href"));
            if (nid == null || !nid.find()) {
                continue;
            }
            items.add(listItem(entry, link, nid.group(1)));
        }
        Matcher total = TOTAL.matcher(document.text());
        return new ListPage(items, total.find() ? Integer.valueOf(total.group(1)) : null);
    }

    public static RawItem applyDetail(RawItem item, String html) {
        Document document = Jsoup.parse(html, BASE_URL);
        Map<String, Element> fields = detailFields(document);
        RawItem.RawItemBuilder builder = item.toBuilder()
                .text(detailText(fields))
                .targetGroupsHint(Set.of(TargetGroup.ASSOCIATION))
                .totalAmountHint(amount(fields, TOTAL_AMOUNT))
                .minAmountHint(amount(fields, MIN_AMOUNT))
                .maxAmountHint(amount(fields, MAX_AMOUNT))
                .countiesHint(counties(fields))
                .attachments(originalLink(fields));
        if (fields.containsKey(PROVIDER)) {
            builder.provider(fields.get(PROVIDER).text());
        }
        if (fields.containsKey(PUBLISHED)) {
            CroatianDates.findFirst(fields.get(PUBLISHED).text()).ifPresent(builder::publishedAt);
        }
        if (fields.containsKey(CLOSING)) {
            CroatianDates.findFirst(fields.get(CLOSING).text()).ifPresent(builder::deadlineHint);
        }
        return builder.build();
    }

    private static RawItem listItem(Element entry, Element link, String nid) {
        Element dateSpan = entry.selectFirst("span.date");
        List<String> parts = dateSpan == null ? List.of()
                : Arrays.stream(dateSpan.text().split("\\|")).map(String::strip).toList();
        String providerLevel = parts.size() > 1 ? parts.get(1) : null;
        return RawItem.builder()
                .externalId(nid)
                .url(DETAIL_URL + nid)
                .title(link.text().strip())
                .provider(entry.ownText().strip())
                .publishedAt(parts.isEmpty() ? null : CroatianDates.findFirst(parts.getFirst()).orElse(null))
                .statusHint(parts.size() > 2 ? status(parts.get(2)) : null)
                .categories(providerLevel == null ? List.of() : List.of(providerLevel))
                .extra(providerLevel == null ? Map.of() : Map.of("providerLevel", providerLevel))
                .build();
    }

    private static FundingCallStatus status(String text) {
        String lower = text.toLowerCase(Locale.ROOT);
        if (lower.contains("otvoren")) {
            return FundingCallStatus.OPEN;
        }
        return lower.contains("zatvoren") ? FundingCallStatus.CLOSED : null;
    }

    private static Map<String, Element> detailFields(Document document) {
        Map<String, Element> fields = new LinkedHashMap<>();
        for (Element row : document.select(".page_content table tr")) {
            if (row.childrenSize() < 2) {
                continue;
            }
            String label = row.child(0).text().strip().replaceAll(":$", "");
            fields.put(label, row.child(1));
        }
        return fields;
    }

    private static String detailText(Map<String, Element> fields) {
        return fields.entrySet().stream()
                .map(entry -> entry.getKey() + ": " + entry.getValue().text())
                .collect(Collectors.joining("\n"));
    }

    private static BigDecimal amount(Map<String, Element> fields, String label) {
        Element value = fields.get(label);
        return value == null ? null : AmountParser.parseStructured(value.text()).orElse(null);
    }

    private static Set<String> counties(Map<String, Element> fields) {
        Element value = fields.get(COUNTY);
        if (value == null) {
            return Set.of();
        }
        return Arrays.stream(value.text().split("[,;]"))
                .map(String::strip)
                .filter(county -> !county.isEmpty())
                .collect(Collectors.toSet());
    }

    private static List<RawAttachment> originalLink(Map<String, Element> fields) {
        Element value = fields.get(ORIGINAL_LINK);
        Element link = value == null ? null : value.selectFirst("a[href]");
        if (link == null || link.absUrl("href").isBlank()) {
            return List.of();
        }
        return List.of(new RawAttachment("Izvorni natječaj/poziv", link.absUrl("href")));
    }
}
