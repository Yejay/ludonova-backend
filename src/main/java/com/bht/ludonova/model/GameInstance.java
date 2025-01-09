package com.bht.ludonova.model;

import com.bht.ludonova.model.enums.GameStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Date;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "game_instances", indexes = {
        @Index(name = "idx_game_instance_user_id", columnList = "user_id"),
        @Index(name = "idx_game_instance_user_game", columnList = "user_id,game_id", unique = true)
})
public class GameInstance {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "game_id", nullable = false)
    private Game game;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private GameStatus status;

    @Column(name = "progress_percentage")
    private Integer progressPercentage;

    @Column(name = "play_time")
    private Integer playTime;

    @Column(name = "last_played")
    private LocalDateTime lastPlayed;

    @Column(name = "added_at")
    private LocalDateTime addedAt;

    private String notes;

    @Column(name = "playtime_minutes")
    private Integer playtimeMinutes;

    @Column(name = "last_played_at")
    private Date lastPlayedAt;

    public void setPlaytimeMinutes(Integer playtimeMinutes) {
        this.playtimeMinutes = playtimeMinutes;
    }

    public void setLastPlayedAt(Date lastPlayedAt) {
        this.lastPlayedAt = lastPlayedAt;
    }
}
