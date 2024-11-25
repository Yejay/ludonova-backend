package com.bht.ludonova.config;

import com.bht.ludonova.model.Game;
import com.bht.ludonova.model.GameInstance;
import com.bht.ludonova.model.User;
import com.bht.ludonova.model.enums.GameSource;
import com.bht.ludonova.model.enums.GameStatus;
import com.bht.ludonova.model.enums.Platform;
import com.bht.ludonova.model.enums.Role;
import com.bht.ludonova.repository.GameInstanceRepository;
import com.bht.ludonova.repository.GameRepository;
import com.bht.ludonova.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

@Configuration
public class DataInitializer {
    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

    @Bean
    CommandLineRunner initDatabase(
            UserRepository userRepository,
            GameRepository gameRepository,
            GameInstanceRepository gameInstanceRepository,
            PasswordEncoder passwordEncoder
    ) {
        return args -> {
            // Create regular test user if doesn't exist
            User testUser;
            if (userRepository.findByUsername("test").isEmpty()) {
                testUser = User.builder()
                        .username("test")
                        .password(passwordEncoder.encode("test123"))
                        .email("test@example.com")
                        .role(Role.USER)
                        .build();
                testUser = userRepository.save(testUser);
                log.info("Test user created successfully");
            } else {
                testUser = userRepository.findByUsername("test").get();
            }

            // Create admin user if doesn't exist
            if (userRepository.findByUsername("admin").isEmpty()) {
                User adminUser = User.builder()
                        .username("admin")
                        .password(passwordEncoder.encode("admin123"))
                        .email("admin@example.com")
                        .role(Role.ADMIN)
                        .build();
                userRepository.save(adminUser);
                log.info("Admin user created successfully");
            }

            // Find or create test game
            // Game testGame;
            // List<Game> existingGames = gameRepository.findAllByApiIdAndSource("220", GameSource.STEAM);
            // if (existingGames.isEmpty()) {
            //     testGame = Game.builder()
            //             .title("Half-Life 2")
            //             .platform(Platform.PC)
            //             .apiId("220")
            //             .source(GameSource.STEAM)
            //             .releaseDate(LocalDate.of(2004, 11, 16))
            //             .genres(Set.of("FPS", "Action", "Sci-Fi"))
            //             .build();
            //     testGame = gameRepository.save(testGame);
            //     log.info("Test game created successfully");
            // } else {
            //     testGame = existingGames.get(0);
            //     log.debug("Using existing test game");
            // }

            // Create game instance for test user if it doesn't exist
            // if (!gameInstanceRepository.existsByUserIdAndGameId(testUser.getId(), testGame.getId())) {
            //     GameInstance instance = GameInstance.builder()
            //             .user(testUser)
            //             .game(testGame)
            //             .status(GameStatus.PLAYING)
            //             .progressPercentage(0)
            //             .playTime(0)
            //             .lastPlayed(LocalDateTime.now())
            //             .notes("Just started playing")
            //             .build();
            //     gameInstanceRepository.save(instance);
            //     log.info("Test game instance created successfully");
            // } else {
            //     log.debug("Test game instance already exists");
            // }
        };
    }
}