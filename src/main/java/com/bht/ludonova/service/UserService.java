package com.bht.ludonova.service;

import com.bht.ludonova.dto.steam.SteamUserDTO;
import com.bht.ludonova.dto.user.UserDTO;
import com.bht.ludonova.exception.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.bht.ludonova.model.SteamUser;
import com.bht.ludonova.model.User;
import com.bht.ludonova.repository.UserRepository;

import java.util.UUID;

@Service
@Transactional
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public User getOrCreateSteamUser(SteamUser steamUser) {
        return userRepository.findBySteamUser_SteamId(steamUser.getSteamId())
                .orElseGet(() -> createSteamUser(steamUser));
    }

    private User createSteamUser(SteamUser steamUser) {
        User user = new User();
        user.setUsername("steam_" + steamUser.getSteamId());
        user.setPassword(passwordEncoder.encode(UUID.randomUUID().toString()));
        user.setSteamUser(steamUser);
        return userRepository.save(user);
    }

    public UserDTO getCurrentUserDTO() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByUsername(username)
                .map(this::convertToDTO)
                .orElseThrow(() -> new AuthenticationException("User not found"));
    }

    public UserDTO getUserDTOByUsername(String username) {
        return userRepository.findByUsername(username)
                .map(this::convertToDTO)
                .orElseThrow(() -> new AuthenticationException("User not found: " + username));
    }

    public UserDTO convertToDTO(User user) {
        UserDTO dto = new UserDTO();
        dto.setId(user.getId());
        dto.setUsername(user.getUsername());
        dto.setEmail(user.getEmail());

        if (user.getSteamUser() != null) {
            SteamUserDTO steamUserDTO = new SteamUserDTO();
            steamUserDTO.setSteamId(user.getSteamUser().getSteamId());
            steamUserDTO.setPersonaName(user.getSteamUser().getPersonaName());
            steamUserDTO.setProfileUrl(user.getSteamUser().getProfileUrl());
            steamUserDTO.setAvatarUrl(user.getSteamUser().getAvatarUrl());
            dto.setSteamUser(steamUserDTO);
        }

        return dto;
    }
}