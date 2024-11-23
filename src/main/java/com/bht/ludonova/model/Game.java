package com.bht.ludonova.model;

import com.bht.ludonova.model.enums.GameSource;
import com.bht.ludonova.model.enums.Platform;
import jakarta.persistence.*;
import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "games")
public class Game {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Platform platform;

    @Column(name = "api_id")
    private String apiId;  // Steam AppId or RAWG ID

    @Column(name = "release_date")
    private LocalDateTime releaseDate;

    @ElementCollection
    @CollectionTable(name = "game_genres",
            joinColumns = @JoinColumn(name = "game_id"))
    @Column(name = "genre")
    @Builder.Default
    private Set<String> genres = new HashSet<>();

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private GameSource source;

    @OneToMany(mappedBy = "game")
    @Builder.Default
    private Set<GameInstance> instances = new HashSet<>();
}