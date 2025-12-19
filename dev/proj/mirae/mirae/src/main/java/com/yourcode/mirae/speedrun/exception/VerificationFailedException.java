package com.yourcode.mirae.speedrun.exception;

public class VerificationFailedException extends RuntimeException {
    public VerificationFailedException(Long problemId) {
        super("Problem verification failed: " + problemId);
    }
}
