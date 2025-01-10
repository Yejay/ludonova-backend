package com.bht.ludonova.dto.gameInstance;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import com.bht.ludonova.model.enums.GameStatus;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class GameInstanceBatchCreateDTO {
    @NotEmpty(message = "Game instances list cannot be empty")
    private List<GameInstanceEntry> gameInstances;

    @Data
    public static class GameInstanceEntry {
        @NotNull
        private Long gameId;
        
        @NotNull
        private GameStatus status;
        
        private Integer progressPercentage;
        private Integer playTime;
        private String notes;
        private LocalDateTime lastPlayed;
    }
} 