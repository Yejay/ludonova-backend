package com.bht.ludonova.service;

import java.time.LocalDateTime;
import java.util.List;
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

    public Page<GameInstanceResponseDTO> getUserGameInstances(Long userId, Pageable pageable) {
        return gameInstanceMapper.toDTOPage(
                gameInstanceRepository.findByUserIdOrderByLastPlayedDesc(userId, pageable)
        );
    }

    public List<GameInstanceResponseDTO> getUserGameInstancesByStatus(Long userId, GameStatus status) {
        return gameInstanceMapper.toDTOList(
                gameInstanceRepository.findByUserIdAndStatus(userId, status)
        );
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
}