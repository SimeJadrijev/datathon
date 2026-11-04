package com.datathon.api.ingest;

import static org.assertj.core.api.Assertions.assertThat;

import com.datathon.api.fundingcall.FundingCall;
import com.datathon.api.fundingcall.FundingCallRepository;
import com.datathon.api.ingest.extract.CallTitleFilter;
import com.datathon.api.ingest.extract.FundingFilter;
import com.datathon.api.source.SourceCode;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.context.annotation.Import;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import({NonCallSweeper.class, CallTitleFilter.class, FundingFilter.class})
class NonCallSweeperTest {

    @Autowired
    private NonCallSweeper sweeper;

    @Autowired
    private FundingCallRepository fundingCallRepository;

    @Autowired
    private EntityManager entityManager;

    @BeforeEach
    void setUp() {
        entityManager.createNativeQuery("TRUNCATE TABLE funding_call CASCADE").executeUpdate();
    }

    @Test
    void storedNewsThatIsNotACallIsHiddenAndListedCallsStay() {
        FundingCall decisions = save(SourceCode.APPRRR, "1", "Objavljene odluke iz Programa potpore pčelarima");
        FundingCall call = save(SourceCode.APPRRR, "2", "Objavljen Treći natječaj za intervenciju 73.02");
        FundingCall office = save(SourceCode.ASSOCIATIONS_OFFICE, "3", "Objavljene odluke o dodjeli potpora");

        sweeper.hideNonCalls();

        assertThat(decisions.isHidden()).isTrue();
        assertThat(decisions.getHiddenReason()).isEqualTo(NonCallSweeper.HIDDEN_REASON);
        assertThat(call.isHidden()).isFalse();
        assertThat(office.isHidden()).isFalse();
    }

    private FundingCall save(SourceCode source, String externalId, String title) {
        return fundingCallRepository.save(new FundingCall(source, externalId, title));
    }
}
