package com.bht.ludonova.service.auth;

import com.bht.ludonova.dto.auth.AuthenticationResponse;
import com.bht.ludonova.dto.auth.TokenResponse;
import com.bht.ludonova.dto.user.UserDTO;
import com.bht.ludonova.exception.AuthenticationException;
import com.bht.ludonova.security.JwtTokenProvider;
import com.bht.ludonova.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@Slf4j
public class BasicAuthenticationStrategy implements AuthenticationStrategy {
    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider tokenProvider;
    private final UserService userService;

    public BasicAuthenticationStrategy(
            AuthenticationManager authenticationManager,
            JwtTokenProvider tokenProvider,
            UserService userService) {
        this.authenticationManager = authenticationManager;
        this.tokenProvider = tokenProvider;
        this.userService = userService;
    }

    @Override
    public AuthenticationResponse authenticate(Map<String, String> credentials) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            credentials.get("username"),
                            credentials.get("password")
                    )
            );
            SecurityContextHolder.getContext().setAuthentication(authentication);

            String accessToken = tokenProvider.generateAccessToken(authentication);
            String refreshToken = tokenProvider.generateRefreshToken(authentication.getName());
            UserDTO user = userService.getCurrentUserDTO();

            TokenResponse tokens = TokenResponse.of(
                    accessToken,
                    refreshToken,
                    tokenProvider.getAccessTokenExpirationMs()
            );

            return new AuthenticationResponse(tokens, user);
        } catch (org.springframework.security.core.AuthenticationException e) {
            log.error("Authentication failed", e);
            throw new AuthenticationException("Invalid username or password");
        }
    }

    @Override
    public AuthenticationResponse refresh(String refreshToken) {
        try {
            if (!tokenProvider.validateToken(refreshToken, true)) {
                throw new AuthenticationException("Invalid refresh token");
            }

            String username = tokenProvider.getUsernameFromToken(refreshToken, true);
            UserDTO user = userService.getUserDTOByUsername(username);

            String newAccessToken = tokenProvider.generateAccessToken(username);
            String newRefreshToken = tokenProvider.generateRefreshToken(username);

            TokenResponse tokens = TokenResponse.of(
                    newAccessToken,
                    newRefreshToken,
                    tokenProvider.getAccessTokenExpirationMs()
            );

            return new AuthenticationResponse(tokens, user);
        } catch (Exception e) {
            log.error("Token refresh failed", e);
            throw new AuthenticationException("Failed to refresh token");
        }
    }
}