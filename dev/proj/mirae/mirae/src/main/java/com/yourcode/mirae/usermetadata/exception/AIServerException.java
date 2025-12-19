package com.yourcode.mirae.usermetadata.exception;

import lombok.Getter;

@Getter
public class AIServerException extends RuntimeException {
    private final int statusCode;
    private final String errorCode;

    public AIServerException(String message) {
        super(message);
        this.statusCode = 500;
        this.errorCode = "AI_SERVER_ERROR";
    }

    public AIServerException(String message, int statusCode) {
        super(message);
        this.statusCode = statusCode;
        this.errorCode = "AI_SERVER_ERROR";
    }

    public AIServerException(String message, int statusCode, String errorCode) {
        super(message);
        this.statusCode = statusCode;
        this.errorCode = errorCode;
    }

    public AIServerException(String message, Throwable cause) {
        super(message, cause);
        this.statusCode = 503;
        this.errorCode = "AI_SERVER_UNAVAILABLE";
    }
}
