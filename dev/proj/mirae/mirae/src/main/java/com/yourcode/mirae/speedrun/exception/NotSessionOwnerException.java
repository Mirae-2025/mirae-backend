package com.yourcode.mirae.speedrun.exception;

public class NotSessionOwnerException extends RuntimeException {
    public NotSessionOwnerException(String sessionId) {
        super("Only the session owner can perform this action: " + sessionId);
    }
}
