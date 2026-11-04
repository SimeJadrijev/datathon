package com.datathon.api.ingest.source;

import com.datathon.api.fundingcall.FundingCall;
import com.datathon.api.ingest.RawItem;
import com.datathon.api.ingest.SourceFetcher;
import com.datathon.api.ingest.extract.CallTitleFilter;
import com.datathon.api.ingest.http.HttpFetchException;
import com.datathon.api.ingest.http.PoliteHttpClient;
import com.datathon.api.ingest.http.RobotsBlockedException;
import com.datathon.api.ingest.source.LocalHtmlListFetcher.HtmlListSite;
import com.datathon.api.ingest.source.LocalWordPressFetcher.WordPressSite;
import com.datathon.api.source.SourceCode;
import java.time.Clock;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import tools.jackson.databind.JsonNode;

@Slf4j
@Component
public class AgencyFetcher implements SourceFetcher {

    static final int DAYS_BACK = 365;
    static final List<WordPressSite> WORD_PRESS_SITES = List.of(
            new WordPressSite(new LocalSite("https://hrzz.hr", "Hrvatska zaklada za znanost", FundingCall.ALL_COUNTIES),
                    "posts?categories=39"),
            new WordPressSite(new LocalSite("https://euribarstvo.hr", "Ministarstvo poljoprivrede, šumarstva i ribarstva "
                    + "(Program za ribarstvo)", FundingCall.ALL_COUNTIES), "posts?categories=15"));
    static final HtmlListSite AMPEU = new HtmlListSite(
            new LocalSite("https://www.ampeu.hr/natjecaji", "Agencija za mobilnost i programe EU", FundingCall.ALL_COUNTIES),
            "div.NewsItem", "a.NewsItem__link[href]", ".NewsItem__title");

    private final PoliteHttpClient http;
    private final CallTitleFilter callTitleFilter;
    private final Clock clock;

    @Autowired
    public AgencyFetcher(PoliteHttpClient http, CallTitleFilter callTitleFilter) {
        this(http, callTitleFilter, Clock.systemDefaultZone());
    }

    AgencyFetcher(PoliteHttpClient http, CallTitleFilter callTitleFilter, Clock clock) {
        this.http = http;
        this.callTitleFilter = callTitleFilter;
        this.clock = clock;
    }

    @Override
    public SourceCode code() {
        return SourceCode.AGENCIES;
    }

    @Override
    public boolean needsTitleFilter() {
        return false;
    }

    @Override
    public List<RawItem> fetchList() {
        List<RawItem> items = new ArrayList<>();
        String after = LocalDate.now(clock).minusDays(DAYS_BACK) + "T00:00:00";
        for (WordPressSite site : WORD_PRESS_SITES) {
            try {
                Optional<JsonNode> posts = WordPressPaging.readPage(1,
                        page -> http.getJson(LocalWordPressFetcher.pageUrl(site, after, page)));
                posts.ifPresent(found -> keepCalls(LocalWordPressFetcher.parsePosts(found, site.site()), items));
            } catch (HttpFetchException | RobotsBlockedException e) {
                log.warn("{}: list failed: {}", site.site().url(), e.getMessage());
            }
        }
        try {
            keepCalls(LocalHtmlListFetcher.parseList(http.getText(AMPEU.site().url()), AMPEU), items);
        } catch (HttpFetchException | RobotsBlockedException e) {
            log.warn("{}: list failed: {}", AMPEU.site().url(), e.getMessage());
        }
        return items;
    }

    @Override
    public RawItem fetchDetail(RawItem item) {
        return LocalPages.needsDetail(item) ? LocalPages.withDetail(http.getText(item.url()), item) : item;
    }

    private void keepCalls(List<RawItem> candidates, List<RawItem> items) {
        candidates.stream().filter(item -> callTitleFilter.isCall(item.title())).forEach(items::add);
    }
}
