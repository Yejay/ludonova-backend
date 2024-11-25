package com.bht.ludonova.dto.gameInstance;

import java.time.LocalDateTime;
import java.util.Set;
import lombok.Data;
import com.bht.ludonova.model.enums.GameStatus;

@Data
public class GameInstanceResponseDTO {
    private Long id;
    private Long gameId;
    private String gameTitle;
    private String backgroundImage;
    private GameStatus status;
    private Integer progressPercentage;
    private Integer playTime;
    private String notes;
    private LocalDateTime lastPlayed;
    private LocalDateTime addedAt;
    private Set<String> genres;
}