package com.datathon.api.common;

/**
 * The message is for the log. The user message, when given, is the Croatian text the UI shows; without it the
 * message itself is shown (older callers already pass Croatian text).
 */
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
