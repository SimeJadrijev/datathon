package com.datathon.api.integration.llm;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import tools.jackson.databind.json.JsonMapper;

@Configuration
@EnableConfigurationProperties(LlmProperties.class)
public class LlmConfig {

    @Bean
    LlmClient llmClient(LlmProperties properties, AnthropicClientFactory clientFactory,
                        MessageParamsFactory paramsFactory, LlmUsageService usageService, JsonMapper jsonMapper) {
        if (!properties.hasApiKey()) {
            return new NoopLlmClient();
        }

        return new AnthropicLlmClient(properties, paramsFactory, usageService, jsonMapper,
                clientFactory.create(properties));
    }
}
