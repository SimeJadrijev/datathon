package com.datathon.api.ingest.source;

import com.datathon.api.ingest.IngestProperties;
import com.datathon.api.ingest.RawItem;
import com.datathon.api.ingest.SourceFetcher;
import com.datathon.api.ingest.http.PoliteHttpClient;
import com.datathon.api.ingest.source.AssociationsOfficeParser.ListPage;
import com.datathon.api.source.SourceCode;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class AssociationsOfficeFetcher implements SourceFetcher {

    private static final String LIST_URL = AssociationsOfficeParser.BASE_URL
            + "/istaknute-teme/financiranje-programa-i-projekata-udruga-iz-javnih-izvora/natjecaji-5503/5503"
            + "?trazi=1&davatelj=&podrucje=&status=%d&datumod=&datumdo=&pojam=&page=%d";
    private static final int OPEN = 1;
    private static final int CLOSED = 2;
    private static final int MAX_OPEN_PAGES = 60;

    private final PoliteHttpClient http;
    private final IngestProperties properties;

    @Override
    public boolean needsTitleFilter() {
        return false;
    }

    @Override
    public SourceCode code() {
        return SourceCode.ASSOCIATIONS_OFFICE;
    }

    @Override
    public List<RawItem> fetchList() {
        Map<String, RawItem> items = new LinkedHashMap<>();
        collect(OPEN, MAX_OPEN_PAGES, items);
        collect(CLOSED, properties.closedPages(), items);
        return new ArrayList<>(items.values());
    }

    @Override
    public RawItem fetchDetail(RawItem item) {
        return AssociationsOfficeParser.applyDetail(item, http.getText(item.url()));
    }

    private void collect(int status, int maxPages, Map<String, RawItem> items) {
        for (int page = 1; page <= maxPages; page++) {
            ListPage listPage = AssociationsOfficeParser.parseListPage(http.getText(LIST_URL.formatted(status, page)));
            int added = 0;
            for (RawItem item : listPage.items()) {
                if (items.putIfAbsent(item.externalId(), item) == null) {
                    added++;
                }
            }
            if (page == 1) {
                log.info("udruge.gov.hr status {}: {} on the source", status, listPage.totalOnSource());
            }
            if (added == 0) {
                return;
            }
        }
    }
}
