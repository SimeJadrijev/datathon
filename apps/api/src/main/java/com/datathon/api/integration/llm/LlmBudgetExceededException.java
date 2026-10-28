package com.datathon.api.integration.llm;

public class LlmBudgetExceededException extends LlmUnavailableException {

    public LlmBudgetExceededException(String message) {
        super(message);
    }
}
