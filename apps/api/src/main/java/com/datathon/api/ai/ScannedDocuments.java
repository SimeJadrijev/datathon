package com.datathon.api.ai;

import com.datathon.api.fundingcall.Attachment;
import com.datathon.api.fundingcall.FundingCall;
import com.datathon.api.ingest.extract.PdfTextExtractor;
import com.datathon.api.ingest.http.HttpFetchException;
import com.datathon.api.ingest.http.PoliteHttpClient;
import com.datathon.api.ingest.http.RobotsBlockedException;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class ScannedDocuments {

    static final int MAX_DOCUMENTS = 2;
    static final int MAX_BYTES = 5 * 1024 * 1024;
    static final int MAX_PAGES = 20;

    private static final Pattern EMPTY_SECTION = Pattern.compile("^=== (.+?) ===\\R\\s*(?=^=== |\\z)",
            Pattern.MULTILINE);

    private final PoliteHttpClient http;

    public List<String> urls(FundingCall call) {
        List<String> urls = new ArrayList<>();
        for (String name : emptySections(call.getSourceText())) {
            urlOf(call, name).ifPresent(urls::add);
        }
        return urls;
    }

    public List<byte[]> download(List<String> urls) {
        List<byte[]> documents = new ArrayList<>();
        for (String url : urls) {
            if (documents.size() == MAX_DOCUMENTS) {
                break;
            }
            downloadPdf(url).ifPresent(documents::add);
        }
        return documents;
    }

    static Set<String> emptySections(String sourceText) {
        Set<String> names = new LinkedHashSet<>();
        if (sourceText == null) {
            return names;
        }
        Matcher matcher = EMPTY_SECTION.matcher(sourceText);
        while (matcher.find()) {
            names.add(matcher.group(1));
        }
        return names;
    }

    private static Optional<String> urlOf(FundingCall call, String name) {
        return call.getAttachments().stream()
                .filter(attachment -> name.equals(attachment.getName()))
                .map(Attachment::getUrl)
                .findFirst();
    }

    private Optional<byte[]> downloadPdf(String url) {
        try {
            byte[] pdf = http.getBytes(url);
            int pages = PdfTextExtractor.pageCount(pdf);
            if (pdf.length > MAX_BYTES || pages == 0 || pages > MAX_PAGES) {
                log.info("Not sending scanned PDF {} to the model: {} bytes, {} pages", url, pdf.length, pages);
                return Optional.empty();
            }
            return Optional.of(pdf);
        } catch (RobotsBlockedException | HttpFetchException e) {
            log.warn("Skipping scanned PDF {}: {}", url, e.getMessage());
            return Optional.empty();
        }
    }
}
