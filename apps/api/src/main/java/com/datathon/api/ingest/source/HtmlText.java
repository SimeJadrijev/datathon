package com.datathon.api.ingest.source;

import java.util.stream.Collectors;
import org.jsoup.nodes.Element;

final class HtmlText {

    private static final String BLOCK_ELEMENTS = "p, li, div, h1, h2, h3, h4, h5, h6, tr, section";

    private HtmlText() {
    }

    static String lines(Element element) {
        Element copy = element.clone();
        copy.select("br").append("\n");
        copy.select(BLOCK_ELEMENTS).append("\n");
        return copy.wholeText().replace('\u00a0', ' ').lines()
                .map(String::strip)
                .filter(line -> !line.isEmpty())
                .collect(Collectors.joining("\n"));
    }
}
