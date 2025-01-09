package com.bht.ludonova.dto.gameInstance;

import lombok.Data;
import lombok.Builder;

@Data
@Builder
public class GameInstanceStatsDTO {
    private int totalGames;
    private int playing;
    private int completed;
    private int planToPlay;
    private int dropped;
    private int totalPlayTime;
    private double averageCompletion;
} 