package com.datathon.api.ingest.http;

public class HttpFetchException extends RuntimeException {

    private final int statusCode;

    public HttpFetchException(String message) {
        super(message);
        this.statusCode = 0;
    }

    public HttpFetchException(String message, int statusCode) {
        super(message);
        this.statusCode = statusCode;
    }

    public HttpFetchException(String message, Throwable cause) {
        super(message, cause);
        this.statusCode = 0;
    }

    /** The HTTP status the server answered with, or 0 when there was no answer. */
    public int statusCode() {
        return statusCode;
    }
}
