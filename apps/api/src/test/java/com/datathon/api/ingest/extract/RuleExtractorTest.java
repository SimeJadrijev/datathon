package com.datathon.api.ingest.extract;

import static org.assertj.core.api.Assertions.assertThat;

import com.datathon.api.fundingcall.FundingCallStatus;
import com.datathon.api.fundingcall.FundingCallType;
import com.datathon.api.fundingcall.TargetGroup;
import com.datathon.api.ingest.RawItem;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import org.junit.jupiter.api.Test;

class RuleExtractorTest {

    private final RuleExtractor extractor = new RuleExtractor(
            Clock.fixed(Instant.parse("2026-10-28T10:00:00Z"), ZoneId.of("Europe/Zagreb")));

    @Test
    void cancelledCallIsClosedEvenWhenTheSourceSaysOpen() {
        RawItem item = RawItem.builder()
                .title("PONIŠTEN Natječaj za financiranje programa i projekata iz područja zdravstva")
                .statusHint(FundingCallStatus.OPEN)
                .build();

        assertThat(extractor.extract(item).status()).isEqualTo(FundingCallStatus.CLOSED);
    }

    @Test
    void derivesStatusAndTypeWhenTheSourceGivesNoHints() {
        RawItem item = RawItem.builder()
                .title("Javni poziv za dodjelu potpora za samozapošljavanje")
                .publishedAt(LocalDate.of(2026, 10, 1))
                .text("Rok za podnošenje zahtjeva je 15. 11. 2026. Pravo imaju nezaposlene osobe i roditelji.")
                .build();

        ExtractedFields fields = extractor.extract(item);

        assertThat(fields.type()).isEqualTo(FundingCallType.SUBSIDY);
        assertThat(fields.deadline()).isEqualTo(LocalDate.of(2026, 11, 15));
        assertThat(fields.status()).isEqualTo(FundingCallStatus.OPEN);
        assertThat(fields.targetGroups()).containsExactly(TargetGroup.CITIZEN);
    }

    @Test
    void pastDeadlineMeansClosed() {
        RawItem item = RawItem.builder()
                .title("Natječaj za stipendije")
                .text("Rok za prijavu: 1. 10. 2026.")
                .build();

        assertThat(extractor.extract(item).status()).isEqualTo(FundingCallStatus.CLOSED);
    }

    @Test
    void openCallWithoutDeadlineForAPastYearIsClosed() {
        RawItem item = RawItem.builder()
                .title("Javni poziv za financiranje jednokratnih aktivnosti udruga na području grada Vrgorca za 2023. godinu")
                .statusHint(FundingCallStatus.OPEN)
                .build();

        assertThat(extractor.extract(item).status()).isEqualTo(FundingCallStatus.CLOSED);
    }

    @Test
    void suspendedCallIsClosedAndCurrentYearStaysOpen() {
        RawItem suspended = RawItem.builder().title("OBUSTAVA - Javni natječaj za financiranje programa")
                .statusHint(FundingCallStatus.OPEN).build();
        RawItem current = RawItem.builder().title("Javni poziv za stipendije za akademsku godinu 2025./2026.")
                .statusHint(FundingCallStatus.OPEN).build();

        assertThat(extractor.extract(suspended).status()).isEqualTo(FundingCallStatus.CLOSED);
        assertThat(extractor.extract(current).status()).isEqualTo(FundingCallStatus.OPEN);
    }

    @Test
    void announcementWhoseDeadlinePassedIsClosed() {
        RawItem item = RawItem.builder().title("Najava javnog poziva za potpore").text("Rok za prijavu: 1. 10. 2026.").build();

        assertThat(extractor.extract(item).status()).isEqualTo(FundingCallStatus.CLOSED);
    }

    @Test
    void accreditationIsNotALoan() {
        RawItem item = RawItem.builder().title("Javni poziv za akreditaciju u programu Erasmus+").build();

        assertThat(extractor.extract(item).type()).isEqualTo(FundingCallType.PUBLIC_CALL);
    }
}
