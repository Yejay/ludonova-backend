package com.bht.ludonova.service;

import com.bht.ludonova.dto.steam.SteamApiResponseDTO;
import com.bht.ludonova.exception.SteamAuthenticationException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import com.bht.ludonova.model.SteamUser;
import lombok.extern.slf4j.Slf4j;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
public class SteamService {
    @Value("${steam.api.key}")
    private String steamApiKey;

    private final WebClient steamApiClient;
    private final WebClient openIdClient;

    public SteamService() {
        this.steamApiClient = WebClient.builder()
                .baseUrl("https://api.steampowered.com")
                .codecs(configurer -> configurer.defaultCodecs().maxInMemorySize(16 * 1024 * 1024))
                .build();

        this.openIdClient = WebClient.builder()
                .codecs(configurer -> configurer.defaultCodecs().maxInMemorySize(16 * 1024 * 1024))
                .build();
    }

    public boolean validateSteamResponse(Map<String, String> params) {
        try {
            log.info("Validating Steam response with params: {}", params);
            String url = "https://steamcommunity.com/openid/login";

            // Create a new map with check_authentication mode
            Map<String, String> validationParams = new HashMap<>(params);
            validationParams.put("openid.mode", "check_authentication");

            // Convert map to form data
            String formData = validationParams.entrySet().stream()
                    .map(entry -> entry.getKey() + "=" + encodeValue(entry.getValue()))
                    .collect(Collectors.joining("&"));

            String responseBody = openIdClient.post()
                    .uri(url)
                    .header("Content-Type", "application/x-www-form-urlencoded")
                    .bodyValue(formData)
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            log.info("Steam validation response: {}", responseBody);
            return responseBody != null && responseBody.contains("is_valid:true");
        } catch (Exception e) {
            log.error("Error validating Steam response", e);
            return false;
        }
    }

    public SteamUser fetchUserDetails(String steamId) {
        String url = "/ISteamUser/GetPlayerSummaries/v2/";

        try {
            SteamApiResponseDTO dto = steamApiClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path(url)
                            .queryParam("key", steamApiKey)
                            .queryParam("steamids", steamId)
                            .build())
                    .retrieve()
                    .bodyToMono(SteamApiResponseDTO.class)
                    .block();

            if (dto != null && dto.getResponse() != null && !dto.getResponse().getPlayers().isEmpty()) {
                SteamApiResponseDTO.Player player = dto.getResponse().getPlayers().get(0);
                return mapToSteamUser(player);
            }

            throw new SteamApiException("Failed to fetch Steam user details");
        } catch (Exception e) {
            log.error("Error fetching Steam user details", e);
            throw new SteamApiException("Failed to fetch Steam user details", e);
        }
    }

    private String encodeValue(String value) {
        try {
            return URLEncoder.encode(value, StandardCharsets.UTF_8.toString());
        } catch (UnsupportedEncodingException e) {
            return value;
        }
    }

    private SteamUser mapToSteamUser(SteamApiResponseDTO.Player player) {
        SteamUser steamUser = new SteamUser();
        steamUser.setSteamId(player.getSteamId());
        steamUser.setPersonaName(player.getPersonaName());
        steamUser.setProfileUrl(player.getProfileUrl());
        steamUser.setAvatarUrl(player.getAvatarUrl());
        return steamUser;
    }

    public static class SteamApiException extends RuntimeException {
        public SteamApiException(String message) {
            super(message);
        }

        public SteamApiException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}