package com.yourcode.mirae.speedrun.exception;

public class SessionNotRunningException extends RuntimeException {
    public SessionNotRunningException(String sessionId) {
        super("Session is not running: " + sessionId);
    }
}
