package com.yourcode.mirae.speedrun.exception;

public class UserNotInSessionException extends RuntimeException {
    public UserNotInSessionException(String sessionId, Long userId) {
        super("User " + userId + " is not in session: " + sessionId);
    }
}
