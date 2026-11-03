package com.datathon.api.common;

/**
 * The message is for the log. The user message, when given, is the Croatian text the UI shows; without it the
 * message itself is shown (older callers already pass Croatian text).
 */
public class InvalidRequestException extends RuntimeException {

    private final String userMessage;

    public InvalidRequestException(String message) {
        this(message, message);
    }

    public InvalidRequestException(String message, String userMessage) {
        super(message);
        this.userMessage = userMessage;
    }

    public String getUserMessage() {
        return userMessage;
    }
}
