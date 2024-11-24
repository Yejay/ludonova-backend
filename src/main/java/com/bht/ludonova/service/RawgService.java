package com.bht.ludonova.service;

import com.bht.ludonova.dto.rawg.RawgSearchResponseDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

@Service
@Slf4j
public class RawgService {
    private final WebClient rawgWebClient;
    private final String apiKey;

    public RawgService(
            WebClient rawgWebClient,
            @Value("${rawg.api.key}") String apiKey) {
        this.rawgWebClient = rawgWebClient;
        this.apiKey = apiKey;
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

    public RawgSearchResponseDTO listGames(int page, String ordering, String platforms, Integer pageSize) {
        return rawgWebClient.get()
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

                    return builder.build();
                })
                .retrieve()
                .bodyToMono(RawgSearchResponseDTO.class)
                .block();
    }
}