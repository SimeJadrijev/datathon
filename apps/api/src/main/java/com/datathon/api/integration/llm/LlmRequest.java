package com.datathon.api.integration.llm;

import java.util.List;

/** {@code model} may be null: the configured default model is used. */
public record LlmRequest(String purpose, String model, String systemPrompt, String userText,
        ResponseSchema responseSchema, List<byte[]> pdfDocuments) {

    public LlmRequest {
        pdfDocuments = pdfDocuments == null ? List.of() : List.copyOf(pdfDocuments);
    }

    public LlmRequest(String purpose, String model, String systemPrompt, String userText,
            ResponseSchema responseSchema) {
        this(purpose, model, systemPrompt, userText, responseSchema, List.of());
    }

    public LlmRequest(String purpose, String systemPrompt, String userText, ResponseSchema responseSchema) {
        this(purpose, null, systemPrompt, userText, responseSchema, List.of());
    }
}
