package com.yourcode.mirae.speedrun.exception;

public class DuplicateSubmitException extends RuntimeException {
    public DuplicateSubmitException(Long problemId) {
        super("Problem already submitted: " + problemId);
    }
}
