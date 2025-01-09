package com.bht.ludonova.controller;

import com.bht.ludonova.dto.gameInstance.GameInstanceResponseDTO;
import com.bht.ludonova.dto.steam.SteamGamesResponseDTO;
import com.bht.ludonova.mapper.GameInstanceMapper;
import com.bht.ludonova.model.GameInstance;
import com.bht.ludonova.model.User;
import com.bht.ludonova.service.SteamService;
import com.bht.ludonova.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/steam")
@RequiredArgsConstructor
public class SteamController {
    private final SteamService steamService;
    private final UserService userService;
    private final GameInstanceMapper gameInstanceMapper;

    @PostMapping("/sync")
    public ResponseEntity<Void> syncSteamLibrary() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) {
            log.error("No authentication found in security context");
            return ResponseEntity.status(401).build();
        }

        User user = userService.getCurrentUser();
        if (user == null) {
            log.error("Could not find user for authentication: {}", authentication.getName());
            return ResponseEntity.status(401).build();
        }

        log.info("Received request to sync Steam library for user: {}", user.getUsername());
        steamService.syncSteamLibrary(user);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/library")
    public ResponseEntity<SteamGamesResponseDTO> getSteamLibrary() {
        User user = userService.getCurrentUser();
        log.info("Received request to get Steam library for user: {}", user.getUsername());
        return ResponseEntity.ok(steamService.getSteamLibrary(user));
    }

    @PostMapping("/games/import")
    public ResponseEntity<GameInstanceResponseDTO> importSteamGame(@RequestBody SteamGamesResponseDTO.Game steamGame) {
        User user = userService.getCurrentUser();
        log.info("Received request to import Steam game {} for user: {}", steamGame.getName(), user.getUsername());
        
        GameInstance gameInstance = steamService.importSteamGame(user, steamGame);
        GameInstanceResponseDTO response = gameInstanceMapper.toDTO(gameInstance);
        
        return ResponseEntity.ok(response);
    }
} 