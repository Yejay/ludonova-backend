package com.bht.ludonova.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.bht.ludonova.model.GameInstance;
import com.bht.ludonova.model.enums.GameStatus;
import com.bht.ludonova.service.GameInstanceService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/game-instances")
@RequiredArgsConstructor
public class GameInstanceController {
    private final GameInstanceService gameInstanceService;

    @GetMapping("/user/{userId}")
    public List<GameInstance> getUserGameInstances(@PathVariable Long userId) {
        return gameInstanceService.findByUserId(userId);
    }

    @GetMapping("/user/{userId}/status/{status}")
    public List<GameInstance> getUserGameInstancesByStatus(
            @PathVariable Long userId,
            @PathVariable GameStatus status) {
        return gameInstanceService.findByUserIdAndStatus(userId, status);
    }

    @PostMapping
    public ResponseEntity<GameInstance> createGameInstance(@RequestBody GameInstance gameInstance) {
        return ResponseEntity.ok(gameInstanceService.save(gameInstance));
    }

    @PutMapping("/{id}")
    public ResponseEntity<GameInstance> updateGameInstance(
            @PathVariable Long id,
            @RequestBody GameInstance gameInstance) {
        if (!id.equals(gameInstance.getId())) {
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.ok(gameInstanceService.save(gameInstance));
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<Void> updateGameStatus(
            @PathVariable Long id,
            @RequestParam GameStatus status) {
        gameInstanceService.updateStatus(id, status);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteGameInstance(@PathVariable Long id) {
        gameInstanceService.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
