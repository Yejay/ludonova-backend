package com.bht.ludonova.dto.game;

import com.bht.ludonova.model.enums.Platform;
import lombok.Data;

import java.util.Set;

@Data
public class GameUpdateDTO {
    private String title;
    private Platform platform;
    private Set<String> genres;
    // TODO evaluate, if these should be able to be updated by the user.
//    private LocalDate releaseDate;
//    private GameSource source;
}
