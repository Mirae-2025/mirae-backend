package com.yourcode.mirae.speedrun.exception;

public class SessionExpiredException extends RuntimeException {
    public SessionExpiredException(String sessionId) {
        super("Session has expired: " + sessionId);
    }
}
