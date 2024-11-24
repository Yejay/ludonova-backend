package com.bht.ludonova.controller;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import com.bht.ludonova.model.Game;
import com.bht.ludonova.service.GameService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/games")
@RequiredArgsConstructor
@Tag(name = "Games", description = "API for accessing game information")
public class GameController {
    private final GameService gameService;

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public Page<Game> searchGames(
            @RequestParam(required = false) String query,
            @PageableDefault(size = 20) Pageable pageable) {
        return gameService.searchGames(query, pageable);
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Game> getGame(@PathVariable Long id) {
        return gameService.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}
