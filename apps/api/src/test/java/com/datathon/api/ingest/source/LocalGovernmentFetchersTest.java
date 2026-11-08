package com.datathon.api.ingest.source;

import static com.datathon.api.ingest.source.SourceFixtures.json;
import static com.datathon.api.ingest.source.SourceFixtures.text;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.datathon.api.ingest.RawItem;
import com.datathon.api.ingest.extract.CallTitleFilter;
import com.datathon.api.ingest.extract.FundingFilter;
import com.datathon.api.ingest.extract.RuleExtractor;
import com.datathon.api.ingest.http.PoliteHttpClient;
import com.datathon.api.ingest.source.LocalWordPressFetcher.WordPressSite;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.Test;

class LocalGovernmentFetchersTest {

    private static final CallTitleFilter CALLS = new CallTitleFilter(new FundingFilter());
    private static final LocalSite CAKOVEC = new LocalSite("https://www.cakovec.hr", "Grad Čakovec", "Međimurska");
    private static final LocalSite SPLIT = new LocalSite("https://split.hr/natjecaji-i-oglasi", "Grad Split", "Splitsko-dalmatinska");
    private static final LocalSite DALMATIA = new LocalSite("https://www.dalmacija.hr/natjecaji/aktualni-natjecaji",
            "Splitsko-dalmatinska županija", "Splitsko-dalmatinska");
    private static final LocalSite SIBENIK = new LocalSite("https://www.sibensko-kninska-zupanija.hr/pozivi-i-natjecaji",
            "Šibensko-kninska županija", "Šibensko-kninska");
    private static final LocalSite OSIJEK_BARANJA = new LocalSite(
            "https://www.obz.hr/index.php/potpore/natjecaji-i-javni-pozivi-u-tijeku-potpore", "Osječko-baranjska županija",
            "Osječko-baranjska");

    @Test
    void wordPressPostsCarryTheCityAsProviderAndItsCounty() {
        List<RawItem> posts = LocalWordPressFetcher.parsePosts(json("local-gov/wordpress-cakovec-posts.json"), CAKOVEC);

        RawItem solar = posts.getFirst();
        assertThat(solar.title()).startsWith("Javni poziv za sufinanciranje izgradnje sunčane elektrane");
        assertThat(solar.externalId()).startsWith("cakovec.hr:");
        assertThat(solar.provider()).isEqualTo("Grad Čakovec");
        assertThat(solar.countiesHint()).containsExactly("Međimurska");
        assertThat(solar.text()).isNotBlank();
        assertThat(posts).extracting(RawItem::title).filteredOn(CALLS::isCall).hasSize(1);
    }

    @Test
    void wordPressListAsksOnlyForRecentPosts() {
        WordPressSite osijek = new WordPressSite(new LocalSite("https://www.osijek.hr", "Grad Osijek", "Osječko-baranjska"),
                "natjecaji");
        WordPressSite kastela = new WordPressSite(new LocalSite("https://www.kastela.hr", "Grad Kaštela", "Splitsko-dalmatinska"),
                "natjecaji?vrsta-natjecaja=76,77");

        assertThat(LocalWordPressFetcher.pageUrl(osijek, "2026-07-06T00:00:00", 2))
                .isEqualTo("https://www.osijek.hr/wp-json/wp/v2/natjecaji?after=2026-07-06T00:00:00&per_page=50&page=2");
        assertThat(LocalWordPressFetcher.pageUrl(kastela, "2026-07-06T00:00:00", 1))
                .isEqualTo("https://www.kastela.hr/wp-json/wp/v2/natjecaji?vrsta-natjecaja=76,77&after=2026-07-06T00:00:00"
                        + "&per_page=50&page=1");
    }

    @Test
    void wordPressTypeWithoutTextNeedsTheDetailPage() {
        List<RawItem> notices = LocalWordPressFetcher.parsePosts(json("local-gov/wordpress-osijek-natjecaji.json"),
                new LocalSite("https://www.osijek.hr", "Grad Osijek", "Osječko-baranjska"));

        assertThat(notices).allMatch(LocalPages::needsDetail);
        assertThat(notices).extracting(RawItem::title).filteredOn(CALLS::isCall).isEmpty();
    }

