package com.datathon.api.ingest.source;

import com.datathon.api.ingest.RawAttachment;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import org.jsoup.nodes.Element;

final class DocumentLinks {

    private static final Pattern DOCUMENT = Pattern.compile("\\.(pdf|docx?|xlsx?|odt|zip)(\\?|$)",
            Pattern.CASE_INSENSITIVE);
    private static final int MAX_NAME_CHARS = 500;

    private DocumentLinks() {
    }

    static List<RawAttachment> in(Element root) {
        Map<String, RawAttachment> byUrl = new LinkedHashMap<>();
        for (Element link : root.select("a[href]")) {
            String url = link.absUrl("href");
            if (!url.isBlank() && DOCUMENT.matcher(url).find()) {
                byUrl.putIfAbsent(url, new RawAttachment(name(link, url), url));
            }
        }
        return new ArrayList<>(byUrl.values());
    }

    private static String name(Element link, String url) {
        String text = link.text().strip();
        String name = text.isEmpty() ? url.substring(url.lastIndexOf('/') + 1) : text;
        return name.length() > MAX_NAME_CHARS ? name.substring(0, MAX_NAME_CHARS) : name;
    }
}
