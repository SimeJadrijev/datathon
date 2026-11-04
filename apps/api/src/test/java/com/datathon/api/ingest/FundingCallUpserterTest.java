package com.datathon.api.ingest;

import static org.assertj.core.api.Assertions.assertThat;

import com.datathon.api.fundingcall.FundingCall;
import com.datathon.api.fundingcall.FundingCallRepository;
import com.datathon.api.fundingcall.FundingCallStatus;
import com.datathon.api.ingest.FundingCallUpserter.Result;
import com.datathon.api.ingest.extract.ExtractedFields;
import com.datathon.api.source.SourceCode;
import jakarta.persistence.EntityManager;
import java.time.LocalDate;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.context.annotation.Import;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(FundingCallUpserter.class)
class FundingCallUpserterTest {

    private static final LocalDate TODAY = LocalDate.now();

    @Autowired
    private FundingCallUpserter upserter;

    @Autowired
    private FundingCallRepository fundingCallRepository;

    @Autowired
    private EntityManager entityManager;

    @BeforeEach
    void setUp() {
        entityManager.createNativeQuery("TRUNCATE TABLE funding_call CASCADE").executeUpdate();
    }

    @Test
    void deadlineReadByTheModelKeepsTheCallOpenOnTheNextIngest() {
        RawItem item = item(null);
        upserter.upsert(SourceCode.FZOEU, item, fields(FundingCallStatus.CLOSED, TODAY.minusDays(10)));
        FundingCall call = stored();
        call.setDeadline(TODAY.plusDays(30));
        call.setStatus(FundingCallStatus.OPEN);
        call.getAiFields().add("deadline");

        Result result = upserter.upsert(SourceCode.FZOEU, item, fields(FundingCallStatus.CLOSED, TODAY.minusDays(10)));

        assertThat(result).isEqualTo(Result.UNCHANGED);
        assertThat(stored().getStatus()).isEqualTo(FundingCallStatus.OPEN);
        assertThat(stored().getDeadline()).isEqualTo(TODAY.plusDays(30));
    }

    @Test
    void extendedDeadlineFromStructuredDataIsStoredWhenTheTextIsTheSame() {
        RawItem item = item(null);
        upserter.upsert(SourceCode.EU_FUNDS_COHESION, item, fields(FundingCallStatus.OPEN, TODAY.plusDays(5)));

        upserter.upsert(SourceCode.EU_FUNDS_COHESION, item, fields(FundingCallStatus.OPEN, TODAY.plusDays(40)));

        FundingCall call = fundingCallRepository.findBySourceAndExternalId(SourceCode.EU_FUNDS_COHESION, "upsert-1")
                .orElseThrow();
        assertThat(call.getDeadline()).isEqualTo(TODAY.plusDays(40));
    }

    @Test
    void structuredDeadlineReplacesTheOneTheModelClearedOnTheNextIngest() {
        RawItem item = item(null).toBuilder().deadlineHint(TODAY.plusDays(20)).build();
        upserter.upsert(SourceCode.FZOEU, item, fields(FundingCallStatus.OPEN, TODAY.plusDays(20)));
        FundingCall call = stored();
        call.setDeadline(null);
        call.setDeadlineFromSource(false);
        call.getAiFields().add("deadline");

        Result result = upserter.upsert(SourceCode.FZOEU, item, fields(FundingCallStatus.OPEN, TODAY.plusDays(20)));

        assertThat(result).isEqualTo(Result.UNCHANGED);
        assertThat(stored().getDeadline()).isEqualTo(TODAY.plusDays(20));
        assertThat(stored().isDeadlineFromSource()).isTrue();
        assertThat(stored().getAiFields()).doesNotContain("deadline");
    }

    @Test
    void continuousMeasureStaysOpenWithoutTheDeadlineTheModelFound() {
        RawItem item = item(FundingCallStatus.OPEN).toBuilder().deadlineNoteHint("kontinuirano, bez roka").build();
        ExtractedFields continuous = new ExtractedFields(null, FundingCallStatus.OPEN, null, null,
                "kontinuirano, bez roka", null, null, null, Set.of(), Set.of(), Set.of(), Set.of());
        upserter.upsert(SourceCode.HZZ, item, continuous);
        FundingCall call = fundingCallRepository.findBySourceAndExternalId(SourceCode.HZZ, "upsert-1").orElseThrow();
        call.setDeadline(TODAY.minusDays(30));
        call.setStatus(FundingCallStatus.CLOSED);
        call.getAiFields().add("deadline");

        upserter.upsert(SourceCode.HZZ, item, continuous);

        FundingCall stored = fundingCallRepository.findBySourceAndExternalId(SourceCode.HZZ, "upsert-1").orElseThrow();
        assertThat(stored.getStatus()).isEqualTo(FundingCallStatus.OPEN);
        assertThat(stored.getDeadline()).isNull();
        assertThat(stored.isDeadlineFromSource()).isTrue();
    }

    @Test
    void sourceSayingClosedWinsOverTheModelDeadline() {
        upserter.upsert(SourceCode.FZOEU, item(null), fields(FundingCallStatus.OPEN, TODAY.plusDays(5)));
        FundingCall call = stored();
        call.setDeadline(TODAY.plusDays(30));
        call.getAiFields().add("deadline");

        upserter.upsert(SourceCode.FZOEU, item(FundingCallStatus.CLOSED), fields(FundingCallStatus.CLOSED, TODAY.plusDays(5)));

        assertThat(stored().getStatus()).isEqualTo(FundingCallStatus.CLOSED);
    }

    @Test
    void unknownStatusDoesNotClearAKnownOne() {
        RawItem item = item(null);
        upserter.upsert(SourceCode.FZOEU, item, fields(FundingCallStatus.ANNOUNCED, null));

        upserter.upsert(SourceCode.FZOEU, item.toBuilder().text("Novi tekst").build(), fields(null, null));

        assertThat(stored().getStatus()).isEqualTo(FundingCallStatus.ANNOUNCED);
    }

    private FundingCall stored() {
        return fundingCallRepository.findBySourceAndExternalId(SourceCode.FZOEU, "upsert-1").orElseThrow();
    }

    private static RawItem item(FundingCallStatus statusHint) {
        return RawItem.builder()
                .externalId("upsert-1")
                .url("https://www.fzoeu.hr/test/upsert-1")
                .title("Javni poziv za sufinanciranje solarnih elektrana")
                .statusHint(statusHint)
                .text("Opis poziva.")
                .build();
    }

    private static ExtractedFields fields(FundingCallStatus status, LocalDate deadline) {
        return new ExtractedFields(null, status, null, deadline, null, null, null, null, Set.of(), Set.of(), Set.of(),
                Set.of());
    }
}
