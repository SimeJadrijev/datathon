package com.datathon.api.integration.llm;

import tools.jackson.databind.JsonNode;

public interface LlmClient {

    JsonNode complete(LlmRequest request);
}
