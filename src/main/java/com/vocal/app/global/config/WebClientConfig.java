package com.vocal.app.global.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.*;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class WebClientConfig {
    @Value("${ai.server.url}") private String aiServerUrl;

    @Bean
    public WebClient aiWebClient() {
        return WebClient.builder().baseUrl(aiServerUrl)
                .codecs(cfg -> cfg.defaultCodecs().maxInMemorySize(10 * 1024 * 1024)).build();
    }
}
