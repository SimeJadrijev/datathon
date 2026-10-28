package com.datathon.api.ingest.source;

import static org.assertj.core.api.Assertions.assertThat;

import com.datathon.api.fundingcall.FundingCallStatus;
import com.datathon.api.fundingcall.TargetGroup;
import com.datathon.api.ingest.RawItem;
import com.datathon.api.ingest.extract.ExtractedFields;
import com.datathon.api.ingest.extract.RuleExtractor;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;

class AssociationsOfficeParserTest {

    @Test
    void parsesListPage() throws IOException {
        var page = AssociationsOfficeParser.parseListPage(fixture("list-open.html"));

        assertThat(page.totalOnSource()).isEqualTo(92);
        assertThat(page.items()).hasSize(10);
        RawItem first = page.items().getFirst();
        assertThat(first.externalId()).isEqualTo("3490");
        assertThat(first.url()).isEqualTo("https://udruge.gov.hr/natjecaji-5503/5503?nid=3490");
        assertThat(first.title()).startsWith("Javni natječaj za sponzoriranje programa");
        assertThat(first.provider()).isEqualTo("Javna ustaova Rezervat Lokrum");
        assertThat(first.publishedAt()).isEqualTo(LocalDate.of(2026, 10, 27));
        assertThat(first.statusHint()).isEqualTo(FundingCallStatus.OPEN);
        assertThat(first.categories()).containsExactly("Ostali davatelji");
    }

    @Test
    void readsStructuredDetail() throws IOException {
        RawItem listed = RawItem.builder()
                .externalId("3479")
                .url("https://udruge.gov.hr/natjecaji-5503/5503?nid=3479")
                .title("Javni poziv za predlaganje Programa javnih potreba u kulturi Grada Belišća za 2027. godinu")
                .statusHint(FundingCallStatus.OPEN)
                .build();

        RawItem detailed = AssociationsOfficeParser.applyDetail(listed, fixture("detail.html"));

        assertThat(detailed.provider()).isEqualTo("Grad Belišće");
        assertThat(detailed.publishedAt()).isEqualTo(LocalDate.of(2026, 10, 8));
        assertThat(detailed.deadlineHint()).isEqualTo(LocalDate.of(2026, 11, 9));
        assertThat(detailed.totalAmountHint()).isEqualByComparingTo("21500");
        assertThat(detailed.minAmountHint()).isEqualByComparingTo("150");
        assertThat(detailed.maxAmountHint()).isEqualByComparingTo("8000");
        assertThat(detailed.countiesHint()).containsExactly("Osječko-baranjska");
        assertThat(detailed.targetGroupsHint()).containsExactly(TargetGroup.ASSOCIATION);
        assertThat(detailed.attachments()).singleElement()
                .satisfies(link -> assertThat(link.url()).isEqualTo("https://e-usluge.zio.hr/"));
        assertThat(detailed.statusHint()).isEqualTo(FundingCallStatus.OPEN);
    }

    @Test
    void detailGivesRuleExtractorStructuredValues() throws IOException {
        RawItem listed = RawItem.builder().externalId("3479").title("Javni poziv za predlaganje Programa javnih potreba u kulturi")
                .statusHint(FundingCallStatus.OPEN).build();

        ExtractedFields fields = new RuleExtractor().extract(AssociationsOfficeParser.applyDetail(listed, fixture("detail.html")));

        assertThat(fields.deadline()).isEqualTo(LocalDate.of(2026, 11, 9));
        assertThat(fields.totalAmount()).isEqualByComparingTo("21500");
        assertThat(fields.maxAmount()).isEqualByComparingTo("8000");
        assertThat(fields.targetGroups()).contains(TargetGroup.ASSOCIATION);
        assertThat(fields.status()).isEqualTo(FundingCallStatus.OPEN);
    }

    private static String fixture(String name) throws IOException {
        try (InputStream input = new ClassPathResource("fixtures/associations-office/" + name).getInputStream()) {
            return new String(input.readAllBytes(), StandardCharsets.UTF_8);
        }
    }
}
