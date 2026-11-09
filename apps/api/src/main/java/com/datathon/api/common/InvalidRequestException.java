package com.datathon.api.common;

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
