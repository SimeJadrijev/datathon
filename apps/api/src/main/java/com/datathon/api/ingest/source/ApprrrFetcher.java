package com.datathon.api.ingest.source;

import com.datathon.api.ingest.RawItem;
import com.datathon.api.ingest.SourceFetcher;
import com.datathon.api.ingest.extract.FundingFilter;
import com.datathon.api.ingest.http.HttpFetchException;
import com.datathon.api.ingest.http.PoliteHttpClient;
import com.datathon.api.ingest.http.RobotsBlockedException;
import com.datathon.api.ingest.source.ApprrrParser.FeedEntry;
import com.datathon.api.source.SourceCode;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import tools.jackson.databind.JsonNode;

@Slf4j
@Component
@RequiredArgsConstructor
public class ApprrrFetcher implements SourceFetcher {

    static final String BASE_URL = "https://www.apprrr.hr";

    private static final String FIELDS = "id,date,link,title,content,categories";
    private static final String CALLS_URL = BASE_URL + "/wp-json/wp/v2/posts?categories=155&per_page=100&page=%d&_fields="
            + FIELDS;
    private static final String POST_URL = BASE_URL + "/wp-json/wp/v2/posts/%s?_fields=" + FIELDS;
    private static final String FEED_URL = BASE_URL + "/feed/";
    private static final int PAGE_SIZE = 100;
    private static final int MAX_PAGES = 5;

    private final PoliteHttpClient http;
    private final FundingFilter fundingFilter;

    @Override
    public SourceCode code() {
        return SourceCode.APPRRR;
    }

    @Override
    public boolean needsTitleFilter() {
        return false;
    }

    @Override
    public List<RawItem> fetchList() {
        Map<String, RawItem> items = new LinkedHashMap<>();
        for (int page = 1; page <= MAX_PAGES; page++) {
            JsonNode posts = http.getJson(CALLS_URL.formatted(page));
            ApprrrParser.parsePosts(posts).forEach(item -> items.putIfAbsent(item.externalId(), item));
            if (posts.size() < PAGE_SIZE) {
                break;
            }
        }
        addFreshFromFeed(items);
        return new ArrayList<>(items.values());
    }

    @Override
    public RawItem fetchDetail(RawItem item) {
        String page = ApprrrParser.interventionPage(item).orElse(null);
        if (page == null) {
            return item;
        }
        try {
            return ApprrrParser.applyInterventionPage(item, http.getText(page));
        } catch (HttpFetchException | RobotsBlockedException e) {
            log.warn("APPRRR: intervention page {} not read: {}", page, e.getMessage());
            return item;
        }
    }

    /** The feed holds only the 10 newest posts but includes announcements filed outside the calls category. */
    private void addFreshFromFeed(Map<String, RawItem> items) {
        try {
            for (FeedEntry entry : ApprrrParser.parseFeed(http.getText(FEED_URL))) {
                if (!items.containsKey(entry.id()) && fundingFilter.isFunding(entry.title())) {
                    RawItem fresh = ApprrrParser.parsePost(http.getJson(POST_URL.formatted(entry.id())));
                    items.putIfAbsent(fresh.externalId(), fresh);
                }
            }
        } catch (HttpFetchException | RobotsBlockedException e) {
            log.warn("APPRRR: feed not read: {}", e.getMessage());
        }
    }
}
