package com.datathon.api.ingest.source;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.datathon.api.fundingcall.FundingCall;
import com.datathon.api.fundingcall.FundingCallStatus;
import com.datathon.api.fundingcall.TargetGroup;
import com.datathon.api.ingest.IngestProperties;
import com.datathon.api.ingest.RawAttachment;
import com.datathon.api.ingest.RawItem;
import com.datathon.api.ingest.extract.ExtractedFields;
import com.datathon.api.ingest.extract.RuleExtractor;
import com.datathon.api.ingest.http.PoliteHttpClient;
import com.datathon.api.ingest.http.RobotsRules;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;

class EuFundsNpooParserTest {

    @Test
    void parsesOpenListPage() throws IOException {
        List<RawItem> items = EuFundsNpooParser.parseListPage(fixture("list-open.html"));

        assertThat(items).hasSize(8);
        RawItem first = items.getFirst();
        assertThat(first.externalId()).isEqualTo("113");
        assertThat(first.url()).isEqualTo("https://fondovieu.gov.hr/pozivi/113");
        assertThat(first.title()).startsWith("Izgradnja, rekonstrukcija i opremanje osnovnih škola");
        assertThat(first.deadlineHint()).isEqualTo(LocalDate.of(2026, 12, 31));
        assertThat(first.countiesHint()).containsExactly(FundingCall.ALL_COUNTIES);
        assertThat(first.extra()).containsEntry("callCode", "NPOO.C3.1.R1-I2.01")
                .containsEntry("callType", "Otvoreni poziv");
    }

    @Test
    void everyListedCallHasADeadline() throws IOException {
        List<RawItem> items = EuFundsNpooParser.parseListPage(fixture("list-open.html"));
        items.addAll(EuFundsNpooParser.parseListPage(fixture("list-closed-last.html")));

        assertThat(items).hasSize(12).allSatisfy(item -> assertThat(item.deadlineHint()).isNotNull());
        assertThat(items).extracting(RawItem::externalId).doesNotHaveDuplicates();
    }

    @Test
    void readsDetailOfAnOpenCall() throws IOException {
        RawItem detailed = EuFundsNpooParser.applyDetail(listedCall("113"), fixture("detail-open-call.html"));

        assertThat(detailed.provider()).isEqualTo("Ministarstvo znanosti, obrazovanja i mladih");
        assertThat(detailed.publishedAt()).isEqualTo(LocalDate.of(2023, 7, 7));
        assertThat(detailed.deadlineHint()).isEqualTo(LocalDate.of(2026, 12, 31));
        assertThat(detailed.totalAmountHint()).isEqualByComparingTo("1622773604.00");
        assertThat(detailed.minAmountHint()).isEqualByComparingTo("50000");
        assertThat(detailed.maxAmountHint()).isEqualByComparingTo("30000000");
        assertThat(detailed.targetGroupsHint()).containsExactly(TargetGroup.LOCAL_GOVERNMENT);
        assertThat(detailed.text()).contains("Kod poziva: NPOO.C3.1.R1-I2.01")
                .contains("Svrha ovog poziva")
                .contains("Prihvatljivi prijavitelji: Ministarstva; Jedinice lokalne samouprave");
    }

    @Test
    void readsDetailOfAClosedCall() throws IOException {
        RawItem detailed = EuFundsNpooParser.applyDetail(listedCall("1"), fixture("detail-closed-call.html"));

        assertThat(detailed.deadlineHint()).isEqualTo(LocalDate.of(2021, 2, 8));
        assertThat(detailed.totalAmountHint()).isEqualByComparingTo("43400358.36");
        assertThat(detailed.minAmountHint()).isEqualByComparingTo("26544.57");
        assertThat(detailed.maxAmountHint()).isEqualByComparingTo("2654456.17");
        assertThat(detailed.provider()).isEqualTo("Ministarstvo gospodarstva");
    }

    @Test
    void attachmentsAreOnlyLinkedNeverFetched() throws IOException {
        RawItem detailed = EuFundsNpooParser.applyDetail(listedCall("113"), fixture("detail-open-call.html"));

        assertThat(detailed.attachments()).hasSize(3)
                .allSatisfy(attachment -> assertThat(attachment.url())
                        .startsWith("https://api-fondovieu.gov.hr/api/v1/public/getDownloadUrl/"));
        assertThat(detailed.pdfText()).isEmpty();
    }

