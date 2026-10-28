package com.datathon.api.integration.llm;

/** {@code model} may be null: the configured default model is used. */
public record LlmRequest(String purpose, String model, String systemPrompt, String userText,
        ResponseSchema responseSchema) {

    public LlmRequest(String purpose, String systemPrompt, String userText, ResponseSchema responseSchema) {
        this(purpose, null, systemPrompt, userText, responseSchema);
    }
}
