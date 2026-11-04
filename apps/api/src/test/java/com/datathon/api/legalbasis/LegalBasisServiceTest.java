package com.datathon.api.legalbasis;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;

import com.datathon.api.fundingcall.FundingCall;
import com.datathon.api.fundingcall.FundingCallRepository;
import com.datathon.api.fundingcall.FundingCallStatus;
import com.datathon.api.fundingcall.LegalReference;
import com.datathon.api.regulation.Regulation;
import com.datathon.api.regulation.RegulationRepository;
import com.datathon.api.source.SourceCode;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class LegalBasisServiceTest {

    private static final String CALL_TEXT = "Na temelju Zakona o klimatskim promjenama i zaštiti ozonskog sloja "
            + "(»Narodne novine«, br. 127/19 i 67/25) Fond objavljuje";

    @Autowired
    private FundingCallRepository fundingCallRepository;

    @Autowired
    private RegulationRepository regulationRepository;

    @Autowired
    private EntityManager entityManager;

    private LegalBasisService legalBasisService;
    private Regulation amendment;

    @BeforeEach
    void setUp() {
        entityManager.createNativeQuery("TRUNCATE TABLE funding_call, regulation CASCADE").executeUpdate();
        legalBasisService = new LegalBasisService(regulationRepository, fundingCallRepository);
        amendment = regulation("2025-67-900",
                "Zakon o izmjenama i dopunama Zakona o klimatskim promjenama i zaštiti ozonskog sloja");
        regulation("2025-67-901", "Uredba o kakvoći mora");
    }

    @Test
    void citedIssueInTheDatabaseLinksToTheRegulationAndOtherIssuesOnlyToTheGazette() {
        FundingCall fundingCall = fundingCall("Poziv za klimu", CALL_TEXT);

        assertThat(legalBasisService.forFundingCall(fundingCall))
                .extracting(LegalBasis::gazetteNumber, LegalBasis::regulationId, LegalBasis::gazetteUrl)
                .containsExactly(
                        tuple("127/2019", null, "https://narodne-novine.nn.hr/eli/sluzbeni/2019/127"),
                        tuple("67/2025", amendment.getId(), "https://narodne-novine.nn.hr/eli/sluzbeni/2025/67"));
    }

    @Test
    void regulationListsVisibleCallsThatCiteItOpenFirst() {
        fundingCall("Zatvoreni poziv", CALL_TEXT).setStatus(FundingCallStatus.CLOSED);
        fundingCall("Otvoreni poziv", CALL_TEXT).setStatus(FundingCallStatus.OPEN);
        fundingCall("Skriveni poziv", CALL_TEXT).setHidden(true);
        fundingCall("Poziv bez navoda", "Rok je 30 dana.");
        entityManager.flush();

        assertThat(legalBasisService.citingFundingCalls(amendment))
                .extracting(CitingFundingCall::title)
                .containsExactly("Otvoreni poziv", "Zatvoreni poziv");
    }

    @Test
    void otherActInTheSameIssueIsNotCited() {
        fundingCall("Poziv za klimu", CALL_TEXT);
        entityManager.flush();
        Regulation otherAct = regulationRepository.findByExternalId("2025-67-901").orElseThrow();

        assertThat(legalBasisService.citingFundingCalls(otherAct)).isEmpty();
    }

    private Regulation regulation(String externalId, String title) {
        Regulation regulation = new Regulation(externalId, title);
        regulation.setGazetteNumber("67/2025");
        return regulationRepository.saveAndFlush(regulation);
    }

    private FundingCall fundingCall(String title, String text) {
        FundingCall fundingCall = new FundingCall(SourceCode.FZOEU, title, title);
        fundingCall.setSourceText(text);
        fundingCall.getLegalReferences().addAll(LegalReference.fromText(text));
        return fundingCallRepository.save(fundingCall);
    }
}