    @Test
    void easyDnnListDropsJobAdsAndNewsletters() {
        List<RawItem> items = LocalEasyDnnFetcher.parseList(text("local-gov/easydnn-split-list.html"), SPLIT);

        assertThat(items).extracting(RawItem::title).noneMatch(title -> title.startsWith("Javni natječaj za prijam"));
        assertThat(items).extracting(RawItem::title).filteredOn(CALLS::isCall)
                .noneMatch(title -> title.startsWith("Newsletter") || title.startsWith("Rang lista"));
        RawItem scholarship = items.stream().filter(item -> item.title().startsWith("Natječaj za stipendiranje učenika i studenata"))
                .findFirst().orElseThrow();
        assertThat(scholarship.publishedAt()).isEqualTo(LocalDate.of(2026, 10, 20));
        assertThat(scholarship.text()).contains("Prijave na natječaj traju od");
        assertThat(LocalEasyDnnFetcher.nextPageUrl(text("local-gov/easydnn-split-list.html"), SPLIT, 2))
                .contains("https://split.hr/natjecaji-i-oglasi/pgrid/5936/pageid/2");
    }

    @Test
    void easyDnnReadsBothDateLayouts() {
        RawItem dalmatia = LocalEasyDnnFetcher.parseList(text("local-gov/easydnn-dalmacija-list.html"), DALMATIA).getFirst();
        RawItem sibenik = LocalEasyDnnFetcher.parseList(text("local-gov/easydnn-sibenik-list.html"), SIBENIK).getFirst();

        assertThat(dalmatia.publishedAt()).isEqualTo(LocalDate.of(2026, 11, 3));
        assertThat(sibenik.publishedAt()).isEqualTo(LocalDate.of(2026, 11, 3));
        assertThat(sibenik.text()).contains("Natječaj otvoren do 19.11.2026.");
    }

    @Test
    void easyDnnDetailAddsTheArticleBody() {
        RawItem listed = LocalEasyDnnFetcher.parseList(text("local-gov/easydnn-split-list.html"), SPLIT).stream()
                .filter(item -> item.title().startsWith("Natječaj za stipendiranje učenika i studenata")).findFirst().orElseThrow();

        RawItem detailed = LocalPages.withDetail(text("local-gov/easydnn-split-detail.html"), listed);

        assertThat(detailed.text()).contains("Prijave na natječaj traju od").contains("eusluge.split.hr");
    }

    @Test
    void joomlaFeedItemsGetDateAndIdAndClosingNoticesAreDropped() {
        List<RawItem> items = LocalJoomlaFeedFetcher.parseFeed(text("local-gov/joomla-obz-feed.xml"), OSIJEK_BARANJA);

        assertThat(items).isNotEmpty();
        assertThat(items.getFirst().externalId()).isEqualTo("obz.hr:8549");
        assertThat(items.getFirst().publishedAt()).isEqualTo(LocalDate.of(2026, 10, 30));
        assertThat(items).extracting(RawItem::title).filteredOn(CALLS::isCall)
                .noneMatch(title -> title.startsWith("Obavijest o zatvaranju"));
    }

    @Test
    void zagrebListLinksDocumentsAsAttachmentsAndPagesForDetail() {
        List<RawItem> items = ZagrebFetcher.parseList(text("local-gov/zagreb-list.html"));

        RawItem housing = items.stream().filter(item -> item.title().startsWith("Javni poziv za dodjelu potpora za stanovanje"))
                .findFirst().orElseThrow();
        assertThat(housing.externalId()).isEqualTo("zagreb.hr:227280");
        assertThat(housing.countiesHint()).containsExactly("Grad Zagreb");
        assertThat(housing.publishedAt()).isEqualTo(LocalDate.of(2026, 10, 29));
        assertThat(items).filteredOn(item -> DocumentLinks.isDocument(item.url())).allMatch(item -> !item.attachments().isEmpty());
        assertThat(items).extracting(RawItem::title).filteredOn(CALLS::isCall)
                .noneMatch(title -> title.startsWith("Javni natječaj za dodjelu lokacije"));
    }

    @Test
    void zagrebDetailGivesAmountsAndTheUntilFundsUsedNote() {
        RawItem listed = ZagrebFetcher.parseList(text("local-gov/zagreb-list.html")).stream()
                .filter(item -> item.externalId().equals("zagreb.hr:227280")).findFirst().orElseThrow();

        RawItem detailed = LocalPages.withDetail(text("local-gov/zagreb-detail.html"), listed);
        var fields = new RuleExtractor().extract(detailed);

        assertThat(detailed.text()).contains("Ukupna vrijednost Javnog poziva iznosi 65.000 eura");
        assertThat(fields.totalAmount()).isEqualByComparingTo("65000");
        assertThat(fields.deadlineNote()).isEqualTo("do iskorištenja sredstava");
        assertThat(detailed.attachments()).isNotEmpty();
    }

