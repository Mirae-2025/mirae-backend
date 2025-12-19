package com.yourcode.mirae.speedrun.websocket;

import com.yourcode.mirae.speedrun.dto.LeaderboardEntry;
import com.yourcode.mirae.speedrun.service.RankingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class LeaderboardBroadcastService {

    private final SimpMessagingTemplate messagingTemplate;
    private final RankingService rankingService;

    public void broadcastLeaderboard(String sessionId) {
        List<LeaderboardEntry> leaderboard = rankingService.getSessionLeaderboard(sessionId, 10);

        messagingTemplate.convertAndSend(
                "/topic/session/" + sessionId + "/leaderboard",
                leaderboard
        );

        log.debug("Broadcasted leaderboard for session: {}", sessionId);
    }

    public void broadcastSessionState(String sessionId, String state) {
        messagingTemplate.convertAndSend(
                "/topic/session/" + sessionId + "/state",
                Map.of("state", state, "timestamp", System.currentTimeMillis())
        );

        log.debug("Broadcasted session state {} for session: {}", state, sessionId);
    }

    public void broadcastUserSubmit(String sessionId, Long userId, int score, int rank) {
        messagingTemplate.convertAndSend(
                "/topic/session/" + sessionId + "/submit",
                Map.of(
                        "userId", userId,
                        "score", score,
                        "rank", rank,
                        "timestamp", System.currentTimeMillis()
                )
        );
    }
}
