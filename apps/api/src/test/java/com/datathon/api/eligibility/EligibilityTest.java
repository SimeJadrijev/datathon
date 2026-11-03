package com.datathon.api.eligibility;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.datathon.api.common.InvalidRequestException;
import com.datathon.api.common.NotFoundException;
import com.datathon.api.fundingcall.FundingCall;
import com.datathon.api.fundingcall.FundingCallRepository;
import com.datathon.api.fundingcall.TargetGroup;
import com.datathon.api.integration.llm.LlmClient;
import com.datathon.api.integration.llm.LlmProperties;
import com.datathon.api.integration.llm.LlmUnavailableException;
import com.datathon.api.source.SourceCode;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.Test;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;
import tools.jackson.databind.json.JsonMapper;

class EligibilityTest {

    private static final String CALL_TEXT = "Korisnici su punoljetne fizičke osobe s prebivalištem u Republici Hrvatskoj. "
            + "Rok za prijavu je 1. prosinca 2026.";
    private static final String QUOTE = "punoljetne fizičke osobe s prebivalištem u Republici Hrvatskoj";

    private final JsonMapper jsonMapper = JsonMapper.builder().build();
    private final LlmProperties properties = new LlmProperties("key", "http://localhost", "haiku", "sonnet",
            BigDecimal.ONE, 1000, 1, 1, 1, Map.of());

    private EligibilityContext context() {
        List<EligibilityQuestion> questions = List.of(
                new EligibilityQuestion("q1", "Jeste li punoljetni?", EligibilityQuestionType.YES_NO, List.of()));
        return new EligibilityContext(1L, "Test poziv", "https://example.hr/poziv", CALL_TEXT,
                LocalDate.of(2026, 12, 1), java.util.Set.of(TargetGroup.CITIZEN), java.util.Set.of("ALL"), questions);
    }

    private EligibilityJudge judgeAnswering(String json) {
        LlmClient client = request -> jsonMapper.readTree(json);
        return new EligibilityJudge(client, properties);
    }

    @Test
    void quotedReasonKeepsTheVerdictAndCarriesDisclaimerAndSource() {
        EligibilityJudge judge = judgeAnswering("""
                {"verdict":"LIKELY_ELIGIBLE","reasons":[{"text":"Punoljetni ste.","quote":"%s"}],"missingInfo":[]}"""
                .formatted(QUOTE));

        EligibilityCheckResponse response = judge.judge(context(), Map.of("q1", true));

        assertThat(response.verdict()).isEqualTo(EligibilityVerdict.LIKELY_ELIGIBLE);
        assertThat(response.reasons()).singleElement().satisfies(reason -> assertThat(reason.quote()).isEqualTo(QUOTE));
        assertThat(response.disclaimer()).startsWith("Informativna procjena");
        assertThat(response.sourceUrl()).isEqualTo("https://example.hr/poziv");
    }

    @Test
    void quoteThatIsNotInTheCallTextBecomesUnclear() {
        EligibilityJudge judge = judgeAnswering("""
                {"verdict":"LIKELY_ELIGIBLE","reasons":[{"text":"Ispunjavate uvjet.","quote":"Svi građani Europske unije imaju pravo na potporu"}],"missingInfo":[]}""");

        EligibilityCheckResponse response = judge.judge(context(), Map.of("q1", true));

        assertThat(response.verdict()).isEqualTo(EligibilityVerdict.UNCLEAR);
        assertThat(response.reasons()).isEmpty();
        assertThat(response.missingInfo()).isNotEmpty();
    }

    @Test
    void modelDownGivesRuleBasedUnclearAnswerInsteadOfAnError() {
        LlmClient down = request -> {
            throw new LlmUnavailableException("down");
        };
        EligibilityCheckService service = serviceFor(callWithDeadline(LocalDate.now().plusDays(30)), down);

        EligibilityCheckResponse response = service.check(1L,
                new EligibilityCheckRequest(Map.of("q1", true), TargetGroup.COMPANY, null));

        assertThat(response.verdict()).isEqualTo(EligibilityVerdict.UNCLEAR);
        assertThat(response.reasons()).singleElement()
                .satisfies(reason -> assertThat(reason.text()).contains("ciljnoj skupini"));
        assertThat(response.disclaimer()).isNotBlank();
    }

    @Test
    void expiredDeadlineIsNeverLikelyEligibleAndSkipsTheModel() {
        AtomicInteger modelCalls = new AtomicInteger();
        LlmClient eligible = request -> {
            modelCalls.incrementAndGet();
            return jsonMapper.readTree("""
                    {"verdict":"LIKELY_ELIGIBLE","reasons":[{"text":"Punoljetni ste.","quote":"%s"}],"missingInfo":[]}"""
                    .formatted(QUOTE));
        };
        EligibilityCheckService service = serviceFor(callWithDeadline(LocalDate.now().minusDays(1)), eligible);

        EligibilityCheckResponse response = service.check(1L, new EligibilityCheckRequest(Map.of("q1", true), null, null));

        assertThat(modelCalls).hasValue(0);
        assertThat(response.verdict()).isEqualTo(EligibilityVerdict.LIKELY_NOT_ELIGIBLE);
        assertThat(response.reasons()).singleElement()
                .satisfies(reason -> assertThat(reason.text()).contains("je istekao"));
        assertThat(response.disclaimer()).isEqualTo(EligibilityCheckResponse.DISCLAIMER);
        assertThat(response.sourceUrl()).isEqualTo("https://example.hr/poziv");
    }

    @Test
    void unknownQuestionIdIsRejectedWithCroatianUserMessage() {
        EligibilityCheckService service = serviceFor(callWithDeadline(null), request -> jsonMapper.createObjectNode());

        assertThatThrownBy(() -> service.check(1L, new EligibilityCheckRequest(Map.of("zzz", true), null, null)))
                .isInstanceOfSatisfying(InvalidRequestException.class,
                        e -> assertThat(e.getUserMessage()).contains("nije ispravan"));
    }

    @Test
    void unknownCallGivesCroatianNotFoundMessage() {
        EligibilityCheckService service = serviceFor(callWithDeadline(null), request -> jsonMapper.createObjectNode());

        assertThatThrownBy(() -> service.check(2L, new EligibilityCheckRequest(Map.of(), null, null)))
                .isInstanceOfSatisfying(NotFoundException.class,
                        e -> assertThat(e.getUserMessage()).isEqualTo("Natječaj ne postoji."));
    }

    private FundingCall callWithDeadline(LocalDate deadline) {
        FundingCall call = new FundingCall(SourceCode.FZOEU, "1", "Test poziv");
        call.setUrl("https://example.hr/poziv");
        call.setSourceText(CALL_TEXT);
        call.setDeadline(deadline);
        call.getTargetGroups().add(TargetGroup.CITIZEN);
        call.setEligibilityQuestions(new EligibilityQuestionStore(jsonMapper).write(context().questions()));
        return call;
    }

    private EligibilityCheckService serviceFor(FundingCall call, LlmClient client) {
        FundingCallRepository repository = mock(FundingCallRepository.class);
        when(repository.findById(1L)).thenReturn(Optional.of(call));
        TransactionTemplate transactionTemplate = mock(TransactionTemplate.class);
        doAnswer(invocation -> ((TransactionCallback<?>) invocation.getArgument(0)).doInTransaction(null))
                .when(transactionTemplate).execute(any());
        return new EligibilityCheckService(repository, new EligibilityQuestionStore(jsonMapper),
                new EligibilityJudge(client, properties), new EligibilityRuleChecker(), transactionTemplate);
    }
}
