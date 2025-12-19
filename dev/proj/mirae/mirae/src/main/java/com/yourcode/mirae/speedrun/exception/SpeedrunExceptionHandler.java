package com.yourcode.mirae.speedrun.exception;

import com.yourcode.mirae.speedrun.dto.ErrorResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice(basePackages = "com.yourcode.mirae.speedrun")
@Slf4j
public class SpeedrunExceptionHandler {

    @ExceptionHandler(SessionNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleSessionNotFound(SessionNotFoundException e) {
        log.warn("Session not found: {}", e.getMessage());
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(new ErrorResponse("SESSION_NOT_FOUND", e.getMessage()));
    }

    @ExceptionHandler(SessionNotRunningException.class)
    public ResponseEntity<ErrorResponse> handleSessionNotRunning(SessionNotRunningException e) {
        log.warn("Session not running: {}", e.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ErrorResponse("SESSION_NOT_RUNNING", e.getMessage()));
    }

    @ExceptionHandler(SessionFullException.class)
    public ResponseEntity<ErrorResponse> handleSessionFull(SessionFullException e) {
        log.warn("Session full: {}", e.getMessage());
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(new ErrorResponse("SESSION_FULL", e.getMessage()));
    }

    @ExceptionHandler(AlreadyJoinedException.class)
    public ResponseEntity<ErrorResponse> handleAlreadyJoined(AlreadyJoinedException e) {
        log.warn("Already joined: {}", e.getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(new ErrorResponse("ALREADY_JOINED", e.getMessage()));
    }

    @ExceptionHandler(DuplicateSubmitException.class)
    public ResponseEntity<ErrorResponse> handleDuplicateSubmit(DuplicateSubmitException e) {
        log.warn("Duplicate submit: {}", e.getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(new ErrorResponse("DUPLICATE_SUBMIT", e.getMessage()));
    }

    @ExceptionHandler(SessionExpiredException.class)
    public ResponseEntity<ErrorResponse> handleSessionExpired(SessionExpiredException e) {
        log.warn("Session expired: {}", e.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ErrorResponse("SESSION_EXPIRED", e.getMessage()));
    }

    @ExceptionHandler(NotSessionOwnerException.class)
    public ResponseEntity<ErrorResponse> handleNotSessionOwner(NotSessionOwnerException e) {
        log.warn("Not session owner: {}", e.getMessage());
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(new ErrorResponse("NOT_SESSION_OWNER", e.getMessage()));
    }

    @ExceptionHandler(UserNotInSessionException.class)
    public ResponseEntity<ErrorResponse> handleUserNotInSession(UserNotInSessionException e) {
        log.warn("User not in session: {}", e.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ErrorResponse("USER_NOT_IN_SESSION", e.getMessage()));
    }

    @ExceptionHandler(VerificationFailedException.class)
    public ResponseEntity<ErrorResponse> handleVerificationFailed(VerificationFailedException e) {
        log.warn("Verification failed: {}", e.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(new ErrorResponse("VERIFICATION_FAILED", e.getMessage()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(Exception e) {
        log.error("Unexpected error in speedrun module", e);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErrorResponse("INTERNAL_ERROR", "An unexpected error occurred"));
    }
}
