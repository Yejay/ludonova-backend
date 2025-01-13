package com.bht.ludonova.service;

import com.bht.ludonova.dto.auth.AuthenticationResponse;
import com.bht.ludonova.dto.auth.TokenResponse;
import com.bht.ludonova.exception.AuthenticationException;
import com.bht.ludonova.model.SteamUser;
import com.bht.ludonova.model.User;
import com.bht.ludonova.repository.UserRepository;
import com.bht.ludonova.security.JwtService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@Slf4j
@RequiredArgsConstructor
public class AuthenticationService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final UserService userService;
    private final SteamService steamService;

    public AuthenticationResponse authenticate(String provider, Map<String, String> credentials) {
        if ("basic".equals(provider)) {
            String login = credentials.get("login");
            String password = credentials.get("password");

            // Find user by username or email
            User user = userRepository.findByUsername(login)
                    .orElseGet(() -> userRepository.findByEmail(login)
                            .orElseThrow(() -> new AuthenticationException("Invalid username/email or password")));

            // Check password
            if (!passwordEncoder.matches(password, user.getPassword())) {
                throw new AuthenticationException("Invalid username/email or password");
            }

            // Check if email is verified
            if (!user.isEmailVerified()) {
                throw new AuthenticationException(
                    String.format("Please verify your email (%s) before logging in", user.getEmail()),
                    "EMAIL_NOT_VERIFIED"
                );
            }

            // Generate tokens
            String accessToken = jwtService.generateToken(user);
            String refreshToken = jwtService.generateRefreshToken(user);

            return new AuthenticationResponse(
                    new TokenResponse(accessToken, refreshToken),
                    userService.convertToDTO(user)
            );
        } else if ("steam".equals(provider)) {
            // Validate Steam response
            if (!steamService.validateSteamResponse(credentials)) {
                throw new AuthenticationException("Invalid Steam response");
            }

            // Extract Steam ID
            String steamId = extractSteamId(credentials);
            if (steamId == null) {
                throw new AuthenticationException("Could not extract Steam ID");
            }

            // Get or create user
            SteamUser steamUser = steamService.fetchUserDetails(steamId);
            User user = userService.getOrCreateSteamUser(steamUser);

            // Generate tokens
            String accessToken = jwtService.generateToken(user);
            String refreshToken = jwtService.generateRefreshToken(user);

            return new AuthenticationResponse(
                    new TokenResponse(accessToken, refreshToken),
                    userService.convertToDTO(user)
            );
        }

        throw new AuthenticationException("Unsupported authentication provider: " + provider);
    }

    public AuthenticationResponse refresh(String refreshToken) {
        if (!jwtService.isTokenValid(refreshToken)) {
            throw new AuthenticationException("Invalid refresh token");
        }

        String username = jwtService.extractUsername(refreshToken);
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new AuthenticationException("User not found"));

        String newAccessToken = jwtService.generateToken(user);
        String newRefreshToken = jwtService.generateRefreshToken(user);

        return new AuthenticationResponse(
                new TokenResponse(newAccessToken, newRefreshToken),
                userService.convertToDTO(user)
        );
    }

    private String extractSteamId(Map<String, String> params) {
        String identity = params.get("openid.claimed_id");
        if (identity != null && identity.matches("https://steamcommunity.com/openid/id/\\d+")) {
            return identity.substring(identity.lastIndexOf("/") + 1);
        }
        return null;
    }
}