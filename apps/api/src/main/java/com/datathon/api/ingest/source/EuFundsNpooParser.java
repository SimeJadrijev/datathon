package com.datathon.api.ingest.source;

import com.datathon.api.fundingcall.FundingCall;
import com.datathon.api.fundingcall.FundingCallType;
import com.datathon.api.fundingcall.TargetGroup;
import com.datathon.api.ingest.RawAttachment;
import com.datathon.api.ingest.RawItem;
import com.datathon.api.ingest.extract.AmountParser;
import com.datathon.api.ingest.extract.CroatianDates;
import com.datathon.api.ingest.extract.KeywordClassifier;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

public final class EuFundsNpooParser {

    static final String BASE_URL = "https://fondovieu.gov.hr";
    static final String PROVIDER = "Ministarstvo regionalnoga razvoja i fondova Europske unije (NPOO)";

    private static final Pattern CALL_PATH = Pattern.compile("^/pozivi/(\\d+)$");
    private static final String CALL_CODE = "Kod poziva";
    private static final String CALL_TYPE = "Tip natječaja";
    private static final String LIST_DEADLINE = "Rok za prijave";
    private static final String PUBLISHED = "Datum objave";
    private static final String DEADLINE = "Rok za podnošenje projektnih prijava";
    private static final String RESPONSIBLE_BODY = "Nadležno tijelo";
    private static final String TOTAL_AMOUNT = "Ukupna bespovratna sredstva";
    private static final String MIN_AMOUNT = "Minimalni iznos bespovratnih sredstava";
    private static final String MAX_AMOUNT = "Maksimalni iznos bespovratnih sredstava";
    private static final String ELIGIBLE_APPLICANTS = "Prihvatljivi prijavitelji";

    private EuFundsNpooParser() {
    }

    public static List<RawItem> parseListPage(String html) {
        Document document = Jsoup.parse(html, BASE_URL);
        List<RawItem> items = new ArrayList<>();
        for (Element row : document.select("table tr")) {
            Element link = row.selectFirst("a[href^=/pozivi/]");
            Matcher id = link == null ? null : CALL_PATH.matcher(link.attr("href"));
            if (id == null || !id.matches()) {
                continue;
            }
            items.add(listItem(row, id.group(1)));
        }
        return items;
    }

    public static RawItem applyDetail(RawItem item, String html) {
        Document document = Jsoup.parse(html, BASE_URL);
        Element main = document.selectFirst("main") != null ? document.selectFirst("main") : document.body();
        Map<String, String> fields = detailFields(main);
        List<String> applicants = eligibleApplicants(main);
        RawItem.RawItemBuilder builder = item.toBuilder()
                .text(detailText(fields, applicants, main))
                .targetGroupsHint(targetGroups(applicants))
                .totalAmountHint(amount(fields, TOTAL_AMOUNT))
                .minAmountHint(amount(fields, MIN_AMOUNT))
                .maxAmountHint(amount(fields, MAX_AMOUNT))
                .attachments(attachments(main));
        if (fields.containsKey(RESPONSIBLE_BODY)) {
            builder.provider(fields.get(RESPONSIBLE_BODY));
        }
        if (fields.containsKey(PUBLISHED)) {
            CroatianDates.findFirst(fields.get(PUBLISHED)).ifPresent(builder::publishedAt);
        }
        if (fields.containsKey(DEADLINE)) {
            CroatianDates.findFirst(fields.get(DEADLINE)).ifPresent(builder::deadlineHint);
        }
        return builder.build();
    }

    private static RawItem listItem(Element row, String id) {
        Element title = row.selectFirst("p.font-semibold");
        Map<String, String> labelled = new LinkedHashMap<>();
        for (Element paragraph : row.select("p:not(.font-semibold)")) {
            String[] parts = paragraph.text().split(":", 2);
            if (parts.length == 2) {
                labelled.put(parts[0].strip(), parts[1].strip());
            }
        }
        Map<String, String> extra = new LinkedHashMap<>();
        putIfPresent(extra, "callCode", labelled.get(CALL_CODE));
        putIfPresent(extra, "callType", labelled.get(CALL_TYPE));
        return RawItem.builder()
                .externalId(id)
                .url(BASE_URL + "/pozivi/" + id)
                .title(title == null ? "" : title.text().strip())
                .provider(PROVIDER)
                .typeHint(FundingCallType.PUBLIC_CALL)
                .deadlineHint(CroatianDates.findFirst(labelled.get(LIST_DEADLINE)).orElse(null))
                .countiesHint(Set.of(FundingCall.ALL_COUNTIES))
                .extra(extra)
                .build();
    }

    private static Map<String, String> detailFields(Element main) {
        Map<String, String> fields = new LinkedHashMap<>();
        for (Element paragraph : main.select("p:has(> strong)")) {
            String label = paragraph.selectFirst("strong").text().strip().replaceAll(":$", "");
            String value = paragraph.ownText().strip();
            if (!value.isEmpty()) {
                fields.putIfAbsent(label, value);
            }
        }
        return fields;
    }

    private static List<String> eligibleApplicants(Element main) {
        for (Element label : main.select("strong")) {
            if (label.text().startsWith(ELIGIBLE_APPLICANTS)) {
                return label.parent().select("li").eachText();
            }
        }
        return List.of();
    }

    private static Set<TargetGroup> targetGroups(List<String> applicants) {
        return KeywordClassifier.targetGroups(String.join("; ", applicants), "");
    }

    private static String detailText(Map<String, String> fields, List<String> applicants, Element main) {
        List<String> lines = new ArrayList<>();
        fields.forEach((label, value) -> lines.add(label + ": " + value));
        if (!applicants.isEmpty()) {
            lines.add(ELIGIBLE_APPLICANTS + ": " + String.join("; ", applicants));
        }
        for (Element section : main.select("section")) {
            Element body = section.selectFirst("div.v-html-element");
            Element heading = section.selectFirst("h2");
            if (body == null || heading == null) {
                continue;
            }
            lines.add(heading.text().strip());
            List<String> paragraphs = body.select("p").eachText();
            lines.addAll(paragraphs.isEmpty() ? List.of(body.text()) : paragraphs);
        }
        return String.join("\n", lines);
    }

    private static BigDecimal amount(Map<String, String> fields, String label) {
        String value = fields.get(label);
        if (value == null) {
            return null;
        }
        return AmountParser.parseStructured(value).filter(amount -> amount.signum() > 0).orElse(null);
    }

    private static List<RawAttachment> attachments(Element main) {
        Map<String, RawAttachment> byUrl = new LinkedHashMap<>();
        for (Element link : main.select("a[href*=/public/getDownloadUrl/]")) {
            String url = link.absUrl("href");
            if (!url.isBlank()) {
                byUrl.putIfAbsent(url, new RawAttachment(link.text().strip(), url));
            }
        }
        return new ArrayList<>(byUrl.values());
    }

    private static void putIfPresent(Map<String, String> map, String key, String value) {
        if (value != null && !value.isBlank()) {
            map.put(key, value);
        }
    }
}