    @Test
    void oneFailingWordPressSiteDoesNotStopTheOthers() {
        PoliteHttpClient http = mock(PoliteHttpClient.class);
        when(http.getJson(anyString())).thenAnswer(invocation -> {
            String url = invocation.getArgument(0);
            if (url.contains("osijek")) {
                throw new com.datathon.api.ingest.http.HttpFetchException("HTTP 503", 503);
            }
            return json("local-gov/wordpress-cakovec-posts.json");
        });
        List<WordPressSite> sites = List.of(
                new WordPressSite(new LocalSite("https://www.osijek.hr", "Grad Osijek", "Osječko-baranjska"), "natjecaji"),
                new WordPressSite(CAKOVEC, "posts?categories=69,89"));

        List<RawItem> items = new LocalWordPressFetcher(http, CALLS, sites, java.time.Clock.systemDefaultZone()).fetchList();

        assertThat(items).extracting(RawItem::provider).containsOnly("Grad Čakovec");
    }

    @Test
    void htmlListUsesTheSiteSelectorsAndDropsDatesFromTitles() {
        var dubrovnik = new LocalHtmlListFetcher.HtmlListSite(
                new LocalSite("https://www.dubrovnik.hr/natjecaji", "Grad Dubrovnik", "Dubrovačko-neretvanska"),
                "div.news-item.post-item", "a[href]", null);

        List<RawItem> items = LocalHtmlListFetcher.parseList(text("local-gov/html-dubrovnik-list.html"), dubrovnik);

        assertThat(items).isNotEmpty().allMatch(item -> item.externalId().startsWith("dubrovnik.hr:"));
        assertThat(items).extracting(RawItem::title).noneMatch(title -> title.matches("^\\d.*"));
        assertThat(items).extracting(RawItem::title).filteredOn(CALLS::isCall)
                .noneMatch(title -> title.contains("zakup") || title.startsWith("Obavijest o ishodu"));
    }

    @Test
    void htmlListKeepsCountyGrantCallsAndDropsSalesAndDecisions() {
        var bjelovarBilogora = new LocalHtmlListFetcher.HtmlListSite(new LocalSite(
                "https://www.bbz.hr/javni-pozivi-natjecaji/javni-pozivi", "Bjelovarsko-bilogorska županija", "Bjelovarsko-bilogorska"),
                "article.post", "h4 a[href]", null);

        List<String> calls = LocalHtmlListFetcher.parseList(text("local-gov/html-bbz-list.html"), bjelovarBilogora).stream()
                .map(RawItem::title).filter(CALLS::isCall).toList();

        assertThat(calls).hasSizeLessThanOrEqualTo(LocalHtmlListFetcher.MAX_ITEMS_PER_SITE)
                .anyMatch(title -> title.startsWith("JAVNI POZIV za dodjelu novčanih potpora"))
                .noneMatch(title -> title.contains("kupnju nekretnina") || title.startsWith("ODLUKA"));
    }

    @Test
    void ampeuItemsHaveTheTitleWithoutCategoryAndTheDeadlineInTheText() {
        List<RawItem> items = LocalHtmlListFetcher.parseList(text("local-gov/html-ampeu-list.html"), AgencyFetcher.AMPEU);

        RawItem fulbright = items.stream().filter(item -> item.title().startsWith("Program „Fulbright Specialist“"))
                .findFirst().orElseThrow();
        assertThat(fulbright.publishedAt()).isEqualTo(LocalDate.of(2026, 11, 2));
        assertThat(new RuleExtractor().extract(fulbright).deadline()).isEqualTo(LocalDate.of(2027, 1, 31));
        assertThat(fulbright.countiesHint()).containsExactly("ALL");
        assertThat(items).extracting(RawItem::title).filteredOn(CALLS::isCall)
                .noneMatch(title -> title.startsWith("Javni natječaj za zapošljavanje"));
    }

    @Test
    void abbreviatedMonthDateOfTheBjelovarListIsRead() {
        assertThat(LocalHtmlListFetcher.shortMonthDate("26 lis 2026 JAVNI POZIV za dodjelu potpora"))
                .isEqualTo(LocalDate.of(2026, 10, 26));
        assertThat(LocalHtmlListFetcher.shortMonthDate("7 kol 2026 Javni poziv")).isEqualTo(LocalDate.of(2026, 8, 7));
        assertThat(LocalHtmlListFetcher.shortMonthDate("Javni poziv bez datuma")).isNull();
    }

    @Test
    void titleWhitespaceIsCollapsed() {
        RawItem item = RawItem.builder().title(" Erasmus+ natječaj -  države\u00A0članice\n EU-a ").build();

        assertThat(item.title()).isEqualTo("Erasmus+ natječaj - države članice EU-a");
    }
}
