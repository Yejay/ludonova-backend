package com.bht.ludonova.service;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.bht.ludonova.model.Game;
import com.bht.ludonova.model.enums.GameSource;
import com.bht.ludonova.model.enums.Platform;
import com.bht.ludonova.repository.GameRepository;

import lombok.RequiredArgsConstructor;

@Service
@Transactional
@RequiredArgsConstructor
public class GameService {
    private final GameRepository gameRepository;

    public Game save(Game game) {
        return gameRepository.save(game);
    }

    public Optional<Game> findById(Long id) {
        return gameRepository.findById(id);
    }

    public List<Game> findAll() {
        return gameRepository.findAll();
    }

    public List<Game> findByPlatform(Platform platform) {
        return gameRepository.findByPlatform(platform);
    }

    public Optional<Game> findByApiIdAndSource(String apiId, GameSource source) {
        return gameRepository.findByApiIdAndSource(apiId, source);
    }

    public void deleteById(Long id) {
        gameRepository.deleteById(id);
    }
}
