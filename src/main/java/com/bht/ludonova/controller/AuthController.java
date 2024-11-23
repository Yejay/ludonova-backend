package com.bht.ludonova.controller;

import com.bht.ludonova.dto.auth.AuthenticationResponse;
import com.bht.ludonova.dto.auth.RefreshTokenRequest;
import com.bht.ludonova.dto.auth.LoginRequest;
import com.bht.ludonova.exception.AuthenticationException;
import com.bht.ludonova.service.AuthenticationService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@Slf4j
public class AuthController {
    private final AuthenticationService authService;

    public AuthController(AuthenticationService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public ResponseEntity<AuthenticationResponse> login(@Valid @RequestBody LoginRequest loginRequest) {
        Map<String, String> credentials = Map.of(
                "username", loginRequest.getUsername(),
                "password", loginRequest.getPassword()
        );

        AuthenticationResponse response = authService.authenticate("basic", credentials);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/refresh")
    public ResponseEntity<AuthenticationResponse> refresh(@Valid @RequestBody RefreshTokenRequest request) {
        log.debug("Received refresh token request with token: {}", request.getRefreshToken());
        try {
            AuthenticationResponse response = authService.refresh(request.getRefreshToken());
            return ResponseEntity.ok(response);
        } catch (AuthenticationException e) {
            log.error("Failed to refresh token", e);
            throw new AuthenticationException("Invalid refresh token");
        }
    }
}
