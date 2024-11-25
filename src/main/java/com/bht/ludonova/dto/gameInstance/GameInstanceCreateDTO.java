package com.bht.ludonova.dto.gameInstance;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import com.bht.ludonova.model.enums.GameStatus;

@Data
public class GameInstanceCreateDTO {
    @NotNull
    private Long gameId;

    @NotNull
    private GameStatus status;

    private Integer progressPercentage;
    private Integer playTime;
    private String notes;
}