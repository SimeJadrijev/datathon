package com.datathon.api.integration.llm;

import java.math.BigDecimal;
import java.util.Map;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("llm")
@Getter
@RequiredArgsConstructor
public class LlmProperties {

    private final String apiKey;
    private final String baseUrl;
    private final String model;
    private final String eligibilityModel;
    private final BigDecimal budgetUsd;
    private final int maxTokens;
    private final int connectTimeoutSeconds;
    private final int readTimeoutSeconds;
    private final int maxAttempts;
    private final Map<String, Price> prices;

    public boolean hasApiKey() {
        return apiKey != null && !apiKey.isBlank();
    }

    public record Price(BigDecimal input, BigDecimal output) {
    }
    
}
