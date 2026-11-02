package com.datathon.api.ingest.source;

import static com.datathon.api.ingest.source.SourceFixtures.json;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.datathon.api.ingest.RawItem;
import com.datathon.api.ingest.extract.FundingFilter;
import com.datathon.api.ingest.http.HttpFetchException;
import com.datathon.api.ingest.http.PoliteHttpClient;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.json.JsonMapper;

class WordPressFetchersTest {

    private static final String HZZ = "https://mjere.hzz.hr/wp-json/wp/v2";
    private static final String APPRRR = "https://www.apprrr.hr";
    private static final JsonMapper JSON = JsonMapper.builder().build();

    private final PoliteHttpClient http = mock(PoliteHttpClient.class);

    @BeforeEach
    void stubTaxonomies() {
        when(http.getJson(HZZ + "/measures_years?per_page=100")).thenReturn(json("hzz/years.json"));
        when(http.getJson(HZZ + "/measures_category?per_page=100")).thenReturn(json("hzz/categories.json"));
    }

    @Test
    void hzzWithExactlyOneFullPageEndsOnTheOutOfRangeAnswer() {
        when(http.getJson(HZZ + "/measures?per_page=100&page=1")).thenReturn(measures(100));
        when(http.getJson(HZZ + "/measures?per_page=100&page=2")).thenThrow(outOfRange());

        List<RawItem> items = new HzzFetcher(http).fetchList();

        assertThat(items).hasSize(100);
    }

    @Test
    void hzzWithTwoFullPagesKeepsBothAndEndsOnTheOutOfRangeAnswer() {
        when(http.getJson(HZZ + "/measures?per_page=100&page=1")).thenReturn(measures(100));
        when(http.getJson(HZZ + "/measures?per_page=100&page=2")).thenReturn(measures(100, 1000));
        when(http.getJson(HZZ + "/measures?per_page=100&page=3")).thenThrow(outOfRange());

        assertThat(new HzzFetcher(http).fetchList()).hasSize(200);
    }

    @Test
    void hzzFailureOnTheFirstPageIsStillAFailure() {
        when(http.getJson(HZZ + "/measures?per_page=100&page=1")).thenThrow(outOfRange());

        assertThatThrownBy(() -> new HzzFetcher(http).fetchList()).isInstanceOf(HttpFetchException.class);
    }

    @Test
    void hzzServerErrorOnALaterPageIsStillAFailure() {
        when(http.getJson(HZZ + "/measures?per_page=100&page=1")).thenReturn(measures(100));
        when(http.getJson(HZZ + "/measures?per_page=100&page=2"))
                .thenThrow(new HttpFetchException("HTTP 500", 500));

        assertThatThrownBy(() -> new HzzFetcher(http).fetchList()).isInstanceOf(HttpFetchException.class);
    }

    @Test
    void apprrrWithExactlyOneFullPageEndsOnTheOutOfRangeAnswer() {
        when(http.getJson(callsPage(1))).thenReturn(posts(100));
        when(http.getJson(callsPage(2))).thenThrow(outOfRange());
        when(http.getText(APPRRR + "/feed/")).thenReturn("<rss><channel></channel></rss>");

        List<RawItem> items = new ApprrrFetcher(http, new FundingFilter()).fetchList();

        assertThat(items).hasSize(100);
    }

    @Test
    void apprrrFeedPostThatFailsDoesNotSkipTheOnesAfterIt() {
        when(http.getJson(callsPage(1))).thenReturn(JSON.createArrayNode());
        when(http.getText(APPRRR + "/feed/")).thenReturn(feed(
                "1001", "Javni poziv za potpore prvi", "1002", "Javni poziv za potpore drugi",
                "1003", "Javni poziv za potpore treći"));
        when(http.getJson(postUrl("1001"))).thenThrow(new HttpFetchException("HTTP 500", 500));
        when(http.getJson(postUrl("1002"))).thenReturn(post(1002));
        when(http.getJson(postUrl("1003"))).thenReturn(post(1003));

        List<RawItem> items = new ApprrrFetcher(http, new FundingFilter()).fetchList();

        assertThat(items).extracting(RawItem::externalId).containsExactly("1002", "1003");
    }

