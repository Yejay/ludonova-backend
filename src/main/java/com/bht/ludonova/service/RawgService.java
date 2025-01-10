package com.bht.ludonova.service;

import com.bht.ludonova.dto.rawg.RawgSearchResponseDTO;
import com.bht.ludonova.dto.rawg.RawgGameDTO;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

@Service
@Slf4j
public class RawgService {
    private final WebClient rawgWebClient;
    private final String apiKey;
    private final ObjectMapper objectMapper;

    public RawgService(
            WebClient rawgWebClient,
            @Value("${rawg.api.key}") String apiKey,
            ObjectMapper objectMapper) {
        this.rawgWebClient = rawgWebClient;
        this.apiKey = apiKey;
        this.objectMapper = objectMapper;
    }

    public RawgSearchResponseDTO searchGames(String query, int page) {
        return rawgWebClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/games")
                        .queryParam("key", apiKey)
                        .queryParam("search", query)
                        .queryParam("page", page)
                        .queryParam("page_size", 20)
                        .build())
                .retrieve()
                .bodyToMono(RawgSearchResponseDTO.class)
                .block();
    }

    public RawgGameDTO getGameDetails(Long gameId) {
        log.debug("Fetching game details from RAWG for game ID: {}", gameId);
        try {
            String response = rawgWebClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/games/{id}")
                            .queryParam("key", apiKey)
                            .build(gameId))
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            log.debug("Raw RAWG API response for game details: {}", response);

            if (response == null) {
                log.error("Received null response from RAWG API for game details");
                return null;
            }

            try {
                objectMapper.findAndRegisterModules(); // This helps with date handling
                return objectMapper.readValue(response, RawgGameDTO.class);
            } catch (JsonProcessingException e) {
                log.error("Failed to parse RAWG API game details response: {}", e.getMessage());
                log.error("Raw response was: {}", response);
                throw new RuntimeException("Failed to parse RAWG API game details response", e);
            }
        } catch (Exception e) {
            log.error("Failed to fetch game details from RAWG: {}", e.getMessage());
            throw new RuntimeException("Failed to fetch game details from RAWG", e);
        }
    }

    public RawgSearchResponseDTO listGames(int page, String ordering, String platforms, Integer pageSize,
                                         String minRatings, String minRating) {
        log.debug("Fetching games from RAWG - page: {}, ordering: {}, platforms: {}, pageSize: {}, minRatings: {}, minRating: {}", 
            page, ordering, platforms, pageSize, minRatings, minRating);

        try {
            String response = rawgWebClient.get()
                    .uri(uriBuilder -> {
                        var builder = uriBuilder
                                .path("/games")
                                .queryParam("key", apiKey)
                                .queryParam("page", page)
                                .queryParam("page_size", pageSize != null ? pageSize : 20);

                        if (ordering != null) {
                            builder.queryParam("ordering", ordering);
                        }

                        if (platforms != null) {
                            builder.queryParam("platforms", platforms);
                        }

                        // Add quality filters
                        if (minRatings != null) {
                            builder.queryParam("ratings_count", minRatings);
                        }

                        if (minRating != null) {
                            builder.queryParam("metacritic", minRating);
                        }

                        return builder.build();
                    })
                    .retrieve()
                    .bodyToMono(String.class)
                    .block();

            log.debug("Raw RAWG API response: {}", response);

            if (response == null) {
                log.error("Received null response from RAWG API");
                return null;
            }

            try {
                objectMapper.findAndRegisterModules(); // This helps with date handling
                return objectMapper.readValue(response, RawgSearchResponseDTO.class);
            } catch (JsonProcessingException e) {
                log.error("Failed to parse RAWG API response: {}", e.getMessage());
                log.error("Raw response was: {}", response);
                throw new RuntimeException("Failed to parse RAWG API response", e);
            }
        } catch (Exception e) {
            log.error("Failed to fetch games from RAWG: {}", e.getMessage());
            throw new RuntimeException("Failed to fetch games from RAWG", e);
        }
    }

    // Keep the old method for backward compatibility
    public RawgSearchResponseDTO listGames(int page, String ordering, String platforms, Integer pageSize) {
        return listGames(page, ordering, platforms, pageSize, null, null);
    }
}