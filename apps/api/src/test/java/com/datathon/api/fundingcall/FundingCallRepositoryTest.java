package com.datathon.api.fundingcall;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.datathon.api.source.SourceCode;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.dao.DataIntegrityViolationException;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class FundingCallRepositoryTest {

    @Autowired
    private FundingCallRepository fundingCallRepository;

    @Test
    void rejectsSecondCallWithSameSourceAndExternalId() {
        fundingCallRepository.saveAndFlush(new FundingCall(SourceCode.FZOEU, "test-275", "Javni poziv EnU-2/26"));

        var duplicate = new FundingCall(SourceCode.FZOEU, "test-275", "Isti poziv drugi put");

        assertThatThrownBy(() -> fundingCallRepository.saveAndFlush(duplicate))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void allowsSameExternalIdFromAnotherSource() {
        fundingCallRepository.saveAndFlush(new FundingCall(SourceCode.FZOEU, "test-275", "FZOEU poziv"));
        fundingCallRepository.saveAndFlush(new FundingCall(SourceCode.HZZ, "test-275", "HZZ mjera"));

        assertThat(fundingCallRepository.findBySourceAndExternalId(SourceCode.HZZ, "test-275")).isPresent();
    }
}
