package com.datathon.api.integration.llm;

import tools.jackson.databind.JsonNode;

public interface LlmClient {

    /** Returns the model's answer as JSON that follows {@link LlmRequest#schema()} (tool use, not free text). */
    JsonNode complete(LlmRequest request);
}
