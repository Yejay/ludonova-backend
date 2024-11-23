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
            if (userRepository.findByUsername("test").isEmpty()) {
                User testUser = User.builder()
                        .username("test")
                        .password(passwordEncoder.encode("test123"))
                        .email("test@example.com")
                        .role(Role.USER)  // Set regular user role
                        .build();
                userRepository.save(testUser);
                log.info("Test user created successfully");
            }

            // Create admin user if doesn't exist
            if (userRepository.findByUsername("admin").isEmpty()) {
                User adminUser = User.builder()
                        .username("admin")
                        .password(passwordEncoder.encode("admin123"))
                        .email("admin@example.com")
                        .role(Role.ADMIN)  // Set admin role
                        .build();
                userRepository.save(adminUser);
                log.info("Admin user created successfully");
            }

            // Create test game
            Game testGame = Game.builder()
                    .title("Half-Life 2")
                    .platform(Platform.PC)
                    .apiId("220")
                    .source(GameSource.STEAM)
                    .releaseDate(LocalDate.of(2004, 11, 16))
                    .genres(Set.of("FPS", "Action", "Sci-Fi"))
                    .build();

            testGame = gameRepository.save(testGame);
            log.info("Test game created successfully");

            // Create game instance for test user
            User user = userRepository.findByUsername("test").get();
            if (!gameInstanceRepository.existsByUserIdAndGameId(user.getId(), testGame.getId())) {
                GameInstance instance = GameInstance.builder()
                        .user(user)
                        .game(testGame)
                        .status(GameStatus.PLAYING)
                        .progressPercentage(0)
                        .playTime(0)
                        .lastPlayed(LocalDateTime.now())
                        .notes("Just started playing")
                        .build();
                gameInstanceRepository.save(instance);
                log.info("Test game instance created successfully");
            }
        };
    }
}