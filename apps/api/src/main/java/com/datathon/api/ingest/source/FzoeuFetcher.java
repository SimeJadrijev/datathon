package com.datathon.api.ingest.source;

import com.datathon.api.ingest.RawItem;
import com.datathon.api.ingest.SourceFetcher;
import com.datathon.api.ingest.extract.PdfTextExtractor;
import com.datathon.api.ingest.http.PoliteHttpClient;
import com.datathon.api.source.SourceCode;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class FzoeuFetcher implements SourceFetcher {

    private final PoliteHttpClient http;
    private final PdfTextExtractor pdfTextExtractor;

    @Override
    public SourceCode code() {
        return SourceCode.FZOEU;
    }

    @Override
    public List<RawItem> fetchList() {
        return FzoeuParser.parseList(http.getText(FzoeuParser.PAGE_URL));
    }

    @Override
    public RawItem fetchDetail(RawItem item) {
        return item.toBuilder().pdfText(pdfTextExtractor.extractText(item.attachments())).build();
    }
}
