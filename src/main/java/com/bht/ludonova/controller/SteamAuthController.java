package com.bht.ludonova.controller;

import com.bht.ludonova.dto.auth.AuthenticationResponse;
import com.bht.ludonova.exception.SteamAuthenticationException;
import com.bht.ludonova.service.AuthenticationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;

@RestController
@RequestMapping("/api/auth/steam")
@Slf4j
public class SteamAuthController {
    private final AuthenticationService authService;

    @Value("${steam.return.url}")
    private String returnUrl;

    @Value("${steam.realm.url}")
    private String realmUrl;

    public SteamAuthController(AuthenticationService authService) {
        this.authService = authService;
    }

    @GetMapping("/login")
    public ResponseEntity<Map<String, String>> steamLogin() {
        try {
            String steamOpenIdUrl = buildSteamOpenIdUrl();
            return ResponseEntity.ok(Map.of("url", steamOpenIdUrl));
        } catch (Exception e) {
            log.error("Error generating Steam login URL", e);
            throw new SteamAuthenticationException("Failed to generate Steam login URL");
        }
    }

    @GetMapping("/return")
    public ResponseEntity<AuthenticationResponse> steamReturn(@RequestParam Map<String, String> params) {
        log.debug("Received Steam return params: {}", params);
        AuthenticationResponse response = authService.authenticate("steam", params);
        return ResponseEntity.ok(response);
    }

    private String buildSteamOpenIdUrl() {
        try {
            String encodedReturnUrl = URLEncoder.encode(returnUrl, StandardCharsets.UTF_8);
            String encodedRealm = URLEncoder.encode(realmUrl, StandardCharsets.UTF_8);

            return String.format(
                    "https://steamcommunity.com/openid/login" +
                            "?openid.ns=%s" +
                            "&openid.mode=checkid_setup" +
                            "&openid.return_to=%s" +
                            "&openid.realm=%s" +
                            "&openid.identity=%s" +
                            "&openid.claimed_id=%s",
                    URLEncoder.encode("http://specs.openid.net/auth/2.0", StandardCharsets.UTF_8),
                    encodedReturnUrl,
                    encodedRealm,
                    URLEncoder.encode("http://specs.openid.net/auth/2.0/identifier_select", StandardCharsets.UTF_8),
                    URLEncoder.encode("http://specs.openid.net/auth/2.0/identifier_select", StandardCharsets.UTF_8)
            );
        } catch (Exception e) {
            log.error("Error building Steam OpenID URL", e);
            throw new SteamAuthenticationException("Failed to build Steam authentication URL");
        }
    }
}