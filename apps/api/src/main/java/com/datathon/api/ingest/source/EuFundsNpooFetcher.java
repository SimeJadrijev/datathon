package com.datathon.api.ingest.source;

import com.datathon.api.fundingcall.FundingCallStatus;
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
public class EuFundsNpooFetcher implements SourceFetcher {

    private static final String LIST_URL = EuFundsNpooParser.BASE_URL + "/pozivi?page=%d&statusGroup=%s";
    private static final String OPEN_GROUP = "otvoreni";
    private static final String CLOSED_GROUP = "zatvoreni";
    private static final int MAX_OPEN_PAGES = 30;

    private final PoliteHttpClient http;
    private final IngestProperties properties;

    @Override
    public SourceCode code() {
        return SourceCode.EU_FUNDS_NPOO;
    }

    @Override
    public boolean needsTitleFilter() {
        return false;
    }

    @Override
    public List<RawItem> fetchList() {
        Map<String, RawItem> items = new LinkedHashMap<>();
        collect(OPEN_GROUP, FundingCallStatus.OPEN, MAX_OPEN_PAGES, items);
        collect(CLOSED_GROUP, FundingCallStatus.CLOSED, properties.closedPages(), items);
        return new ArrayList<>(items.values());
    }

    @Override
    public RawItem fetchDetail(RawItem item) {
        return EuFundsNpooParser.applyDetail(item, http.getText(item.url()));
    }

    private void collect(String group, FundingCallStatus status, int maxPages, Map<String, RawItem> items) {
        for (int page = 1; page <= maxPages; page++) {
            List<RawItem> pageItems = EuFundsNpooParser.parseListPage(http.getText(LIST_URL.formatted(page, group)));
            int added = 0;
            for (RawItem item : pageItems) {
                if (items.putIfAbsent(item.externalId(), item.toBuilder().statusHint(status).build()) == null) {
                    added++;
                }
            }
            if (added == 0) {
                return;
            }
        }
    }
}
