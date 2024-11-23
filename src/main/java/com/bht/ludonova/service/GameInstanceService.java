package com.bht.ludonova.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

import com.bht.ludonova.model.GameInstance;
import com.bht.ludonova.model.enums.GameStatus;
import com.bht.ludonova.repository.GameInstanceRepository;

@Service
@Transactional
@RequiredArgsConstructor
public class GameInstanceService {
    private final GameInstanceRepository gameInstanceRepository;

    public GameInstance save(GameInstance gameInstance) {
        if (gameInstance.getStatus() == GameStatus.PLAYING) {
            gameInstance.setLastPlayed(LocalDateTime.now());
        }
        return gameInstanceRepository.save(gameInstance);
    }

    public Optional<GameInstance> findById(Long id) {
        return gameInstanceRepository.findById(id);
    }

    public List<GameInstance> findByUserId(Long userId) {
        return gameInstanceRepository.findByUserId(userId);
    }

    public List<GameInstance> findByUserIdAndStatus(Long userId, GameStatus status) {
        return gameInstanceRepository.findByUserIdAndStatus(userId, status);
    }

    public Optional<GameInstance> findByUserIdAndGameId(Long userId, Long gameId) {
        return gameInstanceRepository.findByUserIdAndGameId(userId, gameId);
    }

    public boolean existsByUserIdAndGameId(Long userId, Long gameId) {
        return gameInstanceRepository.existsByUserIdAndGameId(userId, gameId);
    }

    public void updateStatus(Long id, GameStatus newStatus) {
        findById(id).ifPresent(instance -> {
            instance.setStatus(newStatus);
            if (newStatus == GameStatus.PLAYING) {
                instance.setLastPlayed(LocalDateTime.now());
            }
            save(instance);
        });
    }

    public void deleteById(Long id) {
        gameInstanceRepository.deleteById(id);
    }
}
