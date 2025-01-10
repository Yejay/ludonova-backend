package com.bht.ludonova.model;

import com.bht.ludonova.model.enums.GameSource;
import com.bht.ludonova.model.enums.Platform;
import jakarta.persistence.*;
import lombok.Data;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDate;
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

    @Column(nullable = false, unique = true)
    private String slug;

    @Column(name = "api_id", unique = true)
    private String apiId;

    @Column(name = "background_image")
    private String backgroundImage;

    @ElementCollection
    @CollectionTable(name = "game_genres", 
                    joinColumns = @JoinColumn(name = "game_id"))
    @Column(name = "genre")
    @Builder.Default
    private Set<String> genres = new HashSet<>();

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private GameSource source;

    @Column(name = "rawg_last_updated")
    private LocalDateTime rawgLastUpdated;

    @Column(name = "release_date")
    private LocalDate releaseDate;

    private Double rating;

    @Column(columnDefinition = "TEXT")
    private String description;
}
