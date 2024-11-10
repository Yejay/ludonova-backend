package com.bht.ludonova.config;

import com.bht.ludonova.model.User;
import com.bht.ludonova.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class DataInitializer {

    @Bean
    CommandLineRunner initDatabase(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        return args -> {
            // Create test user if it doesn't exist
            if (userRepository.findByUsername("test").isEmpty()) {
                User testUser = User.builder()
                        .username("test")
                        .password(passwordEncoder.encode("test123"))
                        .email("test@example.com")
                        .build();
                userRepository.save(testUser);
                System.out.println("Test user created successfully");
            }
        };
    }
}