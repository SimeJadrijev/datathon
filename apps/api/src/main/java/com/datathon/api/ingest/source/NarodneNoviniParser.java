package com.datathon.api.ingest.source;

import com.datathon.api.ingest.extract.CroatianDates;
import com.datathon.api.ingest.extract.KeywordClassifier;
import com.datathon.api.regulation.RegulationType;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

public final class NarodneNoviniParser {

    public record EliMetadata(LocalDate enactedAt, LocalDate publishedAt, RegulationType type) {
    }

    static final String BASE_URL = "https://narodne-novine.nn.hr";

    private static final Pattern GAZETTE_INFO = Pattern.compile(
            "NN\\s+(\\d+)/(\\d{4}),\\s*\\((\\d+)\\),\\s*([^,]+),\\s*([\\d.]+)");
    private static final String ELI_ONTOLOGY = "http://data.europa.eu/eli/ontology#";

    private NarodneNoviniParser() {
    }

    public static List<ParsedRegulation> parseListPage(String html) {
        Document document = Jsoup.parse(html, BASE_URL);
        List<ParsedRegulation> regulations = new ArrayList<>();
        for (Element entry : document.select(".searchListItem")) {
            Element link = entry.selectFirst(".resultTitle a");
            Element info = entry.selectFirst(".official-number-and-date");
            Matcher gazette = info == null ? null : GAZETTE_INFO.matcher(info.text());
            if (link != null && gazette != null && gazette.find()) {
                regulations.add(toRegulation(link, gazette));
            }
        }
        return regulations;
    }

    public static Optional<EliMetadata> parseEli(String html) {
        Document document = Jsoup.parse(html, BASE_URL);
        LocalDate enactedAt = date(document, "date_document");
        LocalDate publishedAt = date(document, "date_publication");
        RegulationType type = documentType(document);
        if (enactedAt == null && publishedAt == null && type == null) {
            return Optional.empty();
        }
        return Optional.of(new EliMetadata(enactedAt, publishedAt, type));
    }

    static RegulationType typeOf(String name) {
        String lower = name == null ? "" : name.strip().toLowerCase(Locale.ROOT);
        if (lower.startsWith("zakon")) {
            return RegulationType.LAW;
        }
        if (lower.startsWith("uredb")) {
            return RegulationType.DECREE;
        }
        if (lower.startsWith("pravilnik")) {
            return RegulationType.ORDINANCE;
        }
        return lower.startsWith("odluk") ? RegulationType.DECISION : RegulationType.OTHER;
    }

    private static ParsedRegulation toRegulation(Element link, Matcher gazette) {
        String number = gazette.group(1);
        String year = gazette.group(2);
        String document = gazette.group(3);
        String eli = BASE_URL + "/eli/sluzbeni/" + year + "/" + number + "/" + document;
        link.select("span.score").remove();
        String title = link.text().strip();
        return ParsedRegulation.builder()
                .externalId(year + "-" + number + "-" + document)
                .title(title)
                .type(typeOf(gazette.group(4)))
                .gazetteNumber(number + "/" + year)
                .documentNumber(Integer.valueOf(document))
                .publishedAt(CroatianDates.findFirst(gazette.group(5)).orElse(null))
                .url(link.absUrl("href"))
                .eliUrl(eli)
                .pdfUrl(eli + "/pdf")
                .areas(KeywordClassifier.areas(title, ""))
                .build();
    }

    private static LocalDate date(Document document, String property) {
        Element meta = document.selectFirst("meta[property=\"" + ELI_ONTOLOGY + property + "\"]");
        if (meta == null || meta.attr("content").isBlank()) {
            return null;
        }
        return LocalDate.parse(meta.attr("content").strip());
    }

    private static RegulationType documentType(Document document) {
        Element meta = document.selectFirst("meta[property=\"" + ELI_ONTOLOGY + "type_document\"]");
        if (meta == null || meta.attr("resource").isBlank()) {
            return null;
        }
        String resource = meta.attr("resource");
        return typeOf(resource.substring(resource.lastIndexOf('/') + 1));
    }
}
