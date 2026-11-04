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
import java.util.Optional;
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
    private static final int MAX_PAGES = 5;

    private final PoliteHttpClient http;
    private final FundingFilter fundingFilter;

    @Override
    public SourceCode code() {
        return SourceCode.APPRRR;
    }

    @Override
    public boolean needsTitleFilter() {
        return true;
    }

    @Override
    public List<RawItem> fetchList() {
        Map<String, RawItem> items = new LinkedHashMap<>();
        for (int page = 1; page <= MAX_PAGES; page++) {
            Optional<JsonNode> posts = WordPressPaging.readPage(page, number -> http.getJson(CALLS_URL.formatted(number)));
            if (posts.isEmpty()) {
                break;
            }
            ApprrrParser.parsePosts(posts.get()).forEach(item -> items.putIfAbsent(item.externalId(), item));
            if (posts.get().size() < WordPressPaging.PAGE_SIZE) {
                break;
            }
        }
        addFreshFromFeed(items);
        return new ArrayList<>(items.values());
    }

    @Override
    public RawItem fetchDetail(RawItem item) {
        String page = ApprrrParser.interventionPage(item).orElse(null);
        if (page == null || !item.attachments().isEmpty()) {
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
        List<FeedEntry> entries;
        try {
            entries = ApprrrParser.parseFeed(http.getText(FEED_URL));
        } catch (HttpFetchException | RobotsBlockedException e) {
            log.warn("APPRRR: feed not read: {}", e.getMessage());
            return;
        }
        for (FeedEntry entry : entries) {
            if (!items.containsKey(entry.id()) && fundingFilter.isFunding(entry.title())) {
                readFeedPost(entry).ifPresent(post -> items.putIfAbsent(post.externalId(), post));
            }
        }
    }

    private Optional<RawItem> readFeedPost(FeedEntry entry) {
        try {
            return Optional.of(ApprrrParser.parsePost(http.getJson(POST_URL.formatted(entry.id()))));
        } catch (HttpFetchException | RobotsBlockedException e) {
            log.warn("APPRRR: feed post {} not read: {}", entry.id(), e.getMessage());
            return Optional.empty();
        }
    }
}
