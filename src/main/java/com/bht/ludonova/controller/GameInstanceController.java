package com.bht.ludonova.controller;

import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import com.bht.ludonova.dto.ErrorResponse;
import com.bht.ludonova.dto.gameInstance.*;
import com.bht.ludonova.model.User;
import com.bht.ludonova.model.enums.GameStatus;
import com.bht.ludonova.exception.*;
import com.bht.ludonova.service.GameInstanceService;
import com.bht.ludonova.service.UserService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/game-instances")
@RequiredArgsConstructor
public class GameInstanceController {
    private final GameInstanceService gameInstanceService;
    private final UserService userService;

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Page<GameInstanceResponseDTO>> getUserGameInstances(Pageable pageable) {
        User currentUser = userService.getCurrentUser();
        return ResponseEntity.ok(gameInstanceService.getUserGameInstances(currentUser.getId(), pageable));
    }

    @GetMapping("/status/{status}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<GameInstanceResponseDTO>> getUserGameInstancesByStatus(
            @PathVariable GameStatus status) {
        User currentUser = userService.getCurrentUser();
        return ResponseEntity.ok(gameInstanceService.getUserGameInstancesByStatus(currentUser.getId(), status));
    }

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> createGameInstance(@RequestBody @Valid GameInstanceCreateDTO dto) {
        try {
            User currentUser = userService.getCurrentUser();
            GameInstanceResponseDTO created = gameInstanceService.createGameInstance(currentUser.getId(), dto);
            return ResponseEntity.ok(created);
        } catch (GameAlreadyAddedException e) {
            return ResponseEntity
                    .badRequest()
                    .body(new ErrorResponse("GAME_ALREADY_EXISTS", "Game already in your list", 400));
        } catch (GameNotFoundException e) {
            return ResponseEntity
                    .notFound()
                    .build();
        }
    }

    @PutMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> updateGameInstance(
            @PathVariable Long id,
            @RequestBody @Valid GameInstanceUpdateDTO dto) {
        try {
            User currentUser = userService.getCurrentUser();
            GameInstanceResponseDTO updated = gameInstanceService.updateGameInstance(currentUser.getId(), id, dto);
            return ResponseEntity.ok(updated);
        } catch (GameInstanceNotFoundException | UnauthorizedException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<?> updateGameStatus(
            @PathVariable Long id,
            @RequestParam GameStatus status) {
        try {
            User currentUser = userService.getCurrentUser();
            GameInstanceResponseDTO updated = gameInstanceService.updateGameStatus(currentUser.getId(), id, status);
            return ResponseEntity.ok(updated);
        } catch (GameInstanceNotFoundException | UnauthorizedException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> deleteGameInstance(@PathVariable Long id) {
        try {
            User currentUser = userService.getCurrentUser();
            gameInstanceService.deleteGameInstance(currentUser.getId(), id);
            return ResponseEntity.noContent().build();
        } catch (GameInstanceNotFoundException | UnauthorizedException e) {
            return ResponseEntity.notFound().build();
        }
    }
}