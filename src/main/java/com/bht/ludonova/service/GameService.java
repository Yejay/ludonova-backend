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

    public long getGameCount() {
        return gameRepository.count();
    }

    @Transactional
    public void initializeGameDatabase() {
        long currentCount = gameRepository.count();
        if (currentCount < 500) {
            log.info("Initializing game database with popular games... Current count: {}", currentCount);
            int totalGamesFetched = 0;
            int targetGames = 500;
            final int MAX_PAGE_SIZE = 40; // RAWG API limit
            final int PAGES_PER_TIER = 5; // Fetch more pages per tier

            // Define metacritic score tiers from highest to lowest
            int[][] metacriticTiers = {
                {90, 100}, // Masterpieces
                {85, 89},  // Excellent games
                {80, 84},  // Very good games
                {75, 79},  // Good games
                {70, 74}   // Above average games
            };

            try {
                for (int[] tier : metacriticTiers) {
                    if (totalGamesFetched >= targetGames) {
                        break;
                    }

                    int minScore = tier[0];
                    int maxScore = tier[1];
                    log.info("Fetching games with metacritic score between {} and {}...", minScore, maxScore);

                    for (int page = 1; page <= PAGES_PER_TIER; page++) {
                        if (totalGamesFetched >= targetGames) {
                            break;
                        }

                        log.info("Fetching page {} for metacritic {}-{}...", page, minScore, maxScore);
                        RawgSearchResponseDTO response = rawgService.listGames(
                            page,
                            "-metacritic",  // Always sort by metacritic score
                            null,
                            MAX_PAGE_SIZE,
                            null,           // No minimum ratings requirement
                            String.format("%d,%d", minScore, maxScore) // metacritic range
                        );

                        if (response == null || response.getResults() == null || response.getResults().isEmpty()) {
                            log.warn("No more results from RAWG API for metacritic {}-{} on page {}", 
                                minScore, maxScore, page);
                            break;
                        }

                        log.debug("Received {} games from RAWG API", response.getResults().size());
                        int pageGamesFetched = 0;
                        for (RawgGameDTO rawgGame : response.getResults()) {
                            if (totalGamesFetched >= targetGames) {
                                break;
                            }

                            try {
                                if (rawgGame.getName() == null || rawgGame.getName().trim().isEmpty()) {
                                    log.warn("Skipping game with null or empty name");
                                    continue;
                                }

                                // Check if we already have this game
                                if (gameRepository.existsByApiIdAndSource(
                                    rawgGame.getId().toString(), GameSource.RAWG)) {
                                    log.debug("Skipping existing game: {}", rawgGame.getName());
                                    continue;
                                }

                                Game game = syncGameFromRawg(rawgGame);
                                if (game != null) {
                                    totalGamesFetched++;
                                    pageGamesFetched++;
                                    log.debug("Synced game: {} (Total: {}, Metacritic: {})", 
                                        game.getTitle(), totalGamesFetched, rawgGame.getMetacritic());
                                }
                            } catch (Exception e) {
                                log.warn("Failed to sync game: {} - Error: {}", rawgGame.getName(), e.getMessage());
                            }
                        }

                        log.info("Fetched {} games from metacritic {}-{} page {}, total games fetched: {}", 
                            pageGamesFetched, minScore, maxScore, page, totalGamesFetched);
                        
                        if (response.getNext() == null || pageGamesFetched == 0) {
                            log.info("No more pages available for metacritic {}-{}", minScore, maxScore);
                            break;
                        }
                        
                        // Add a small delay to avoid rate limiting
                        try {
                            Thread.sleep(1000);
                        } catch (InterruptedException e) {
                            Thread.currentThread().interrupt();
                            break;
                        }
                    }
                }

                log.info("Initialized game database with {} games. Final database count: {}", 
                    totalGamesFetched, gameRepository.count());
            } catch (Exception e) {
                log.error("Error during game database initialization: {} ({})", e.getMessage(), e.getClass().getName(), e);
            }
        } else {
            log.info("Game database already initialized with {} games.", currentCount);
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
            // Only sync games that are exact or very close matches
            searchResponse.getResults().stream()
                .filter(rawgGame -> {
                    String normalizedQuery = query.toLowerCase().trim();
                    String normalizedTitle = rawgGame.getName().toLowerCase().trim();
                    
                    // Exact match
                    if (normalizedTitle.equals(normalizedQuery)) {
                        return true;
                    }
                    
                    // Contains match (only if the query is at least 5 characters)
                    if (normalizedQuery.length() >= 5 && (
                        normalizedTitle.contains(normalizedQuery) || 
                        normalizedQuery.contains(normalizedTitle))) {
                        return true;
                    }
                    
                    // Fuzzy match for longer titles
                    if (normalizedQuery.length() >= 8) {
                        double similarity = calculateSimilarity(normalizedQuery, normalizedTitle);
                        return similarity >= 0.8; // 80% similarity threshold
                    }
                    
                    return false;
                })
                .forEach(this::syncGameFromRawg);
        }
    }

    private double calculateSimilarity(String s1, String s2) {
        int maxLength = Math.max(s1.length(), s2.length());
        if (maxLength == 0) return 1.0;
        return 1.0 - ((double) levenshteinDistance(s1, s2) / maxLength);
    }

    private int levenshteinDistance(String s1, String s2) {
        int[][] dp = new int[s1.length() + 1][s2.length() + 1];
        
        for (int i = 0; i <= s1.length(); i++) {
            for (int j = 0; j <= s2.length(); j++) {
                if (i == 0) {
                    dp[i][j] = j;
                } else if (j == 0) {
                    dp[i][j] = i;
                } else {
                    dp[i][j] = Math.min(
                        dp[i - 1][j - 1] + (s1.charAt(i - 1) == s2.charAt(j - 1) ? 0 : 1),
                        Math.min(dp[i - 1][j] + 1, dp[i][j - 1] + 1)
                    );
                }
            }
        }
        
        return dp[s1.length()][s2.length()];
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