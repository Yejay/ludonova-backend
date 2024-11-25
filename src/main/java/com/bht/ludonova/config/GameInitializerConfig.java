package com.bht.ludonova.config;

import com.bht.ludonova.service.GameService;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class GameInitializerConfig {

    private final GameService gameService;

    @Bean
    public ApplicationRunner initializeGames() {
        return args -> gameService.initializeGameDatabase();
    }
}