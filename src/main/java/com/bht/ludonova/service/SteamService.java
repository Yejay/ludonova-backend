package com.bht.ludonova.service;

import com.bht.ludonova.dto.steam.SteamApiResponseDTO;
import com.bht.ludonova.dto.steam.SteamGamesResponseDTO;
import com.bht.ludonova.exception.SteamAuthenticationException;
import com.bht.ludonova.model.Game;
import com.bht.ludonova.model.GameInstance;
import com.bht.ludonova.model.SteamUser;
import com.bht.ludonova.model.User;
import com.bht.ludonova.model.enums.GameSource;
import com.bht.ludonova.model.enums.GameStatus;
import com.bht.ludonova.repository.GameInstanceRepository;
import com.bht.ludonova.repository.GameRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
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
    private final GameRepository gameRepository;
    private final GameInstanceRepository gameInstanceRepository;
    private final ObjectMapper objectMapper;

    public SteamService(GameRepository gameRepository, GameInstanceRepository gameInstanceRepository, ObjectMapper objectMapper) {
        this.gameRepository = gameRepository;
        this.gameInstanceRepository = gameInstanceRepository;
        this.objectMapper = objectMapper;
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

            throw new SteamAuthenticationException("Failed to fetch Steam user details");

        } catch (Exception e) {
            log.error("Error fetching Steam user details", e);
            throw new SteamAuthenticationException("Failed to fetch Steam user details");
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

    private String encodeValue(String value) {
        try {
            return URLEncoder.encode(value, StandardCharsets.UTF_8.toString());
        } catch (UnsupportedEncodingException e) {
            log.error("Error encoding value", e);
            return value;
        }
    }

    @Transactional
    public void syncSteamLibrary(User user) {
        if (user.getSteamUser() == null) {
            log.error("User {} attempted to sync Steam library but is not connected to Steam", user.getUsername());
            throw new SteamAuthenticationException("User is not connected to Steam");
        }

        String steamId = user.getSteamUser().getSteamId();
        log.info("Starting Steam library sync for user: {} (Steam ID: {})", user.getUsername(), steamId);
        
        // Verify Steam API key is configured
        if (steamApiKey == null || steamApiKey.trim().isEmpty()) {
            log.error("Steam API key is not configured");
            throw new SteamAuthenticationException("Steam API is not properly configured");
        }

        try {
            // Make the request to Steam API
            String url = "/IPlayerService/GetOwnedGames/v1/";
            var responseSpec = steamApiClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path(url)
                            .queryParam("key", steamApiKey)
                            .queryParam("steamid", steamId)
                            .queryParam("include_appinfo", true)
                            .queryParam("include_played_free_games", true)
                            .build());

            // Get the raw response and log it
            String rawResponse = responseSpec
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            log.info("Raw Steam API response received, parsing...");

            // Parse the response using ObjectMapper
            SteamGamesResponseDTO steamGames = objectMapper.readValue(rawResponse, SteamGamesResponseDTO.class);

            if (steamGames == null || steamGames.getResponse() == null) {
                log.error("Failed to parse Steam games response for user: {}", user.getUsername());
                throw new SteamAuthenticationException("Failed to parse Steam games response");
            }

            log.info("Found {} games in Steam library for user: {}", 
                steamGames.getResponse().getGameCount(), user.getUsername());

            // Process each game
            for (SteamGamesResponseDTO.Game steamGame : steamGames.getResponse().getGames()) {
                try {
                    log.debug("Processing game: {} (App ID: {})", steamGame.getName(), steamGame.getAppId());
                    
                    // Find or create the game
                    Game game = findOrCreateGame(steamGame);
                    log.debug("Game record processed: {} (ID: {})", game.getTitle(), game.getId());
                    
                    // Create or update the game instance
                    GameInstance gameInstance = findOrCreateGameInstance(user, game, steamGame);
                    log.debug("Game instance processed: {} for user {}", game.getTitle(), user.getUsername());
                    
                } catch (Exception e) {
                    log.error("Error processing game {} for user {}: {}", 
                        steamGame.getName(), user.getUsername(), e.getMessage());
                    // Continue with next game even if one fails
                }
            }

            log.info("Successfully completed Steam library sync for user: {}", user.getUsername());

        } catch (Exception e) {
            log.error("Error during Steam sync for user: {}", user.getUsername(), e);
            throw new SteamAuthenticationException("An error occurred during Steam sync: " + e.getMessage());
        }
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    protected Game findOrCreateGame(SteamGamesResponseDTO.Game steamGame) {
        return gameRepository.findByApiIdAndSource(
                steamGame.getAppId().toString(), 
                GameSource.STEAM
        ).orElseGet(() -> {
            Game game = new Game();
            game.setTitle(steamGame.getName());
            game.setApiId(steamGame.getAppId().toString());
            game.setSource(GameSource.STEAM);
            game.setBackgroundImage("https://cdn.cloudflare.steamstatic.com/steam/apps/" + 
                steamGame.getAppId() + "/header.jpg");
            game.setSlug(steamGame.getName().toLowerCase()
                .replaceAll("[^a-z0-9\\s-]", "")
                .replaceAll("\\s+", "-"));
            return gameRepository.save(game);
        });
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    protected GameInstance findOrCreateGameInstance(User user, Game game, SteamGamesResponseDTO.Game steamGame) {
        try {
            GameInstance gameInstance = gameInstanceRepository
                    .findByUserIdAndGameId(user.getId(), game.getId())
                    .orElseGet(() -> {
                        GameInstance newInstance = new GameInstance();
                        newInstance.setUser(user);
                        newInstance.setGame(game);
                        newInstance.setStatus(GameStatus.PLAYING);
                        return gameInstanceRepository.save(newInstance);
                    });

            gameInstance.setPlaytimeMinutes(steamGame.getPlaytimeMinutes());
            if (steamGame.getLastPlayedTimestamp() != null && steamGame.getLastPlayedTimestamp() > 0) {
                gameInstance.setLastPlayedAt(new Date(steamGame.getLastPlayedTimestamp() * 1000L));
            }

            return gameInstanceRepository.save(gameInstance);
        } catch (Exception e) {
            log.error("Error creating/updating game instance for game: {} and user: {}", 
                game.getTitle(), user.getUsername(), e);
            throw e;
        }
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    protected void syncGame(User user, SteamGamesResponseDTO.Game steamGame) {
        log.debug("Processing game: {} (App ID: {}) for user: {}", 
            steamGame.getName(), steamGame.getAppId(), user.getUsername());

        Game game = findOrCreateGame(steamGame);
        log.debug("Found/Created game: {} (ID: {})", game.getTitle(), game.getId());

        GameInstance gameInstance = findOrCreateGameInstance(user, game, steamGame);
        log.debug("Successfully synced game: {} for user: {}", 
            game.getTitle(), user.getUsername());
    }

    private SteamGamesResponseDTO fetchSteamGames(String steamId) {
        String url = "/IPlayerService/GetOwnedGames/v1/";
        log.info("Fetching Steam games for Steam ID: {}", steamId);

        try {
            // Log the request URL with redacted API key
            var debugUri = steamApiClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path(url)
                            .queryParam("key", "REDACTED")
                            .queryParam("steamid", steamId)
                            .queryParam("include_appinfo", true)
                            .queryParam("include_played_free_games", true)
                            .build())
                    .toString();
            log.info("Steam API request URL: {}", debugUri);

            // Log API key length for debugging (don't log the actual key)
            log.info("Steam API key length: {}", steamApiKey != null ? steamApiKey.length() : 0);

            // Make the actual request
            var responseSpec = steamApiClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path(url)
                            .queryParam("key", steamApiKey)
                            .queryParam("steamid", steamId)
                            .queryParam("include_appinfo", true)
                            .queryParam("include_played_free_games", true)
                            .build());

            // First get the raw response to log it
            String rawResponse = responseSpec
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            log.info("Raw Steam API response: {}", rawResponse);

            // Parse the response directly using ObjectMapper
            var response = objectMapper.readValue(rawResponse, SteamGamesResponseDTO.class);

            if (response == null) {
                log.error("Received null response from Steam API for Steam ID: {}", steamId);
                throw new SteamAuthenticationException("Failed to fetch Steam games: No response from Steam API");
            }

            log.debug("Steam API response for Steam ID {}: {}", steamId, response);
            return response;

        } catch (WebClientResponseException e) {
            log.error("Steam API error: {} - Response body: {}", e.getMessage(), e.getResponseBodyAsString(), e);
            throw new SteamAuthenticationException("Failed to fetch Steam games: " + e.getMessage());
        } catch (Exception e) {
            log.error("Error fetching Steam games for Steam ID {}: {}", steamId, e.getMessage(), e);
            throw new SteamAuthenticationException("Failed to fetch Steam games: " + e.getMessage());
        }
    }

    @Transactional(readOnly = true)
    public SteamGamesResponseDTO getSteamLibrary(User user) {
        if (user.getSteamUser() == null) {
            log.error("User {} attempted to get Steam library but is not connected to Steam", user.getUsername());
            throw new SteamAuthenticationException("User is not connected to Steam");
        }

        String steamId = user.getSteamUser().getSteamId();
        log.info("Fetching Steam library for user: {} (Steam ID: {})", user.getUsername(), steamId);
        
        // Verify Steam API key is configured
        if (steamApiKey == null || steamApiKey.trim().isEmpty()) {
            log.error("Steam API key is not configured");
            throw new SteamAuthenticationException("Steam API is not properly configured");
        }

        try {
            // Make the request to Steam API
            String url = "/IPlayerService/GetOwnedGames/v1/";
            var responseSpec = steamApiClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path(url)
                            .queryParam("key", steamApiKey)
                            .queryParam("steamid", steamId)
                            .queryParam("include_appinfo", true)
                            .queryParam("include_played_free_games", true)
                            .build());

            // Get the raw response and parse it
            String rawResponse = responseSpec
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            log.info("Successfully fetched Steam library data");
            return objectMapper.readValue(rawResponse, SteamGamesResponseDTO.class);

        } catch (Exception e) {
            log.error("Error fetching Steam library for user: {}", user.getUsername(), e);
            throw new SteamAuthenticationException("An error occurred while fetching Steam library: " + e.getMessage());
        }
    }

    @Transactional
    public GameInstance importSteamGame(User user, SteamGamesResponseDTO.Game steamGame) {
        log.info("Importing Steam game {} for user {}", steamGame.getName(), user.getUsername());
        
        // Create or get the game
        Game game = findOrCreateGame(steamGame);
        
        // Create or update the game instance
        return findOrCreateGameInstance(user, game, steamGame);
    }
}