package com.datathon.api.ingest.source;

import com.datathon.api.ingest.RawAttachment;
import com.datathon.api.ingest.extract.PdfTextExtractor;
import com.datathon.api.ingest.http.HttpFetchException;
import com.datathon.api.ingest.http.PoliteHttpClient;
import com.datathon.api.ingest.http.RobotsBlockedException;
import java.net.URI;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class OriginalCallReader {

    static final int MAX_PAGE_CHARS = 15_000;
    static final String PAGE_HEADER = "=== Izvorni natječaj ===\n";

    private static final String NOT_CONTENT = "script, style, noscript, nav, header, footer, aside, form, iframe";
    private static final String MAIN_CONTENT = "main, article, [role=main], #content, .content";

    private final PoliteHttpClient http;
    private final PdfTextExtractor pdfTextExtractor;

    public String read(RawAttachment link, String ownHost) {
        String url = withoutFragment(link.url());
        if (isOnHost(url, ownHost)) {
            return "";
        }
        RawAttachment target = new RawAttachment(link.name(), url);
        if (!PdfTextExtractor.pick(List.of(target)).isEmpty()) {
            return pdfTextExtractor.extractText(List.of(target));
        }
        if (DocumentLinks.isDocument(url)) {
            return "";
        }
        try {
            return pageText(Jsoup.parse(http.getText(url), url));
        } catch (RobotsBlockedException | HttpFetchException e) {
            log.warn("Skipping original call {}: {}", url, e.getMessage());
            return "";
        }
    }

    private String pageText(Document page) {
        List<RawAttachment> documents = DocumentLinks.in(page.body());
        page.select(NOT_CONTENT).remove();
        Element content = page.selectFirst(MAIN_CONTENT);
        String text = HtmlText.lines(content != null ? content : page.body());
        if (text.length() > MAX_PAGE_CHARS) {
            text = text.substring(0, MAX_PAGE_CHARS);
        }
        return PAGE_HEADER + text + "\n" + pdfTextExtractor.extractText(documents);
    }

    private static String withoutFragment(String url) {
        int hash = url.indexOf('#');
        return hash < 0 ? url : url.substring(0, hash);
    }

    private static boolean isOnHost(String url, String host) {
        try {
            String linkHost = URI.create(url).getHost();
            return linkHost != null && (linkHost.equals(host) || linkHost.endsWith("." + host));
        } catch (IllegalArgumentException e) {
            return true;
        }
    }
}
