package com.bht.ludonova.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.bht.ludonova.dto.gameInstance.*;
import com.bht.ludonova.exception.*;
import com.bht.ludonova.model.*;
import com.bht.ludonova.model.enums.GameStatus;
import com.bht.ludonova.repository.*;
import com.bht.ludonova.mapper.GameInstanceMapper;

import lombok.RequiredArgsConstructor;

@Service
@Transactional
@RequiredArgsConstructor
public class GameInstanceService {
    private static final Logger log = LoggerFactory.getLogger(GameInstanceService.class);
    private final GameInstanceRepository gameInstanceRepository;
    private final GameRepository gameRepository;
    private final UserRepository userRepository;
    private final GameInstanceMapper gameInstanceMapper;

    public GameInstanceResponseDTO createGameInstance(Long userId, GameInstanceCreateDTO dto) {
        // Check if user already has this game
        if (gameInstanceRepository.existsByUserIdAndGameId(userId, dto.getGameId())) {
            throw new GameAlreadyAddedException("Game already in user's list");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        Game game = gameRepository.findById(dto.getGameId())
                .orElseThrow(() -> new GameNotFoundException("Game not found"));

        GameInstance gameInstance = GameInstance.builder()
                .user(user)
                .game(game)
                .status(dto.getStatus())
                .progressPercentage(dto.getProgressPercentage() != null ? dto.getProgressPercentage() : 0)
                .playTime(dto.getPlayTime() != null ? dto.getPlayTime() : 0)
                .notes(dto.getNotes())
                .addedAt(LocalDateTime.now())
                .lastPlayed(dto.getStatus() == GameStatus.PLAYING ? LocalDateTime.now() : null)
                .build();

        return gameInstanceMapper.toDTO(gameInstanceRepository.save(gameInstance));
    }

    public GameInstanceResponseDTO updateGameInstance(Long userId, Long instanceId, GameInstanceUpdateDTO dto) {
        GameInstance instance = gameInstanceRepository.findById(instanceId)
                .orElseThrow(() -> new GameInstanceNotFoundException("Game instance not found"));

        // Verify ownership
        if (!instance.getUser().getId().equals(userId)) {
            throw new UnauthorizedException("Not authorized to update this game instance");
        }

        // Update fields if provided
        if (dto.getStatus() != null) {
            boolean wasPlaying = instance.getStatus() == GameStatus.PLAYING;
            boolean isNowPlaying = dto.getStatus() == GameStatus.PLAYING;

            instance.setStatus(dto.getStatus());

            // Update lastPlayed if transitioning to PLAYING
            if (!wasPlaying && isNowPlaying) {
                instance.setLastPlayed(LocalDateTime.now());
            }
        }

        if (dto.getProgressPercentage() != null) {
            instance.setProgressPercentage(dto.getProgressPercentage());
        }

        if (dto.getPlayTime() != null) {
            instance.setPlayTime(dto.getPlayTime());
        }

        if (dto.getNotes() != null) {
            instance.setNotes(dto.getNotes());
        }

        return gameInstanceMapper.toDTO(gameInstanceRepository.save(instance));
    }

    public Page<GameInstanceResponseDTO> getUserGameInstances(Long userId, Pageable pageable, GameStatus status, String sort) {
        Page<GameInstance> instances;
        
        if (status != null) {
            switch (sort) {
                case "playTime":
                    instances = gameInstanceRepository.findByUserIdAndStatusOrderByPlayTimeDesc(userId, status, pageable);
                    break;
                case "lastPlayed":
                default:
                    instances = gameInstanceRepository.findByUserIdAndStatusOrderByLastPlayedDesc(userId, status, pageable);
                    break;
            }
        } else {
            switch (sort) {
                case "playTime":
                    instances = gameInstanceRepository.findByUserIdOrderByPlayTimeDesc(userId, pageable);
                    break;
                case "lastPlayed":
                default:
                    instances = gameInstanceRepository.findByUserIdOrderByLastPlayedDesc(userId, pageable);
                    break;
            }
        }

        return gameInstanceMapper.toDTOPage(instances);
    }

    public List<GameInstanceResponseDTO> getUserGameInstancesByStatus(Long userId, GameStatus status) {
        return gameInstanceMapper.toDTOList(
                gameInstanceRepository.findByUserIdAndStatus(userId, status)
        );
    }

    public GameInstanceResponseDTO getGameInstanceByGame(Long userId, Long gameId) {
        log.info("Looking up game instance for user {} and game {}", userId, gameId);
        try {
            return gameInstanceRepository.findByUserIdAndGameId(userId, gameId)
                    .map(instance -> {
                        log.info("Found game instance: {}", instance);
                        return gameInstanceMapper.toDTO(instance);
                    })
                    .orElseGet(() -> {
                        log.info("No game instance found");
                        return null;
                    });
        } catch (Exception e) {
            log.error("Error finding game instance", e);
            throw e;
        }
    }

    public GameInstanceResponseDTO updateGameStatus(Long userId, Long instanceId, GameStatus status) {
        GameInstanceUpdateDTO updateDTO = new GameInstanceUpdateDTO();
        updateDTO.setStatus(status);
        return updateGameInstance(userId, instanceId, updateDTO);
    }

    public void deleteGameInstance(Long userId, Long instanceId) {
        GameInstance instance = gameInstanceRepository.findById(instanceId)
                .orElseThrow(() -> new GameInstanceNotFoundException("Game instance not found"));

        // Verify ownership
        if (!instance.getUser().getId().equals(userId)) {
            throw new UnauthorizedException("Not authorized to delete this game instance");
        }

        gameInstanceRepository.delete(instance);
    }

    public GameInstanceStatsDTO getUserGameStats(Long userId) {
        List<GameInstance> instances = gameInstanceRepository.findByUserId(userId);
        
        int playing = 0;
        int completed = 0;
        int planToPlay = 0;
        int dropped = 0;
        int totalPlayTime = 0;
        double totalCompletion = 0;
        int gamesWithCompletion = 0;

        for (GameInstance instance : instances) {
            switch (instance.getStatus()) {
                case PLAYING -> playing++;
                case COMPLETED -> completed++;
                case PLAN_TO_PLAY -> planToPlay++;
                case DROPPED -> dropped++;
            }

            if (instance.getPlayTime() != null) {
                totalPlayTime += instance.getPlayTime();
            }

            if (instance.getProgressPercentage() != null && instance.getProgressPercentage() > 0) {
                totalCompletion += instance.getProgressPercentage();
                gamesWithCompletion++;
            }
        }

        double averageCompletion = gamesWithCompletion > 0 
            ? totalCompletion / gamesWithCompletion 
            : 0.0;

        return GameInstanceStatsDTO.builder()
                .totalGames(instances.size())
                .playing(playing)
                .completed(completed)
                .planToPlay(planToPlay)
                .dropped(dropped)
                .totalPlayTime(totalPlayTime)
                .averageCompletion(averageCompletion)
                .build();
    }

    @Transactional
    public List<GameInstanceResponseDTO> createGameInstances(Long userId, GameInstanceBatchCreateDTO dto) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        return dto.getGameInstances().stream()
                .map(entry -> {
                    // Check if user already has this game
                    if (gameInstanceRepository.existsByUserIdAndGameId(userId, entry.getGameId())) {
                        throw new GameAlreadyAddedException("Game already in user's list: " + entry.getGameId());
                    }

                    Game game = gameRepository.findById(entry.getGameId())
                            .orElseThrow(() -> new GameNotFoundException("Game not found: " + entry.getGameId()));

                    GameInstance gameInstance = GameInstance.builder()
                            .user(user)
                            .game(game)
                            .status(entry.getStatus())
                            .progressPercentage(entry.getProgressPercentage() != null ? entry.getProgressPercentage() : 0)
                            .playTime(entry.getPlayTime() != null ? entry.getPlayTime() : 0)
                            .notes(entry.getNotes())
                            .addedAt(LocalDateTime.now())
                            .lastPlayed(entry.getLastPlayed() != null ? entry.getLastPlayed() : 
                                      (entry.getStatus() == GameStatus.PLAYING ? LocalDateTime.now() : null))
                            .build();

                    return gameInstanceMapper.toDTO(gameInstanceRepository.save(gameInstance));
                })
                .collect(Collectors.toList());
    }
}