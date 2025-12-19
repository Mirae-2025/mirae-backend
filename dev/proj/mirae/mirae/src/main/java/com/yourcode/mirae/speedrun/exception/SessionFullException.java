package com.yourcode.mirae.speedrun.exception;

public class SessionFullException extends RuntimeException {
    public SessionFullException(String sessionId) {
        super("Session is full: " + sessionId);
    }
}
