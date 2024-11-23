package com.bht.ludonova.dto.game;

import com.bht.ludonova.model.enums.GameSource;
import com.bht.ludonova.model.enums.Platform;
import lombok.Data;

import java.time.LocalDate;
import java.util.Set;

@Data
public class AdminGameUpdateDTO {
    private String title;
    private Platform platform;
    private LocalDate releaseDate;
    private Set<String> genres;
    private GameSource source;
    private String apiId;
}
