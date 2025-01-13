package com.bht.ludonova.controller;

import com.bht.ludonova.dto.auth.AuthenticationResponse;
import com.bht.ludonova.dto.auth.RefreshTokenRequest;
import com.bht.ludonova.dto.auth.LoginRequest;
import com.bht.ludonova.dto.user.CreateUserDTO;
import com.bht.ludonova.dto.user.UserDTO;
import com.bht.ludonova.exception.AuthenticationException;
import com.bht.ludonova.model.enums.Role;
import com.bht.ludonova.service.AuthenticationService;
import com.bht.ludonova.service.UserService;
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
    private final UserService userService;

    public AuthController(AuthenticationService authService, UserService userService) {
        this.authService = authService;
        this.userService = userService;
    }

    @PostMapping("/login")
    public ResponseEntity<AuthenticationResponse> login(@Valid @RequestBody LoginRequest loginRequest) {
        Map<String, String> credentials = Map.of(
                "login", loginRequest.getLogin(),
                "password", loginRequest.getPassword()
        );

        AuthenticationResponse response = authService.authenticate("basic", credentials);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/register")
    public ResponseEntity<AuthenticationResponse> register(@Valid @RequestBody CreateUserDTO createUserDTO) {
        // Set role to USER for all registrations
        createUserDTO.setRole(Role.USER);

        // Create the user
        UserDTO createdUser = userService.createUser(createUserDTO);

        // Return response without authentication tokens
        // User needs to verify email before they can log in
        return ResponseEntity.ok(new AuthenticationResponse(null, createdUser));
    }

    @PostMapping("/verify-email")
    public ResponseEntity<String> verifyEmail(
            @RequestParam String email,
            @RequestParam String code) {
        userService.verifyEmail(email, code);
        return ResponseEntity.ok("Email verified successfully");
    }

    @PostMapping("/resend-verification")
    public ResponseEntity<String> resendVerificationEmail(@RequestParam String email) {
        userService.resendVerificationEmail(email);
        return ResponseEntity.ok("Verification email sent");
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
