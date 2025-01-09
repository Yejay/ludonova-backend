package com.bht.ludonova.service;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.cache.CacheManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.cache.annotation.Cacheable;

import com.bht.ludonova.dto.rawg.RawgGameDTO;
import com.bht.ludonova.dto.rawg.RawgSearchResponseDTO;
import com.bht.ludonova.model.Game;
import com.bht.ludonova.model.enums.GameSource;
import com.bht.ludonova.repository.GameRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
@Slf4j
public class GameService {
    private final GameRepository gameRepository;
    private final RawgService rawgService;
    private final CacheManager cacheManager;

    @Cacheable(value = "games", key = "#id")
    public Optional<Game> findById(Long id) {
        return gameRepository.findById(id);
    }

    @Transactional
    public void initializeGameDatabase() {
        if (gameRepository.count() < 20) {
            log.info("Initializing game database with popular games...");
            int page = 1;
            int totalGamesFetched = 0;
            int pageSize = 40; // Fetch 40 games per page

            while (totalGamesFetched < 100) { // Fetch at least 100 games
                try {
                    RawgSearchResponseDTO response = rawgService.listGames(
                            page,
                            "-metacritic",
                            null,
                            pageSize);

                    for (RawgGameDTO rawgGame : response.getResults()) {
                        syncGameFromRawg(rawgGame);
                        totalGamesFetched++;
                    }

                    if (response.getNext() == null) break; // No more pages
                    page++;
                } catch (Exception e) {
                    log.error("Error fetching from RAWG, retrying...", e);
                    break; // Fail gracefully
                }
            }

            log.info("Initialized game database with {} games.", totalGamesFetched);
        } else {
            log.info("Game database already initialized.");
        }
    }

    @Transactional
    public Page<Game> searchGames(String searchQuery, Pageable pageable) {
        if (searchQuery == null || searchQuery.trim().isEmpty()) {
            return gameRepository.findAllByOrderByRatingDesc(pageable);
        }

        // First check local cache/database
        Page<Game> localResults = gameRepository
                .findByTitleContainingIgnoreCase(searchQuery.trim(), pageable);

        // If insufficient results, fetch from RAWG
        if (localResults.getContent().size() < pageable.getPageSize()) {
            try {
                // Fetch first 2 pages from RAWG for better performance
                for (int i = 1; i <= 2; i++) {
                    fetchAndSyncGamesFromRawg(searchQuery, i);
                }
                return gameRepository.findByTitleContainingIgnoreCase(
                        searchQuery.trim(), pageable);
            } catch (Exception e) {
                log.error("Error fetching from RAWG", e);
                return localResults;
            }
        }

        return localResults;
    }

    @Transactional
    protected void fetchAndSyncGamesFromRawg(String query, int page) {
        RawgSearchResponseDTO searchResponse = rawgService.searchGames(query, page);
        if (searchResponse != null && searchResponse.getResults() != null) {
            searchResponse.getResults().forEach(this::syncGameFromRawg);
        }
    }

    @Transactional
    protected Game syncGameFromRawg(RawgGameDTO rawgGame) {
        return gameRepository.findByApiIdAndSource(
                        rawgGame.getId().toString(), GameSource.RAWG)
                .map(existing -> updateGameFromRawg(existing, rawgGame))
                .orElseGet(() -> createGameFromRawg(rawgGame));
    }

    private Game updateGameFromRawg(Game existing, RawgGameDTO rawgGame) {
        existing.setTitle(rawgGame.getName());
        existing.setSlug(generateSlug(rawgGame.getName()));
        existing.setRating(rawgGame.getRating());
        existing.setBackgroundImage(rawgGame.getBackgroundImage());
        existing.setGenres(rawgGame.getGenres().stream()
                .map(RawgGameDTO.Genre::getName)
                .collect(Collectors.toSet()));
        existing.setRawgLastUpdated(LocalDateTime.now());
        return gameRepository.save(existing);
    }

    private Game createGameFromRawg(RawgGameDTO rawgGame) {
        String slug = generateSlug(rawgGame.getName());
        Game game = Game.builder()
                .title(rawgGame.getName())
                .apiId(rawgGame.getId().toString())
                .source(GameSource.RAWG)
                .releaseDate(rawgGame.getReleaseDate())
                .backgroundImage(rawgGame.getBackgroundImage())
                .rating(rawgGame.getRating())
                .slug(slug)
                .genres(rawgGame.getGenres().stream()
                        .map(RawgGameDTO.Genre::getName)
                        .collect(Collectors.toSet()))
                .rawgLastUpdated(LocalDateTime.now())
                .build();
        return gameRepository.save(game);
    }

    private String generateSlug(String title) {
        if (title == null) return "";
        return title.toLowerCase()
                .replaceAll("[^a-z0-9\\s-]", "")
                .replaceAll("\\s+", "-")
                .replaceAll("-+", "-")
                .trim();
    }
}