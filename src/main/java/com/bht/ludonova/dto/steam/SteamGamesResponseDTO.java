package com.bht.ludonova.dto.steam;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
public class SteamGamesResponseDTO {
    @JsonProperty("response")
    private Response response;

    @Data
    @NoArgsConstructor
    public static class Response {
        @JsonProperty("game_count")
        private Integer gameCount;

        @JsonProperty("games")
        private List<Game> games;
    }

    @Data
    @NoArgsConstructor
    public static class Game {
        @JsonProperty("appid")
        private Long appId;

        @JsonProperty("name")
        private String name;

        @JsonProperty("img_icon_url")
        private String iconUrl;

        @JsonProperty("playtime_forever")
        private Integer playtimeMinutes;

        @JsonProperty("rtime_last_played")
        private Long lastPlayedTimestamp;
    }
} 