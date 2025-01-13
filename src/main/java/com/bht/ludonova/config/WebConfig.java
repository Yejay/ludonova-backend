package com.bht.ludonova.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;

@Configuration
public class WebConfig {

    @Value("${FRONTEND_URL:https://ludonova-frontend.vercel.app}")
    private String frontendUrl;

    @Bean
    public WebMvcConfigurer corsConfigurer() {
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                registry.addMapping("/**")
                    .allowedOrigins(frontendUrl)
                    .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS", "HEAD")
                    .allowedHeaders("Authorization", "Cache-Control", "Content-Type", "Accept", "Origin", "X-Requested-With")
                    .allowCredentials(true)
                    .exposedHeaders("Authorization")
                    .maxAge(3600);
            }
        };
    }
}