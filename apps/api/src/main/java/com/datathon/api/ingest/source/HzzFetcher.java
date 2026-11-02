package com.datathon.api.ingest.source;

import com.datathon.api.ingest.RawItem;
import com.datathon.api.ingest.SourceFetcher;
import com.datathon.api.ingest.http.PoliteHttpClient;
import com.datathon.api.source.SourceCode;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import tools.jackson.databind.JsonNode;

@Component
@RequiredArgsConstructor
public class HzzFetcher implements SourceFetcher {

    private static final String API = "https://mjere.hzz.hr/wp-json/wp/v2";
    private static final int MAX_PAGES = 5;

    private final PoliteHttpClient http;

    @Override
    public SourceCode code() {
        return SourceCode.HZZ;
    }

    @Override
    public boolean needsTitleFilter() {
        return false;
    }

    @Override
    public List<RawItem> fetchList() {
        Map<Integer, String> years = HzzParser.termNames(http.getJson(API + "/measures_years?per_page=100"));
        Map<Integer, String> categories = HzzParser.termNames(http.getJson(API + "/measures_category?per_page=100"));
        List<RawItem> items = new ArrayList<>();
        for (int page = 1; page <= MAX_PAGES; page++) {
            Optional<JsonNode> measures = WordPressPaging.readPage(page, number -> http.getJson(
                    API + "/measures?per_page=" + WordPressPaging.PAGE_SIZE + "&page=" + number));
            if (measures.isEmpty()) {
                break;
            }
            items.addAll(HzzParser.parseMeasures(measures.get(), years, categories));
            if (measures.get().size() < WordPressPaging.PAGE_SIZE) {
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
