package com.bht.ludonova.service;

import com.bht.ludonova.dto.auth.AuthenticationResponse;
import com.bht.ludonova.exception.AuthenticationException;
import com.bht.ludonova.service.auth.AuthenticationStrategy;
import com.bht.ludonova.service.auth.BasicAuthenticationStrategy;
import com.bht.ludonova.service.auth.SteamAuthenticationStrategy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@Slf4j
public class AuthenticationService {
    private final Map<String, AuthenticationStrategy> authenticationStrategies;

    public AuthenticationService(
            BasicAuthenticationStrategy basicAuthStrategy,
            SteamAuthenticationStrategy steamAuthStrategy) {
        this.authenticationStrategies = Map.of(
                "basic", basicAuthStrategy,
                "steam", steamAuthStrategy
        );
    }

//    public AuthenticationResponse authenticate(String strategy, Map<String, String> credentials) {
//        AuthenticationStrategy authStrategy = authenticationStrategies.get(strategy);
//        if (authStrategy == null) {
//            throw new AuthenticationException("Unsupported authentication strategy: " + strategy);
//        }
//
//        return authStrategy.authenticate(credentials);
//    }

    public AuthenticationResponse authenticate(String strategy, Map<String, String> credentials) {
        AuthenticationStrategy authStrategy = authenticationStrategies.get(strategy);
        if (authStrategy == null) {
            throw new AuthenticationException("Unsupported authentication strategy: " + strategy);
        }

        AuthenticationResponse response = authStrategy.authenticate(credentials);
        log.debug("Authenticated user with authorities: {}",
                SecurityContextHolder.getContext().getAuthentication().getAuthorities());

        return response;
    }

    public AuthenticationResponse refresh(String refreshToken) {
        // We use the basic strategy for refresh tokens as the mechanism is the same
        return authenticationStrategies.get("basic").refresh(refreshToken);
    }
}