package com.datathon.api.ingest.extract;

import com.datathon.api.ingest.RawAttachment;
import com.datathon.api.ingest.http.HttpFetchException;
import com.datathon.api.ingest.http.PoliteHttpClient;
import com.datathon.api.ingest.http.RobotsBlockedException;
import java.io.IOException;
import java.util.Comparator;
import java.util.List;
import java.util.regex.Pattern;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class PdfTextExtractor {

    static final int MAX_DOCUMENTS = 3;
    static final int MAX_PAGES = 8;

    private static final int FLAGS = Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE;
    private static final Pattern PDF = Pattern.compile("\\.pdf(\\?|$)", FLAGS);
    private static final Pattern LIKELY_CONDITIONS = Pattern.compile(
            "javni poziv|natje[čc]aj|poziv|uput|pravilnik|program", FLAGS);
    private static final Pattern LIKELY_FORM = Pattern.compile(
            "obrazac|izjava|prilog \\d|tablica|odluka o odabiru|rezultat|pitanja", FLAGS);

    private final PoliteHttpClient http;

    public String extractText(List<RawAttachment> attachments) {
        StringBuilder text = new StringBuilder();
        for (RawAttachment pdf : pick(attachments)) {
            try {
                text.append("=== ").append(pdf.name()).append(" ===\n").append(readText(http.getBytes(pdf.url()))).append('\n');
            } catch (RobotsBlockedException | HttpFetchException e) {
                log.warn("Skipping PDF {}: {}", pdf.url(), e.getMessage());
            }
        }
        return text.toString();
    }

    public static List<RawAttachment> pick(List<RawAttachment> attachments) {
        return attachments.stream()
                .filter(attachment -> PDF.matcher(attachment.url()).find())
                .sorted(Comparator
                        .comparing((RawAttachment attachment) -> !LIKELY_CONDITIONS.matcher(attachment.name()).find())
                        .thenComparing(attachment -> LIKELY_FORM.matcher(attachment.name()).find()))
                .limit(MAX_DOCUMENTS)
                .toList();
    }

    public static int pageCount(byte[] pdf) {
        try (PDDocument document = Loader.loadPDF(pdf)) {
            return document.getNumberOfPages();
        } catch (IOException e) {
            return 0;
        }
    }

    public static String readText(byte[] pdf) {
        try (PDDocument document = Loader.loadPDF(pdf)) {
            PDFTextStripper stripper = new PDFTextStripper();
            stripper.setEndPage(MAX_PAGES);
            return stripper.getText(document);
        } catch (IOException e) {
            log.warn("Unreadable PDF: {}", e.getMessage());
            return "";
        }
    }
}
