package com.datathon.api.integration.llm;

import tools.jackson.databind.JsonNode;

public class NoopLlmClient implements LlmClient {

    @Override
    public JsonNode complete(LlmRequest request) {
        throw new LlmUnavailableException("ANTHROPIC_API_KEY is not set, the AI step is skipped");
    }
}
