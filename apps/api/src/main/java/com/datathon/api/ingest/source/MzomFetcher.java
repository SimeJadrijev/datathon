package com.datathon.api.ingest.source;

import com.datathon.api.ingest.IngestProperties;
import com.datathon.api.ingest.RawItem;
import com.datathon.api.ingest.SourceFetcher;
import com.datathon.api.ingest.http.PoliteHttpClient;
import com.datathon.api.source.SourceCode;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class MzomFetcher implements SourceFetcher {

    private static final String LIST_URL = MzomParser.BASE_URL + "/istaknute-teme/natjecaji-196/196?trazi=1&page=%d";

    private final PoliteHttpClient http;
    private final IngestProperties properties;

    @Override
    public SourceCode code() {
        return SourceCode.MZOM;
    }

    @Override
    public List<RawItem> fetchList() {
        Map<String, RawItem> items = new LinkedHashMap<>();
        for (int page = 1; page <= properties.mzomPages(); page++) {
            List<RawItem> pageItems = MzomParser.parseListPage(http.getText(LIST_URL.formatted(page)));
            if (pageItems.isEmpty()) {
                break;
            }
            pageItems.stream()
                    .filter(item -> !MzomParser.isJobRelated(item))
                    .forEach(item -> items.putIfAbsent(item.externalId(), item));
        }
        return new ArrayList<>(items.values());
    }

    @Override
    public RawItem fetchDetail(RawItem item) {
        return MzomParser.applyDetail(item, http.getText(item.url()));
    }
}
