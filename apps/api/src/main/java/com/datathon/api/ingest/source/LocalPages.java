package com.datathon.api.ingest.source;

import com.datathon.api.ingest.RawItem;
import java.util.List;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

final class LocalPages {

    static final int SHORT_TEXT_CHARS = 300;

    private static final List<String> CONTENT_SELECTORS = List.of(
            "#opis", ".c-body", ".entry-content", ".post-content", ".article-content", ".item-page", "[itemprop=articleBody]",
            ".itemFullText", "article", "main");

    private LocalPages() {
    }

    static RawItem withDetail(String html, RawItem item) {
        Document document = Jsoup.parse(html, item.url());
        Element content = mainContent(document);
        if (content == null) {
            return item;
        }
        content.select("script, style, nav, form, s, strike, del").remove();
        String text = HtmlText.lines(content);
        String listText = item.text().isBlank() ? "" : item.text() + "\n";
        return item.toBuilder()
                .text(listText + text)
                .attachments(item.attachments().isEmpty() ? DocumentLinks.in(content) : item.attachments())
                .build();
    }

    static boolean needsDetail(RawItem item) {
        return item.text().length() < SHORT_TEXT_CHARS && !DocumentLinks.isDocument(item.url());
    }

    private static Element mainContent(Document document) {
        for (String selector : CONTENT_SELECTORS) {
            Element content = document.selectFirst(selector);
            if (content != null && !content.text().isBlank()) {
                return content;
            }
        }
        return null;
    }
}
