package com.yourcode.mirae.speedrun.verification;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.google.common.util.concurrent.RateLimiter;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
public class BaekjoonCrawlService {

    private final RateLimiter rateLimiter = RateLimiter.create(0.5); // 2 seconds per request

    private final Cache<String, SubmitResult> cache = Caffeine.newBuilder()
            .expireAfterWrite(5, TimeUnit.MINUTES)
            .maximumSize(1000)
            .build();

    public SubmitResult verify(String handle, Long problemId) {
        String cacheKey = handle + ":" + problemId;
        SubmitResult cached = cache.getIfPresent(cacheKey);
        if (cached != null) {
            return cached;
        }

        rateLimiter.acquire();

        SubmitResult result = crawl(handle, problemId);

        if (result != null) {
            cache.put(cacheKey, result);
        }

        return result;
    }

    private SubmitResult crawl(String handle, Long problemId) {
        try {
            String url = String.format(
                    "https://www.acmicpc.net/status?user_id=%s&problem_id=%d",
                    handle, problemId
            );

            Document doc = Jsoup.connect(url)
                    .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36")
                    .timeout(5000)
                    .get();

            Elements rows = doc.select("table#status-table tbody tr");

            for (Element row : rows) {
                String result = row.select(".result").text();

                if (result.contains("맞았습니다")) {
                    String submitTimeStr = row.select("a.show-date").attr("title");
                    String memory = row.select("td:nth-child(4)").text();
                    String time = row.select("td:nth-child(5)").text();

                    return SubmitResult.builder()
                            .correct(true)
                            .submitTime(parseDateTime(submitTimeStr))
                            .memory(memory)
                            .executionTime(time)
                            .build();
                }
            }

            return SubmitResult.builder().correct(false).build();

        } catch (IOException e) {
            log.error("Baekjoon crawling failed: handle={}, problemId={}", handle, problemId, e);
            return null;
        }
    }

    private LocalDateTime parseDateTime(String dateStr) {
        if (dateStr == null || dateStr.isEmpty()) {
            return null;
        }
        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            return LocalDateTime.parse(dateStr.trim(), formatter);
        } catch (Exception e) {
            log.warn("Failed to parse date: {}", dateStr);
            return null;
        }
    }
}
