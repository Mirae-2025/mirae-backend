package com.yourcode.mirae.speedrun.verification;

import com.yourcode.mirae.auth.entity.User;
import com.yourcode.mirae.auth.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class SubmitVerificationService {

    private final SolvedAcVerificationService solvedAcService;
    private final BaekjoonCrawlService crawlService;
    private final UserRepository userRepository;

    public VerificationResult verify(Long userId, Long problemId, LocalDateTime sessionStart) {
        User user = userRepository.findById(userId).orElse(null);
        if (user == null || user.getBaekjunId() == null) {
            return VerificationResult.rejected("User or Baekjoon handle not found");
        }

        String handle = user.getBaekjunId();

        // 1st verification: solved.ac API
        boolean solvedAcResult = solvedAcService.hasSolved(handle, problemId);
        if (solvedAcResult) {
            return VerificationResult.verified("solved.ac");
        }

        // 2nd verification: Baekjoon crawling (if not found in solved.ac)
        SubmitResult crawlResult = crawlService.verify(handle, problemId);

        if (crawlResult == null) {
            return VerificationResult.pending("Crawling failed, retry needed");
        }

        if (crawlResult.isCorrect()) {
            if (crawlResult.getSubmitTime() != null && crawlResult.getSubmitTime().isAfter(sessionStart)) {
                return VerificationResult.verified("crawling");
            } else {
                return VerificationResult.rejected("Submitted before session start");
            }
        }

        return VerificationResult.rejected("No correct submission found");
    }
}
