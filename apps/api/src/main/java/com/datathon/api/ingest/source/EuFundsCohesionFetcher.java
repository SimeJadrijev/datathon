package com.datathon.api.ingest.source;

import com.datathon.api.ingest.RawItem;
import com.datathon.api.ingest.SourceFetcher;
import com.datathon.api.ingest.http.PoliteHttpClient;
import com.datathon.api.source.SourceCode;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import tools.jackson.databind.JsonNode;

@Component
@RequiredArgsConstructor
public class EuFundsCohesionFetcher implements SourceFetcher {

    static final int PAGE_SIZE = 100;
    private static final int MAX_PAGES = 10;

    private final PoliteHttpClient http;

    @Override
    public SourceCode code() {
        return SourceCode.EU_FUNDS_COHESION;
    }

    @Override
    public boolean needsTitleFilter() {
        return false;
    }

    @Override
    public List<RawItem> fetchList() {
        List<RawItem> items = new ArrayList<>();
        for (int page = 0; page < MAX_PAGES; page++) {
            int skip = page * PAGE_SIZE;
            JsonNode calls = http.getJson(EuFundsCohesionParser.pageUrl(skip, PAGE_SIZE));
            items.addAll(EuFundsCohesionParser.parsePage(calls));
            if (calls.path("Records").size() < PAGE_SIZE || skip + PAGE_SIZE >= EuFundsCohesionParser.totalCount(calls)) {
                break;
            }
        }
        return items;
    }

    @Override
    public RawItem fetchDetail(RawItem item) {
        return item;
    }
}
