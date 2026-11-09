package com.datathon.api.common;

public class NotFoundException extends RuntimeException {

    public static final String FUNDING_CALL_MESSAGE = "Natječaj ne postoji.";

    private final String userMessage;

    public NotFoundException(String message) {
        this(message, message);
    }

    public NotFoundException(String message, String userMessage) {
        super(message);
        this.userMessage = userMessage;
    }

    public String getUserMessage() {
        return userMessage;
    }
}
