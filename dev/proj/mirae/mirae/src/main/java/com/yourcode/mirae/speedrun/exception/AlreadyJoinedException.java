package com.yourcode.mirae.speedrun.exception;

public class AlreadyJoinedException extends RuntimeException {
    public AlreadyJoinedException(String sessionId, Long userId) {
        super("User " + userId + " already joined session: " + sessionId);
    }
}
