package com.datathon.api.integration.llm;

/** The model cannot be used right now (no key, budget reached, API down). Callers skip the AI step. */
public class LlmUnavailableException extends RuntimeException {

    public LlmUnavailableException(String message) {
        super(message);
    }

    public LlmUnavailableException(String message, Throwable cause) {
        super(message, cause);
    }
}
