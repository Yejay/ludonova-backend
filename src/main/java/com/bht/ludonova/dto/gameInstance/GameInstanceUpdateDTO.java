package com.bht.ludonova.dto.gameInstance;

import lombok.Data;
import com.bht.ludonova.model.enums.GameStatus;

@Data
public class GameInstanceUpdateDTO {
    private GameStatus status;
    private Integer progressPercentage;
    private Integer playTime;
    private String notes;
}