package com.datathon.api.integration.llm;

import com.datathon.api.ai.LlmUsage;
import com.datathon.api.ai.LlmUsageRepository;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class LlmUsageService {

    private static final BigDecimal MILLION = BigDecimal.valueOf(1_000_000);

    private final LlmUsageRepository usageRepository;
    private final LlmProperties properties;

    @Transactional(readOnly = true)
    public void assertBudgetAvailable() {
        BigDecimal spent = usageRepository.sumCostUsd();
        
        if (spent.compareTo(properties.getBudgetUsd()) >= 0) {
            throw new LlmBudgetExceededException(
                    "LLM budget reached (" + spent + " of " + properties.getBudgetUsd() + " USD)");
        }
    }

    @Transactional
    public LlmUsage record(String model, String purpose, int inputTokens, int outputTokens) {
        LlmUsage usage = new LlmUsage(model, purpose, Instant.now());

        usage.setInputTokens(inputTokens);
        usage.setOutputTokens(outputTokens);
        usage.setCostUsd(cost(model, inputTokens, outputTokens));

        log.info("LLM {} ({}): {} in, {} out tokens, {} USD", purpose, model, inputTokens, outputTokens,
                usage.getCostUsd());

        return usageRepository.save(usage);
    }

    private BigDecimal cost(String model, int inputTokens, int outputTokens) {
        LlmProperties.Price price = properties.getPrices().get(model);
        if (price == null) {
            log.warn("No price configured for model {}, cost is counted as 0", model);
            return BigDecimal.ZERO;
        }

        BigDecimal input = price.input().multiply(BigDecimal.valueOf(inputTokens));
        BigDecimal output = price.output().multiply(BigDecimal.valueOf(outputTokens));

        return input.add(output).divide(MILLION, 6, RoundingMode.HALF_UP);
    }
}
