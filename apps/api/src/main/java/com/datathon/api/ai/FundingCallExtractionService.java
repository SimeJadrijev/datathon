package com.datathon.api.ai;

import com.datathon.api.fundingcall.FundingCall;
import com.datathon.api.fundingcall.FundingCallRepository;
import com.datathon.api.integration.llm.LlmClient;
import com.datathon.api.integration.llm.LlmRequest;
import com.datathon.api.integration.llm.LlmResponseException;
import com.datathon.api.integration.llm.LlmUnavailableException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionTemplate;
import tools.jackson.databind.JsonNode;

@Service
@RequiredArgsConstructor
@Slf4j
public class FundingCallExtractionService {

    private static final int MAX_TEXT_CHARS = 40_000;
    private static final int MAX_BATCH = 1000;

    private final FundingCallRepository fundingCallRepository;
    private final LlmUsageRepository llmUsageRepository;
    private final LlmClient llmClient;
    private final FundingCallExtractionMerger merger;
    private final TransactionTemplate transactionTemplate;

    private final BatchRunner batchRunner;

    /** Starts a batch in the background. Throws when a batch is already running. */
    public void start(int limit) {
        batchRunner.start("extract-funding-calls", () -> run(limit));
    }

    public ExtractionRunResult run(int limit) {
        List<Long> ids = fundingCallRepository.findIdsForExtraction(PageRequest.of(0, Math.min(limit, MAX_BATCH)));
        BigDecimal costBefore = llmUsageRepository.sumCostUsd();
        int extracted = 0;
        int failed = 0;
        int skipped = 0;
        for (Long id : ids) {
            try {
                extract(id);
                extracted++;
            } catch (LlmUnavailableException e) {
                skipped = ids.size() - extracted - failed;
                log.warn("Extraction stopped, the LLM is unavailable: {}", e.getMessage());
                break;
            } catch (LlmResponseException e) {
                failed++;
                markAttempted(id);
                log.warn("Extraction failed for funding call {}, keeping the rule values: {}", id, e.getMessage());
            }
        }
        BigDecimal cost = llmUsageRepository.sumCostUsd().subtract(costBefore);
        log.info("Extraction batch: {} extracted, {} failed, {} skipped, {} USD", extracted, failed, skipped, cost);
        return new ExtractionRunResult(extracted, failed, skipped, cost);
    }

    /** A call the model cannot read is not offered again, otherwise every batch would pay for it once more. */
    private void markAttempted(Long id) {
        transactionTemplate.executeWithoutResult(status ->
                fundingCallRepository.findById(id).ifPresent(call -> call.setExtractedByLlm(true)));
    }

    /** The model call runs outside any transaction, so a slow call never holds a database connection. */
    void extract(Long id) {
        FundingCall call = fundingCallRepository.findById(id).orElseThrow();
        JsonNode answer = llmClient.complete(new LlmRequest(
                FundingCallExtractionSchema.PURPOSE,
                FundingCallExtractionSchema.SYSTEM_PROMPT,
                userText(call),
                FundingCallExtractionSchema.SCHEMA));
        FundingCallExtraction extraction = FundingCallExtraction.from(answer);
        transactionTemplate.executeWithoutResult(status -> {
            FundingCall managed = fundingCallRepository.findById(id).orElseThrow();
            merger.apply(managed, extraction);
        });
    }

    private String userText(FundingCall call) {
        String text = call.getSourceText();
        return "Today's date: " + LocalDate.now() + "\n"
                + "Source: " + call.getSource() + "\n"
                + "Title: " + call.getTitle() + "\n"
                + "URL: " + call.getUrl() + "\n"
                + "Provider (per source): " + (call.getProvider() != null ? call.getProvider() : "unknown") + "\n\n"
                + "--- TEXT ---\n" + text.substring(0, Math.min(text.length(), MAX_TEXT_CHARS));
    }
}
