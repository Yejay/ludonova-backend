package com.bht.ludonova.service.auth;

import com.bht.ludonova.dto.auth.AuthenticationResponse;
import com.bht.ludonova.dto.auth.TokenResponse;
import com.bht.ludonova.dto.user.UserDTO;
import com.bht.ludonova.exception.SteamAuthenticationException;
import com.bht.ludonova.model.SteamUser;
import com.bht.ludonova.model.User;
import com.bht.ludonova.security.JwtTokenProvider;
import com.bht.ludonova.service.SteamService;
import com.bht.ludonova.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@Slf4j
public class SteamAuthenticationStrategy implements AuthenticationStrategy {
    private final SteamService steamService;
    private final UserService userService;
    private final JwtTokenProvider tokenProvider;

    public SteamAuthenticationStrategy(
            SteamService steamService,
            UserService userService,
            JwtTokenProvider tokenProvider) {
        this.steamService = steamService;
        this.userService = userService;
        this.tokenProvider = tokenProvider;
    }

    @Override
    public AuthenticationResponse authenticate(Map<String, String> openIdParams) {
        try {
            log.debug("Starting Steam authentication with params: {}", openIdParams);

            if (!steamService.validateSteamResponse(openIdParams)) {
                log.error("Steam validation failed");
                throw new SteamAuthenticationException("Invalid Steam response");
            }

            log.debug("Steam response validated successfully");
            String steamId = extractSteamId(openIdParams);

            if (steamId == null) {
                log.error("Could not extract Steam ID from params");
                throw new SteamAuthenticationException("Could not extract Steam ID");
            }

            log.debug("Extracted Steam ID: {}", steamId);
            SteamUser steamUser = steamService.fetchUserDetails(steamId);
            log.debug("Fetched Steam user details: {}", steamUser);

            User user = userService.getOrCreateSteamUser(steamUser);
            log.debug("Got/Created user: {}", user);

            String accessToken = tokenProvider.generateAccessToken(user.getUsername());
            String refreshToken = tokenProvider.generateRefreshToken(user.getUsername());
            UserDTO userDTO = userService.convertToDTO(user);

            log.debug("Generated tokens and converted user to DTO");

            TokenResponse tokens = TokenResponse.of(
                    accessToken,
                    refreshToken,
                    tokenProvider.getAccessTokenExpirationMs()
            );

            return new AuthenticationResponse(tokens, userDTO);
        } catch (Exception e) {
            log.error("Steam authentication failed", e);
            throw new SteamAuthenticationException(e.getMessage());
        }
    }

    @Override
    public AuthenticationResponse refresh(String refreshToken) {
        return null; // Steam users use the same refresh mechanism as basic auth
    }

    private String extractSteamId(Map<String, String> params) {
        String identity = params.get("openid.claimed_id");
        if (identity != null && identity.matches("https://steamcommunity.com/openid/id/\\d+")) {
            return identity.substring(identity.lastIndexOf("/") + 1);
        }
        return null;
    }
}