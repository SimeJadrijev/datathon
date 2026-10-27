package com.datathon.api.seed;

import static org.assertj.core.api.Assertions.assertThat;

import com.datathon.api.fundingcall.FundingCallRepository;
import com.datathon.api.fundingcall.FundingCallStatus;
import com.datathon.api.fundingcall.FundingCallType;
import com.datathon.api.regulation.RegulationRepository;
import com.datathon.api.regulation.RegulationType;
import com.datathon.api.source.SourceCode;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.context.annotation.Import;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(SeedService.class)
class SeedServiceTest {

    private static final LocalDate SNAPSHOT_DATE = LocalDate.of(2026, 10, 26);

    @Autowired
    private SeedService seedService;

    @Autowired
    private FundingCallRepository fundingCallRepository;

    @Autowired
    private RegulationRepository regulationRepository;

    @Test
    void secondImportOfSameSnapshotCreatesNoDuplicates() {
        long before = fundingCallRepository.count();
        var snapshot = new FundingCallSnapshot(SNAPSHOT_DATE, List.of(fundingCall("seed-test-1"), fundingCall("seed-test-2")));

        seedService.importFundingCalls(snapshot);
        seedService.importFundingCalls(snapshot);

        assertThat(fundingCallRepository.count()).isEqualTo(before + 2);
        assertThat(fundingCallRepository.findBySourceAndExternalId(SourceCode.ASSOCIATIONS_OFFICE, "seed-test-1"))
                .hasValueSatisfying(call -> assertThat(call.getCounties()).containsExactly("ALL"));
    }

    @Test
    void secondImportOfRegulationsCreatesNoDuplicates() {
        long before = regulationRepository.count();
        var snapshot = new RegulationSnapshot(SNAPSHOT_DATE, List.of(regulation()));

        seedService.importRegulations(snapshot);
        seedService.importRegulations(snapshot);

        assertThat(regulationRepository.count()).isEqualTo(before + 1);
    }

    private static SeedFundingCall fundingCall(String externalId) {
        return new SeedFundingCall(SourceCode.ASSOCIATIONS_OFFICE, externalId, null, "Natječaj za udruge",
                "Grad Rijeka", FundingCallType.GRANT, FundingCallStatus.OPEN, SNAPSHOT_DATE, null,
                null, null, null, Set.of(), Set.of(), Set.of("ALL"), Set.of(), null,
                List.of(), List.of(), null, false, Set.of(), null, null);
    }

    private static SeedRegulation regulation() {
        return new SeedRegulation("9999-1-1", "Zakon o testiranju", RegulationType.LAW, "1/9999", 1,
                SNAPSHOT_DATE, null, null, null, null, Set.of(), false, null);
    }
}
