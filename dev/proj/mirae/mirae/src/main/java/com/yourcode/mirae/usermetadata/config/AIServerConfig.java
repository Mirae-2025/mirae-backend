package com.yourcode.mirae.usermetadata.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

@Configuration
public class AIServerConfig {

    @Value("${ai.server.base-url:http://localhost:8000}")
    private String aiServerBaseUrl;

    @Value("${ai.server.connect-timeout:5000}")
    private int connectTimeout;

    @Value("${ai.server.read-timeout:30000}")
    private int readTimeout;

    @Bean
    public RestTemplate aiServerRestTemplate() {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(connectTimeout);
        factory.setReadTimeout(readTimeout);
        return new RestTemplate(factory);
    }

    public String getAiServerBaseUrl() {
        return aiServerBaseUrl;
    }
}
