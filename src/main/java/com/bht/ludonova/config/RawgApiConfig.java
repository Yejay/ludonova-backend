package com.bht.ludonova.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
@EnableCaching
public class RawgApiConfig {

    @Value("${rawg.api.key}")
    private String apiKey;

    @Value("${rawg.api.base-url:https://api.rawg.io/api}")
    private String baseUrl;

    @Bean
    public WebClient rawgWebClient() {
        return WebClient.builder()
                .baseUrl(baseUrl)
                .defaultHeader("User-Agent", "LudoNova")
                .build();
    }
}