package com.datathon.api.ai;

import com.datathon.api.common.ExternalSourceException;
import com.datathon.api.integration.llm.LlmClient;
import com.datathon.api.integration.llm.LlmRequest;
import com.datathon.api.integration.llm.LlmResponseException;
import com.datathon.api.integration.llm.LlmUnavailableException;
import com.datathon.api.integration.narodnenovine.NarodneNovineClient;
import com.datathon.api.regulation.Regulation;
import com.datathon.api.regulation.RegulationRepository;
import java.math.BigDecimal;
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
public class RegulationSummaryService {

    private static final int HEAD_CHARS = 15_000;
    private static final int TAIL_CHARS = 3_000;
    private static final int MAX_WHOLE_CHARS = 25_000;
    private static final int ENTRY_CONTEXT_CHARS = 800;
    private static final String ENTRY_INTO_FORCE = "stupa na snagu";
    private static final int MAX_BATCH = 300;
    private static final String NL = System.lineSeparator();

    private final RegulationRepository regulationRepository;
    private final LlmUsageRepository llmUsageRepository;
    private final LlmClient llmClient;
    private final NarodneNovineClient narodneNovineClient;
    private final TransactionTemplate transactionTemplate;
    private final BatchRunner batchRunner;

    public void start(int limit) {
        batchRunner.start("summarize-regulations", () -> run(limit));
    }

    public ExtractionRunResult run(int limit) {
        List<Long> ids = regulationRepository.findIdsForSummary(PageRequest.of(0, Math.min(limit, MAX_BATCH)));
        BigDecimal costBefore = llmUsageRepository.sumCostUsd();
        int summarized = 0;
        int failed = 0;
        int skipped = 0;
        for (Long id : ids) {
            try {
                summarize(id);
                summarized++;
            } catch (LlmUnavailableException e) {
                skipped = ids.size() - summarized - failed;
                log.warn("Summaries stopped, the LLM is unavailable: {}", e.getMessage());
                break;
            } catch (LlmResponseException | ExternalSourceException e) {
                failed++;
                log.warn("Summary failed for regulation {}: {}", id, e.getMessage());
            }
        }
        BigDecimal cost = llmUsageRepository.sumCostUsd().subtract(costBefore);
        log.info("Regulation summaries: {} done, {} failed, {} skipped, {} USD", summarized, failed, skipped, cost);
        return new ExtractionRunResult(summarized, failed, skipped, cost);
    }

    void summarize(Long id) {
        Regulation regulation = regulationRepository.findById(id).orElseThrow();
        String text = narodneNovineClient.fetchText(regulation.getUrl());
        JsonNode answer = llmClient.complete(new LlmRequest(
                RegulationSummarySchema.PURPOSE,
                RegulationSummarySchema.SYSTEM_PROMPT,
                userText(regulation, text),
                RegulationSummarySchema.SCHEMA));
        RegulationSummary summary = RegulationSummary.from(answer);
        transactionTemplate.executeWithoutResult(status -> {
            Regulation managed = regulationRepository.findById(id).orElseThrow();
            apply(managed, summary);
        });
    }

    private void apply(Regulation regulation, RegulationSummary summary) {
        if (summary.summary() != null) {
            regulation.setSummary(summary.summary());
        }
        if (regulation.getAreas().isEmpty()) {
            regulation.getAreas().addAll(summary.areas());
        }
    }

    private String userText(Regulation regulation, String text) {
        return "Title: " + regulation.getTitle() + NL
                + "Gazette: NN " + regulation.getGazetteNumber() + NL + NL
                + (text.length() <= MAX_WHOLE_CHARS ? "--- TEXT ---" + NL + text : excerpts(text));
    }

    private String excerpts(String text) {
        String head = text.substring(0, HEAD_CHARS);
        String tail = text.substring(text.length() - TAIL_CHARS);
        int entry = text.lastIndexOf(ENTRY_INTO_FORCE);
        String entryExcerpt = entry < 0 ? "" : text.substring(
                Math.max(0, entry - ENTRY_CONTEXT_CHARS), Math.min(text.length(), entry + ENTRY_CONTEXT_CHARS));
        return "--- TEXT (START) ---" + NL + head
                + (entryExcerpt.isEmpty() ? "" : NL + NL + "--- TEXT (ENTRY INTO FORCE) ---" + NL + entryExcerpt)
                + NL + NL + "--- TEXT (END) ---" + NL + tail;
    }
}
