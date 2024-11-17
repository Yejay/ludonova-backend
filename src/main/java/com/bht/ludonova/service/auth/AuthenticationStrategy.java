package com.bht.ludonova.service.auth;

import com.bht.ludonova.dto.auth.AuthenticationResponse;

import java.util.Map;

public interface AuthenticationStrategy {
    AuthenticationResponse authenticate(Map<String, String> credentials);
    AuthenticationResponse refresh(String refreshToken);
}
