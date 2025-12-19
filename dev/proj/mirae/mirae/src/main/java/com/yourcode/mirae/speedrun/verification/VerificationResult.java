package com.yourcode.mirae.speedrun.verification;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class VerificationResult {
    private VerificationStatus status;
    private String source;

    public static VerificationResult verified(String source) {
        return new VerificationResult(VerificationStatus.VERIFIED, source);
    }

    public static VerificationResult rejected(String reason) {
        return new VerificationResult(VerificationStatus.REJECTED, reason);
    }

    public static VerificationResult pending(String reason) {
        return new VerificationResult(VerificationStatus.PENDING, reason);
    }
}
