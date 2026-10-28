package com.datathon.api.integration.llm;

/** The model answered, but not with usable JSON. */
public class LlmResponseException extends RuntimeException {

    public LlmResponseException(String message) {
        super(message);
    }

    public LlmResponseException(String message, Throwable cause) {
        super(message, cause);
    }
}
