package com.datathon.api.ingest.source;

import static com.datathon.api.ingest.source.SourceFixtures.text;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.datathon.api.fundingcall.FundingCallStatus;
import com.datathon.api.fundingcall.FundingCallType;
import com.datathon.api.ingest.RawItem;
import com.datathon.api.ingest.extract.CallTitleFilter;
import com.datathon.api.ingest.extract.ExtractedFields;
import com.datathon.api.ingest.extract.FundingFilter;
import com.datathon.api.ingest.extract.RuleExtractor;
import com.datathon.api.ingest.http.PoliteHttpClient;
import com.datathon.api.ingest.source.GovHrNewsParser.Site;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import org.junit.jupiter.api.Test;

class GovHrNewsParserTest {

    private static final Site MPUDT = new Site("https://mpudt.gov.hr", "Ministarstvo pravosuđa, uprave i digitalne transformacije");
    private static final Site CULTURE = new Site("https://min-kulture.gov.hr", "Ministarstvo kulture i medija");
    private static final Clock NOVEMBER_2026 = Clock.fixed(Instant.parse("2026-11-03T10:00:00Z"), ZoneId.of("Europe/Zagreb"));

    @Test
    void readsNewsFromTheSearchPage() {
        List<RawItem> items = GovHrNewsParser.parseSearchPage(text("gov-hr/news-search.html"), MPUDT);

        assertThat(items).hasSize(4);
        assertThat(items).extracting(RawItem::title).noneMatch(title -> title.startsWith("Godišnji program"));
        RawItem datathon = items.stream().filter(item -> item.title().contains("Datathon")).findFirst().orElseThrow();
        assertThat(datathon.externalId()).isEqualTo("mpudt.gov.hr:31145");
        assertThat(datathon.url()).startsWith("https://mpudt.gov.hr/otvorene-prijave-za-datathon");
        assertThat(datathon.publishedAt()).isEqualTo(LocalDate.of(2026, 10, 20));
        assertThat(datathon.provider()).isEqualTo(MPUDT.provider());
    }

    @Test
    void fetcherKeepsCallsAndDropsOtherNews() {
        PoliteHttpClient http = mock(PoliteHttpClient.class);
        when(http.getText(anyString())).thenReturn(text("gov-hr/news-search.html"));

        List<RawItem> calls = new GovHrNewsFetcher(http, new CallTitleFilter(new FundingFilter()), List.of(MPUDT), NOVEMBER_2026).fetchList();

        assertThat(calls).extracting(RawItem::title).noneMatch(title -> title.startsWith("Poziv na sudjelovanje u anketnom"));
        assertThat(calls).extracting(RawItem::title).anyMatch(title -> title.contains("Datathon"));
        assertThat(calls).hasSize(3);
    }

    @Test
    void jobAdsAndCondolencesAreDroppedAndTheGrantCallStays() {
        List<RawItem> news = GovHrNewsParser.parseSearchPage(text("gov-hr/news-search-jobs.html"), CULTURE);

        assertThat(news).extracting(RawItem::title).filteredOn(new CallTitleFilter(new FundingFilter())::isCall)
                .singleElement().asString().startsWith("Poziv na dodjelu bespovratnih sredstava");
    }

    @Test
    void searchAsksForRecentNewsWithTheTerm() {
        assertThat(GovHrNewsFetcher.searchUrl(MPUDT, "natječaj", "05.07.2026.", 2))
                .isEqualTo("https://mpudt.gov.hr/rezultati-pretrazivanja/49?trazi=1&tag=&datumod=05.07.2026.&datumdo="
                        + "&pojam=natje%C4%8Daj&page=2");
    }

    @Test
    void detailIgnoresStruckOutDatesSoTheChangedDeadlineIsRead() {
        RawItem listed = GovHrNewsParser.parseSearchPage(text("gov-hr/news-search.html"), MPUDT).stream()
                .filter(item -> item.title().contains("Datathon")).findFirst().orElseThrow();

        RawItem detailed = GovHrNewsParser.parseDetail(text("gov-hr/news-detail.html"), listed);
        ExtractedFields fields = new RuleExtractor().extract(detailed);

        assertThat(detailed.text()).contains("Nagradni fond iznosi 12.000 eura").doesNotContain("23. listopada 2026.");
        assertThat(fields.deadline()).isEqualTo(LocalDate.of(2026, 10, 30));
        assertThat(fields.status()).isEqualTo(FundingCallStatus.CLOSED);
        assertThat(fields.type()).isEqualTo(FundingCallType.AWARD);
        assertThat(fields.totalAmount()).isEqualByComparingTo("12000");
    }
}