    @Test
    void linksInTheCallTextAndTheFooterAreNotAttachments() throws IOException {
        String html = fixture("detail-open-call.html").replace("</main>",
                "<a href=\"https://fondovieu.gov.hr/prijava-odabir\">Prijava</a>"
                        + "<a href=\"mailto:info@example.hr\">Pitanja</a></main>");

        RawItem detailed = EuFundsNpooParser.applyDetail(listedCall("113"), html);

        assertThat(detailed.attachments()).extracting(RawAttachment::url)
                .allMatch(url -> url.contains("/public/getDownloadUrl/"));
    }

    @Test
    void zeroAmountMeansNoLimitAndStaysEmpty() throws IOException {
        String html = fixture("detail-open-call.html").replace("50.000,00 €", "0,00 €");

        RawItem detailed = EuFundsNpooParser.applyDetail(listedCall("113"), html);

        assertThat(detailed.minAmountHint()).isNull();
        assertThat(detailed.maxAmountHint()).isEqualByComparingTo("30000000");
    }

    @Test
    void ruleExtractorUsesTheStructuredValues() throws IOException {
        RawItem detailed = EuFundsNpooParser.applyDetail(listedCall("113").toBuilder()
                .statusHint(FundingCallStatus.OPEN).build(), fixture("detail-open-call.html"));

        ExtractedFields fields = new RuleExtractor().extract(detailed);

        assertThat(fields.deadline()).isEqualTo(LocalDate.of(2026, 12, 31));
        assertThat(fields.totalAmount()).isEqualByComparingTo("1622773604.00");
        assertThat(fields.status()).isEqualTo(FundingCallStatus.OPEN);
        assertThat(fields.targetGroups()).contains(TargetGroup.LOCAL_GOVERNMENT);
    }

    @Test
    void robotsTxtOfTheSiteBlocksDownloadsButNotCallPages() throws IOException {
        RobotsRules rules = RobotsRules.parse(fixture("robots.txt"), PoliteHttpClient.USER_AGENT);

        assertThat(rules.isAllowed("/download/poziv.pdf")).isFalse();
        assertThat(rules.isAllowed("/prijavitelj")).isFalse();
        assertThat(rules.isAllowed("/pozivi/113")).isTrue();
        assertThat(rules.isAllowed("/pozivi?page=1&statusGroup=otvoreni")).isTrue();
    }

    @Test
    void fetcherReadsOnlyListAndDetailPagesAndNeverAnAttachment() throws IOException {
        PoliteHttpClient http = mock(PoliteHttpClient.class);
        when(http.getText(anyString())).thenReturn("");
        when(http.getText("https://fondovieu.gov.hr/pozivi?page=1&statusGroup=otvoreni"))
                .thenReturn(fixture("list-open.html"));
        when(http.getText("https://fondovieu.gov.hr/pozivi?page=1&statusGroup=zatvoreni"))
                .thenReturn(fixture("list-closed-last.html"));
        when(http.getText("https://fondovieu.gov.hr/pozivi/113")).thenReturn(fixture("detail-open-call.html"));
        EuFundsNpooFetcher fetcher = new EuFundsNpooFetcher(http, new IngestProperties("target/cache", 5, false, 15));

        List<RawItem> items = fetcher.fetchList();
        RawItem detailed = fetcher.fetchDetail(items.getFirst());

        assertThat(items).hasSize(12);
        assertThat(items.getFirst().statusHint()).isEqualTo(FundingCallStatus.OPEN);
        assertThat(items.getLast().statusHint()).isEqualTo(FundingCallStatus.CLOSED);
        assertThat(detailed.attachments()).isNotEmpty();
        verify(http, never()).getBytes(anyString());
        verify(http, never()).getText("https://api-fondovieu.gov.hr/api/v1/public/getDownloadUrl/"
                + "A25FAAC5-CFF9-442C-B9B8-2A6531EF6144?redirect=true");
    }

    private static RawItem listedCall(String id) {
        return RawItem.builder().externalId(id).url("https://fondovieu.gov.hr/pozivi/" + id)
                .title("Poziv " + id).provider(EuFundsNpooParser.PROVIDER).build();
    }

    private static String fixture(String name) throws IOException {
        try (InputStream input = new ClassPathResource("fixtures/eu-funds-npoo/" + name).getInputStream()) {
            return new String(input.readAllBytes(), StandardCharsets.UTF_8);
        }
    }
}
