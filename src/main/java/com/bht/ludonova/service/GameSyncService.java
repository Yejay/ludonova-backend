package com.bht.ludonova.service;

import com.bht.ludonova.dto.rawg.RawgGameDTO;
import com.bht.ludonova.dto.rawg.RawgSearchResponseDTO;
import com.bht.ludonova.model.Game;
import com.bht.ludonova.model.enums.GameSource;
import com.bht.ludonova.repository.GameRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class GameSyncService {
    private final RawgService rawgService;
    private final GameRepository gameRepository;

    @EventListener(ApplicationReadyEvent.class)  // This runs when the application starts
    @Transactional
    public void initializeGameDatabase() {
        // Only fetch initial data if our database is empty or has very few games
        if (gameRepository.count() < 20) {  // Arbitrary threshold
            log.info("Initializing game database with popular games...");
            try {
                // Fetch popular games (adjust page size as needed)
                RawgSearchResponseDTO popularGames = rawgService.listGames(
                        1,          // page
                        "-rating",  // ordering
                        null,       // platforms
                        40         // pageSize
                );

                for (RawgGameDTO rawgGame : popularGames.getResults()) {
                    syncGameFromRawg(rawgGame);
                }

                log.info("Successfully initialized game database");
            } catch (Exception e) {
                log.error("Error during initial game sync", e);
            }
        }
    }

    @Transactional
    public Game syncGameFromRawg(RawgGameDTO rawgGame) {
        // Try to find existing game first
        return gameRepository.findByApiIdAndSource(
                rawgGame.getId().toString(),
                GameSource.RAWG
        ).orElseGet(() -> createGameFromRawg(rawgGame));
    }

    private Game createGameFromRawg(RawgGameDTO rawgGame) {
        Set<String> genres = rawgGame.getGenres().stream()
                .map(RawgGameDTO.Genre::getName)
                .collect(Collectors.toSet());

        return gameRepository.save(Game.builder()
                .title(rawgGame.getName())
                .apiId(rawgGame.getId().toString())
                .source(GameSource.RAWG)
                .releaseDate(rawgGame.getReleaseDate())
                .genres(genres)
                .backgroundImage(rawgGame.getBackgroundImage())
                .rating(rawgGame.getRating())
                .build());
    }
}