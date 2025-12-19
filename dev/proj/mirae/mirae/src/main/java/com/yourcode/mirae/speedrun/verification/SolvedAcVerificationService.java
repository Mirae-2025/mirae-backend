package com.yourcode.mirae.speedrun.verification;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
@RequiredArgsConstructor
@Slf4j
public class SolvedAcVerificationService {

    private final RestTemplate restTemplate;

    private static final String SOLVED_AC_BASE_URL = "https://solved.ac/api/v3";

    public boolean hasSolved(String handle, Long problemId) {
        String url = String.format(
                "%s/search/problem?query=solved_by:%s+id:%d",
                SOLVED_AC_BASE_URL, handle, problemId
        );

        try {
            SolvedAcSearchResponse response = restTemplate.getForObject(url, SolvedAcSearchResponse.class);
            return response != null && response.getCount() > 0;
        } catch (Exception e) {
            log.warn("solved.ac API call failed for handle={}, problemId={}: {}", handle, problemId, e.getMessage());
            return false;
        }
    }

    public SolvedAcUserResponse getUserInfo(String handle) {
        String url = String.format("%s/user/show?handle=%s", SOLVED_AC_BASE_URL, handle);

        try {
            return restTemplate.getForObject(url, SolvedAcUserResponse.class);
        } catch (Exception e) {
            log.warn("solved.ac API call failed for handle={}: {}", handle, e.getMessage());
            return null;
        }
    }

    @Getter
    @Setter
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class SolvedAcSearchResponse {
        private int count;
    }

    @Getter
    @Setter
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class SolvedAcUserResponse {
        private String handle;
        private int tier;
        private int solvedCount;
        private int rating;
    }
}
