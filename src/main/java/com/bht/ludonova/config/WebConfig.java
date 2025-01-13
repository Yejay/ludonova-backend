package com.bht.ludonova.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Configuration
public class WebConfig {
    private static final Logger logger = LoggerFactory.getLogger(WebConfig.class);

    @Value("${FRONTEND_URL:https://ludonova-frontend.vercel.app}")
    private String frontendUrl;

    @Bean
    public WebMvcConfigurer corsConfigurer() {
        logger.info("Configuring CORS with frontendUrl: {}", frontendUrl);
        return new WebMvcConfigurer() {
            @Override
            public void addCorsMappings(CorsRegistry registry) {
                logger.info("Setting up CORS mappings");
                registry.addMapping("/**")
                    .allowedOrigins(frontendUrl)
                    .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS", "HEAD")
                    .allowedHeaders("Authorization", "Cache-Control", "Content-Type", "Accept", "Origin", "X-Requested-With")
                    .allowCredentials(true)
                    .exposedHeaders("Authorization")
                    .maxAge(3600);
                logger.info("CORS configuration completed");
            }
        };
    }
}