    @Test
    void apprrrFeedThatCannotBeReadKeepsTheCallsCategory() {
        when(http.getJson(callsPage(1))).thenReturn(posts(3));
        when(http.getText(APPRRR + "/feed/")).thenThrow(new HttpFetchException("HTTP 503", 503));

        assertThat(new ApprrrFetcher(http, new FundingFilter()).fetchList()).hasSize(3);
    }

    @Test
    void apprrrDoesNotFetchTheInterventionPageWhenThePostAlreadyHasDocuments() {
        RawItem withDocument = ApprrrParser.parsePosts(json("apprrr/posts.json")).getFirst().toBuilder()
                .attachments(List.of(new com.datathon.api.ingest.RawAttachment("Poziv", "https://example.hr/poziv.pdf")))
                .build();

        RawItem result = new ApprrrFetcher(http, new FundingFilter()).fetchDetail(withDocument);

        assertThat(result.attachments()).hasSize(1);
        org.mockito.Mockito.verify(http, org.mockito.Mockito.never()).getText(anyString());
    }

    private static String callsPage(int page) {
        return APPRRR + "/wp-json/wp/v2/posts?categories=155&per_page=100&page=" + page
                + "&_fields=id,date,link,title,content,categories";
    }

    private static String postUrl(String id) {
        return APPRRR + "/wp-json/wp/v2/posts/" + id + "?_fields=id,date,link,title,content,categories";
    }

    private static HttpFetchException outOfRange() {
        return new HttpFetchException("HTTP 400", 400);
    }

    private static JsonNode measures(int count) {
        return measures(count, 0);
    }

    private static JsonNode measures(int count, int firstId) {
        var array = JSON.createArrayNode();
        for (int index = 0; index < count; index++) {
            array.add(JSON.createObjectNode()
                    .put("id", firstId + index + 1)
                    .put("date", "2026-01-01T10:00:00")
                    .put("link", "https://mjere.hzz.hr/mjere/m" + (firstId + index))
                    .<tools.jackson.databind.node.ObjectNode>set("title", JSON.createObjectNode().put("rendered", "Mjera"))
                    .<tools.jackson.databind.node.ObjectNode>set("content", JSON.createObjectNode().put("rendered", "<p>Tekst</p>"))
                    .<tools.jackson.databind.node.ObjectNode>set("measures_years", JSON.createArrayNode())
                    .<tools.jackson.databind.node.ObjectNode>set("measures_category", JSON.createArrayNode()));
        }
        return array;
    }

    private static JsonNode posts(int count) {
        var array = JSON.createArrayNode();
        for (int index = 0; index < count; index++) {
            array.add(post(index + 1));
        }
        return array;
    }

    private static JsonNode post(int id) {
        return JSON.createObjectNode()
                .put("id", id)
                .put("date", "2026-10-13T10:00:00")
                .put("link", "https://www.apprrr.hr/p" + id)
                .<tools.jackson.databind.node.ObjectNode>set("title", JSON.createObjectNode().put("rendered", "Javni poziv " + id))
                .<tools.jackson.databind.node.ObjectNode>set("content", JSON.createObjectNode().put("rendered", "<p>Tekst</p>"));
    }

    private static String feed(String... idsAndTitles) {
        List<String> items = new ArrayList<>();
        for (int index = 0; index < idsAndTitles.length; index += 2) {
            items.add("<item><title>" + idsAndTitles[index + 1] + "</title><guid isPermaLink=\"false\">"
                    + "https://www.apprrr.hr/?p=" + idsAndTitles[index] + "</guid></item>");
        }
        return "<rss><channel>" + String.join("", items) + "</channel></rss>";
    }
}
