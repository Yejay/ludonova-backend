package com.bht.ludonova.dto.game;

import lombok.Data;

@Data
public class GameInstanceUpdateDTO {
    private String status;          // PLAYING, COMPLETED, BACKLOG, etc.
    private Integer progressPercentage;
    private Integer playTime;
    private String notes;
}
