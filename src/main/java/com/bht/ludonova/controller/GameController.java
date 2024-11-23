package com.bht.ludonova.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import com.bht.ludonova.model.Game;
import com.bht.ludonova.model.enums.Platform;
import com.bht.ludonova.service.GameService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/games")
@RequiredArgsConstructor
@Tag(name = "Games", description = "API for managing games") // Tag for Swagger grouping
public class GameController {
    private final GameService gameService;

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public List<Game> getAllGames() {
        return gameService.findAll();
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Game> getGame(@PathVariable Long id) {
        return gameService.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/platform/{platform}")
    @PreAuthorize("isAuthenticated()")
    public List<Game> getGamesByPlatform(@PathVariable Platform platform) {
        return gameService.findByPlatform(platform);
    }

    @PostMapping
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<Game> createGame(@RequestBody Game game) {
        return ResponseEntity.ok(gameService.save(game));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<Game> updateGame(@PathVariable Long id, @RequestBody Game game) {
        if (!id.equals(game.getId())) {
            return ResponseEntity.badRequest().build();
        }
        return ResponseEntity.ok(gameService.save(game));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ROLE_ADMIN')")
    public ResponseEntity<Void> deleteGame(@PathVariable Long id) {
        gameService.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
