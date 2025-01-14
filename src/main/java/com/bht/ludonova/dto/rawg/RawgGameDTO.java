package com.bht.ludonova.dto.rawg;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import java.time.LocalDate;
import java.util.List;

@Data
public class RawgGameDTO {
    private Long id;
    private String slug;
    private String name;

    @JsonProperty("released")
    private LocalDate releaseDate;

    @JsonProperty("background_image")
    private String backgroundImage;

    private Double rating;

    @JsonProperty("ratings_count")
    private Integer ratingsCount;

    @JsonProperty("metacritic")
    private Integer metacritic;

    private List<PlatformInfo> platforms;
    private List<Genre> genres;

    @JsonProperty("description_raw")
    private String description;

    @Data
    public static class PlatformInfo {
        private Platform platform;
    }

    @Data
    public static class Platform {
        private Long id;
        private String name;
        private String slug;
    }

    @Data
    public static class Genre {
        private Long id;
        private String name;
        private String slug;
    }
}