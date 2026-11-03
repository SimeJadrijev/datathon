package com.datathon.api.eligibility;

import com.datathon.api.ai.BatchRunner;
import com.datathon.api.ai.ExtractionRunResult;
import com.datathon.api.ai.LlmUsageRepository;
import com.datathon.api.common.NotFoundException;
import com.datathon.api.fundingcall.FundingCall;
import com.datathon.api.fundingcall.FundingCallRepository;
import com.datathon.api.fundingcall.FundingCallStatus;
import com.datathon.api.integration.llm.LlmClient;
import com.datathon.api.integration.llm.LlmRequest;
import com.datathon.api.integration.llm.LlmResponseException;
import com.datathon.api.integration.llm.LlmUnavailableException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;
import tools.jackson.databind.JsonNode;

/** Writes the eligibility questions once per call, in advance, so the check itself needs one model call at most. */
@Service
@RequiredArgsConstructor
@Slf4j
public class EligibilityQuestionService {

    private static final int MAX_TEXT_CHARS = 40_000;
    private static final int MAX_BATCH = 1000;
    private static final int MAX_QUESTION_LENGTH = 300;
    private static final int MAX_OPTIONS = 6;

    private final FundingCallRepository fundingCallRepository;
    private final LlmUsageRepository llmUsageRepository;
    private final LlmClient llmClient;
    private final EligibilityQuestionStore questionStore;
    private final TransactionTemplate transactionTemplate;
    private final BatchRunner batchRunner;

    @Transactional(readOnly = true)
    public List<EligibilityQuestion> findQuestions(Long fundingCallId) {
        FundingCall call = fundingCallRepository.findById(fundingCallId)
                .orElseThrow(() -> new NotFoundException("Funding call " + fundingCallId + " does not exist",
                        NotFoundException.FUNDING_CALL_MESSAGE));
        if (isClosedForApplications(call)) {
            return List.of();
        }
        return questionStore.read(call.getEligibilityQuestions());
    }

    /** An empty list hides the eligibility panel; the detail page already says the deadline has passed. */
    private boolean isClosedForApplications(FundingCall call) {
        boolean isExpired = call.getDeadline() != null && call.getDeadline().isBefore(LocalDate.now());
        return isExpired || call.getStatus() == FundingCallStatus.CLOSED;
    }

    /** Starts a batch in the background. Throws when a batch is already running. */
    public void start(int limit) {
        batchRunner.start("generate-eligibility-questions", () -> run(limit));
    }

    public ExtractionRunResult run(int limit) {
        List<Long> ids = fundingCallRepository.findIdsForQuestions(LocalDate.now(), PageRequest.of(0, Math.min(limit, MAX_BATCH)));
        BigDecimal costBefore = llmUsageRepository.sumCostUsd();
        int done = 0;
        int failed = 0;
        int skipped = 0;
        for (Long id : ids) {
            try {
                generate(id);
                done++;
            } catch (LlmUnavailableException e) {
                skipped = ids.size() - done - failed;
                log.warn("Question generation stopped, the LLM is unavailable: {}", e.getMessage());
                break;
            } catch (LlmResponseException e) {
                failed++;
                log.warn("Question generation failed for funding call {}: {}", id, e.getMessage());
            }
        }
        BigDecimal cost = llmUsageRepository.sumCostUsd().subtract(costBefore);
        log.info("Eligibility questions: {} done, {} failed, {} skipped, {} USD", done, failed, skipped, cost);
        return new ExtractionRunResult(done, failed, skipped, cost);
    }

    void generate(Long id) {
        FundingCall call = fundingCallRepository.findById(id).orElseThrow();
        JsonNode answer = llmClient.complete(new LlmRequest(
                EligibilityQuestionSchema.PURPOSE,
                EligibilityQuestionSchema.SYSTEM_PROMPT,
                userText(call),
                EligibilityQuestionSchema.SCHEMA));
        String json = questionStore.write(parse(answer));
        transactionTemplate.executeWithoutResult(status ->
                fundingCallRepository.findById(id).orElseThrow().setEligibilityQuestions(json));
    }

    /** Ids are assigned here (q1, q2, ...), so they are always unique and stable. A broken question is dropped. */
    List<EligibilityQuestion> parse(JsonNode answer) {
        List<EligibilityQuestion> questions = new ArrayList<>();
        for (JsonNode item : answer.path("questions")) {
            if (questions.size() >= EligibilityQuestionSchema.MAX_QUESTIONS) {
                break;
            }
            EligibilityQuestion question = toQuestion("q" + (questions.size() + 1), item);
            if (question != null) {
                questions.add(question);
            }
        }
        return questions;
    }

    private EligibilityQuestion toQuestion(String id, JsonNode item) {
        String text = item.path("text").asString("").trim();
        EligibilityQuestionType type = parseType(item.path("type").asString(""));
        if (text.isEmpty() || text.length() > MAX_QUESTION_LENGTH || type == null) {
            return null;
        }
        List<String> options = new ArrayList<>();
        for (JsonNode option : item.path("options")) {
            String value = option.asString("").trim();
            if (!value.isEmpty() && options.size() < MAX_OPTIONS) {
                options.add(value);
            }
        }
        boolean isChoice = type == EligibilityQuestionType.CHOICE;
        if (isChoice && options.size() < 2) {
            return null;
        }
        return new EligibilityQuestion(id, text, type, isChoice ? options : List.of());
    }

    private EligibilityQuestionType parseType(String name) {
        try {
            return EligibilityQuestionType.valueOf(name);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    private String userText(FundingCall call) {
        String text = call.getSourceText();
        String nl = System.lineSeparator();
        return "Title: " + call.getTitle() + nl + nl + "--- TEXT ---" + nl
                + text.substring(0, Math.min(text.length(), MAX_TEXT_CHARS));
    }
}
