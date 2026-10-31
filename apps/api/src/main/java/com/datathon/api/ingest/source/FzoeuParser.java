package com.datathon.api.ingest.source;

import com.datathon.api.fundingcall.FundingCall;
import com.datathon.api.fundingcall.FundingCallStatus;
import com.datathon.api.ingest.RawAttachment;
import com.datathon.api.ingest.RawItem;
import com.datathon.api.ingest.extract.CroatianDates;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HexFormat;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

public final class FzoeuParser {

    static final String PAGE_URL = "https://www.fzoeu.hr/hr/nacionalni-javni-pozivi-i-natjecaji/1367";
    static final String PROVIDER = "Fond za zaštitu okoliša i energetsku učinkovitost";

    private static final Pattern DOCS_FOLDER = Pattern.compile("/docs/(\\d+)/");
    private static final Pattern CALL_CODE = Pattern.compile("\\(([A-Za-z]+-?\\d+/\\d+[^)]*)\\)");
    private static final Pattern OPENED = Pattern.compile("Otvoren:\\s*([\\d.\\s]+)");

    private FzoeuParser() {
    }

    public static List<RawItem> parseList(String html) {
        Document document = Jsoup.parse(html, PAGE_URL);
        List<RawItem> items = new ArrayList<>();
        for (Element header : document.select("li.accordion-header")) {
            Element body = header.nextElementSibling();
            if (body == null || !body.hasClass("accordion-hidden")) {
                continue;
            }
            items.add(item(header, body));
        }
        return items;
    }

    private static RawItem item(Element header, Element body) {
        String statusText = header.select(".status span").text().strip().toLowerCase(Locale.ROOT);
        String title = title(header, statusText);
        List<RawAttachment> attachments = DocumentLinks.in(body);
        String externalId = externalId(title, attachments);
        String text = body.wholeText().lines().map(String::strip).filter(line -> !line.isEmpty())
                .reduce((first, second) -> first + "\n" + second).orElse("");
        Matcher opened = OPENED.matcher(body.text());
        return RawItem.builder()
                .externalId(externalId)
                .url(PAGE_URL + "#" + externalId)
                .title(title)
                .provider(PROVIDER)
                .publishedAt(opened.find() ? CroatianDates.findFirst(opened.group(1)).orElse(null) : null)
                .statusHint(status(statusText))
                .countiesHint(Set.of(FundingCall.ALL_COUNTIES))
                .text(text)
                .attachments(attachments)
                .extra(Map.of("statusText", statusText))
                .build();
    }

    private static String title(Element header, String statusText) {
        String title = header.select("div > div").stream()
                .filter(div -> !div.hasClass("status") && div.selectFirst(".status") == null)
                .reduce((first, second) -> second)
                .map(Element::text)
                .orElse(header.text())
                .strip();
        return !statusText.isEmpty() && title.toLowerCase(Locale.ROOT).startsWith(statusText)
                ? title.substring(statusText.length()).strip()
                : title;
    }

    private static String externalId(String title, List<RawAttachment> attachments) {
        for (RawAttachment attachment : attachments) {
            Matcher folder = DOCS_FOLDER.matcher(attachment.url());
            if (folder.find()) {
                return folder.group(1);
            }
        }
        Matcher code = CALL_CODE.matcher(title);
        return code.find() ? code.group(1).replace('/', '_') : "title-" + sha1(title).substring(0, 12);
    }

    private static FundingCallStatus status(String statusText) {
        if (statusText.contains("otvoren")) {
            return FundingCallStatus.OPEN;
        }
        if (statusText.contains("najav")) {
            return FundingCallStatus.ANNOUNCED;
        }
        if (statusText.contains("zatvoren") || statusText.contains("obustav") || statusText.contains("završen")) {
            return FundingCallStatus.CLOSED;
        }
        return null;
    }

    private static String sha1(String value) {
        try {
            return HexFormat.of().formatHex(MessageDigest.getInstance("SHA-1").digest(value.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException(e);
        }
    }
}
