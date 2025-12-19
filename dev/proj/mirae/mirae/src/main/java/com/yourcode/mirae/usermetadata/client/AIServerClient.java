package com.yourcode.mirae.usermetadata.client;

import com.yourcode.mirae.usermetadata.config.AIServerConfig;
import com.yourcode.mirae.usermetadata.dto.*;
import com.yourcode.mirae.usermetadata.exception.AIServerException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class AIServerClient {

    private final RestTemplate aiServerRestTemplate;
    private final AIServerConfig aiServerConfig;

    // ==================== Health Check ====================

    public boolean isHealthy() {
        try {
            String url = buildUrl("/health");
            ResponseEntity<Map> response = aiServerRestTemplate.getForEntity(url, Map.class);
            return response.getStatusCode() == HttpStatus.OK;
        } catch (Exception e) {
            log.warn("AI server health check failed: {}", e.getMessage());
            return false;
        }
    }

    // ==================== Recommendation APIs ====================

    public RecommendResponse getRecommendations(String userId, Integer k, Boolean excludeSolved,
                                                 String difficultyMin, String difficultyMax) {
        return getRecommendationsByStrategy("tfidf", userId, k, excludeSolved, difficultyMin, difficultyMax);
    }

    public RecommendResponse getPopularityRecommendations(String userId, Integer k, Boolean excludeSolved,
                                                           String difficultyMin, String difficultyMax) {
        return getRecommendationsByStrategy("popularity", userId, k, excludeSolved, difficultyMin, difficultyMax);
    }

    public RecommendResponse getRandomRecommendations(String userId, Integer k, Boolean excludeSolved,
                                                       String difficultyMin, String difficultyMax) {
        return getRecommendationsByStrategy("random", userId, k, excludeSolved, difficultyMin, difficultyMax);
    }

    public RecommendResponse getHybridRecommendations(String userId, Integer k, Boolean excludeSolved,
                                                       String difficultyMin, String difficultyMax) {
        return getRecommendationsByStrategy("hybrid", userId, k, excludeSolved, difficultyMin, difficultyMax);
    }

    public RecommendResponse getWeaknessRecommendations(String userId, Integer k, Boolean excludeSolved,
                                                         String difficultyMin, String difficultyMax) {
        return getRecommendationsByStrategy("weakness", userId, k, excludeSolved, difficultyMin, difficultyMax);
    }

    private RecommendResponse getRecommendationsByStrategy(String strategy, String userId, Integer k,
                                                            Boolean excludeSolved, String difficultyMin,
                                                            String difficultyMax) {
        String endpoint = strategy.equals("tfidf") ? "/recommend" : "/recommend/" + strategy;

        UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(buildUrl(endpoint))
                .queryParam("user_id", userId);

        if (k != null) builder.queryParam("k", k);
        if (excludeSolved != null) builder.queryParam("exclude_solved", excludeSolved);
        if (difficultyMin != null) builder.queryParam("difficulty_min", difficultyMin);
        if (difficultyMax != null) builder.queryParam("difficulty_max", difficultyMax);

        return executeGet(builder.toUriString(), RecommendResponse.class);
    }

    public BatchRecommendResponse getBatchRecommendations(BatchRecommendRequest request, String strategy) {
        String url = UriComponentsBuilder.fromHttpUrl(buildUrl("/recommend/batch"))
                .queryParam("strategy", strategy != null ? strategy : "tfidf")
                .toUriString();

        return executePost(url, request, BatchRecommendResponse.class);
    }

    // ==================== Weakness Analysis ====================

    public WeaknessAnalysisResponse analyzeWeakness(String userId) {
        String url = UriComponentsBuilder.fromHttpUrl(buildUrl("/analysis/weakness"))
                .queryParam("user_id", userId)
                .toUriString();

        return executeGet(url, WeaknessAnalysisResponse.class);
    }

    // ==================== Speedrun APIs ====================

    public SpeedrunSessionResponse createSpeedrunSession(SpeedrunCreateRequest request) {
        return executePost(buildUrl("/speedrun/create"), request, SpeedrunSessionResponse.class);
    }

    public SpeedrunSessionResponse getSpeedrunSession(String sessionId) {
        return executeGet(buildUrl("/speedrun/session/" + sessionId), SpeedrunSessionResponse.class);
    }

    public SpeedrunSubmitResponse submitSpeedrunProblem(SpeedrunSubmitRequest request) {
        return executePost(buildUrl("/speedrun/submit"), request, SpeedrunSubmitResponse.class);
    }

    public SpeedrunResultResponse getSpeedrunResult(String sessionId) {
        return executeGet(buildUrl("/speedrun/result/" + sessionId), SpeedrunResultResponse.class);
    }

    public SpeedrunLeaderboardResponse getSpeedrunLeaderboard(String mode, Integer limit) {
        UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(buildUrl("/speedrun/leaderboard"));
        if (mode != null) builder.queryParam("mode", mode);
        if (limit != null) builder.queryParam("limit", limit);

        return executeGet(builder.toUriString(), SpeedrunLeaderboardResponse.class);
    }

    public SpeedrunSessionResponse getActiveSpeedrun(String userId) {
        try {
            return executeGet(buildUrl("/speedrun/active/" + userId), SpeedrunSessionResponse.class);
        } catch (AIServerException e) {
            if (e.getStatusCode() == 404) {
                return null;
            }
            throw e;
        }
    }

    // ==================== Growth Analysis APIs ====================

    public GrowthReportResponse getGrowthReport(String userId, Integer days) {
        UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(buildUrl("/growth/report"))
                .queryParam("user_id", userId);
        if (days != null) builder.queryParam("days", days);

        return executeGet(builder.toUriString(), GrowthReportResponse.class);
    }

    public AccuracyTrendResponse getAccuracyTrend(String userId, Integer days) {
        UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(buildUrl("/growth/accuracy-trend"))
                .queryParam("user_id", userId);
        if (days != null) builder.queryParam("days", days);

        return executeGet(builder.toUriString(), AccuracyTrendResponse.class);
    }

    public TagAnalysisResponse getTagAnalysis(String userId, Integer days) {
        UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(buildUrl("/growth/tags"))
                .queryParam("user_id", userId);
        if (days != null) builder.queryParam("days", days);

        return executeGet(builder.toUriString(), TagAnalysisResponse.class);
    }

    // ==================== Helper Methods ====================

    private String buildUrl(String path) {
        return aiServerConfig.getAiServerBaseUrl() + path;
    }

    private <T> T executeGet(String url, Class<T> responseType) {
        try {
            log.debug("GET {}", url);
            ResponseEntity<T> response = aiServerRestTemplate.getForEntity(url, responseType);
            return response.getBody();
        } catch (HttpClientErrorException e) {
            log.error("AI server client error: {} - {}", e.getStatusCode(), e.getResponseBodyAsString());
            throw new AIServerException(extractErrorMessage(e), e.getStatusCode().value());
        } catch (HttpServerErrorException e) {
            log.error("AI server error: {} - {}", e.getStatusCode(), e.getResponseBodyAsString());
            throw new AIServerException("AI server error: " + e.getMessage(), e.getStatusCode().value());
        } catch (ResourceAccessException e) {
            log.error("AI server unavailable: {}", e.getMessage());
            throw new AIServerException("AI server is unavailable", e);
        }
    }

    private <T> T executePost(String url, Object request, Class<T> responseType) {
        try {
            log.debug("POST {} with body: {}", url, request);
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<Object> entity = new HttpEntity<>(request, headers);

            ResponseEntity<T> response = aiServerRestTemplate.postForEntity(url, entity, responseType);
            return response.getBody();
        } catch (HttpClientErrorException e) {
            log.error("AI server client error: {} - {}", e.getStatusCode(), e.getResponseBodyAsString());
            throw new AIServerException(extractErrorMessage(e), e.getStatusCode().value());
        } catch (HttpServerErrorException e) {
            log.error("AI server error: {} - {}", e.getStatusCode(), e.getResponseBodyAsString());
            throw new AIServerException("AI server error: " + e.getMessage(), e.getStatusCode().value());
        } catch (ResourceAccessException e) {
            log.error("AI server unavailable: {}", e.getMessage());
            throw new AIServerException("AI server is unavailable", e);
        }
    }

    private String extractErrorMessage(HttpClientErrorException e) {
        try {
            String body = e.getResponseBodyAsString();
            if (body.contains("detail")) {
                int start = body.indexOf("\"detail\":\"") + 10;
                int end = body.indexOf("\"", start);
                if (start > 10 && end > start) {
                    return body.substring(start, end);
                }
            }
            return e.getMessage();
        } catch (Exception ex) {
            return e.getMessage();
        }
    }
}
