package com.datathon.api.eligibility;

import com.datathon.api.common.InvalidRequestException;
import com.datathon.api.common.NotFoundException;
import com.datathon.api.fundingcall.FundingCall;
import com.datathon.api.fundingcall.FundingCallStatus;
import com.datathon.api.fundingcall.FundingCallRepository;
import com.datathon.api.integration.llm.LlmResponseException;
import com.datathon.api.integration.llm.LlmUnavailableException;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;

@Service
@RequiredArgsConstructor
@Slf4j
public class EligibilityCheckService {

    private static final int MAX_ANSWER_LENGTH = 200;

    private final FundingCallRepository fundingCallRepository;
    private final EligibilityQuestionStore questionStore;
    private final EligibilityJudge judge;
    private final EligibilityRuleChecker ruleChecker;
    private final TransactionTemplate transactionTemplate;

    public EligibilityCheckResponse check(Long fundingCallId, EligibilityCheckRequest request) {
        EligibilityContext context = loadContext(fundingCallId);
        Map<String, Object> answers = validatedAnswers(context.questions(), request.answers());
        if (context.isClosedForApplications(LocalDate.now())) {
            return ruleChecker.expired(context);
        }
        if (!answers.isEmpty()) {
            try {
                return judge.judge(context, answers);
            } catch (LlmUnavailableException | LlmResponseException e) {
                log.warn("Eligibility check for funding call {} falls back to rules: {}", fundingCallId, e.getMessage());
            }
        }
        return ruleChecker.check(context, answers, request);
    }

    private EligibilityContext loadContext(Long id) {
        return transactionTemplate.execute(status -> {
            FundingCall call = fundingCallRepository.findById(id)
                    .filter(found -> !found.isHidden() && found.getDuplicateOf() == null)
                    .orElseThrow(() -> new NotFoundException("Funding call " + id + " does not exist",
                            NotFoundException.FUNDING_CALL_MESSAGE));
            return new EligibilityContext(call.getId(), call.getTitle(), call.getUrl(),
                    call.getSourceText() != null ? call.getSourceText() : "",
                    call.getDeadline(),
                    call.getStatus() == FundingCallStatus.CLOSED,
                    new HashSet<>(call.getTargetGroups()),
                    new HashSet<>(call.getCounties()),
                    questionStore.read(call.getEligibilityQuestions()));
        });
    }

    private Map<String, Object> validatedAnswers(List<EligibilityQuestion> questions, Map<String, Object> answers) {
        Map<String, Object> valid = new LinkedHashMap<>();
        for (Map.Entry<String, Object> entry : answers.entrySet()) {
            boolean isKnown = questions.stream().anyMatch(question -> question.id().equals(entry.getKey()));
            if (!isKnown || !isAcceptableValue(entry.getValue())) {
                throw new InvalidRequestException("Unknown question id or invalid answer: " + entry.getKey(),
                        "Odgovor na pitanje " + entry.getKey() + " nije ispravan. Osvježi stranicu i pokušaj ponovno.");
            }
            valid.put(entry.getKey(), entry.getValue());
        }
        return valid;
    }

    private boolean isAcceptableValue(Object value) {
        if (value instanceof Boolean || value instanceof Number) {
            return true;
        }
        return value instanceof String text && !text.isBlank() && text.length() <= MAX_ANSWER_LENGTH;
    }
}
