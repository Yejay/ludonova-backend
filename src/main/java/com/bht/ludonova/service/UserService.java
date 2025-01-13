package com.bht.ludonova.service;

import com.bht.ludonova.dto.steam.SteamUserDTO;
import com.bht.ludonova.dto.user.CreateUserDTO;
import com.bht.ludonova.dto.user.UserDTO;
import com.bht.ludonova.dto.user.UserUpdateDTO;
import com.bht.ludonova.exception.AuthenticationException;
import com.bht.ludonova.exception.UnauthorizedException;
import com.bht.ludonova.exception.LudoNovaException;
import com.bht.ludonova.model.enums.Role;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import jakarta.mail.MessagingException;
import lombok.extern.slf4j.Slf4j;

import com.bht.ludonova.model.SteamUser;
import com.bht.ludonova.model.User;
import com.bht.ludonova.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional
@Slf4j
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder, EmailService emailService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.emailService = emailService;
    }

    public UserDTO createUser(CreateUserDTO createUserDTO) {
        // Check if username already exists
        if (userRepository.findByUsername(createUserDTO.getUsername()).isPresent()) {
            throw new IllegalArgumentException("Username already exists");
        }

        // Check if email already exists
        if (userRepository.findByEmail(createUserDTO.getEmail()).isPresent()) {
            throw new IllegalArgumentException("Email already exists");
        }

        // Generate verification code (6 digits)
        String verificationCode = String.format("%06d", (int) (Math.random() * 1000000));
        LocalDateTime codeExpiry = LocalDateTime.now().plusHours(24);

        User user = User.builder()
                .username(createUserDTO.getUsername())
                .password(passwordEncoder.encode(createUserDTO.getPassword()))
                .email(createUserDTO.getEmail())
                .role(createUserDTO.getRole())
                .emailVerified(false)
                .verificationCode(verificationCode)
                .verificationCodeExpiry(codeExpiry)
                .build();

        User savedUser = userRepository.save(user);

        // Send verification email
        try {
            emailService.sendVerificationEmail(user.getEmail(), verificationCode);
        } catch (MessagingException e) {
            throw new LudoNovaException("Failed to send verification email");
        }

        return convertToDTO(savedUser);
    }

    public void verifyEmail(String email, String code) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new LudoNovaException("User not found"));

        if (user.isEmailVerified()) {
            throw new LudoNovaException("Email is already verified");
        }

        if (user.getVerificationCodeExpiry().isBefore(LocalDateTime.now())) {
            throw new LudoNovaException("Verification code has expired");
        }

        if (!user.getVerificationCode().equals(code)) {
            throw new LudoNovaException("Invalid verification code");
        }

        user.setEmailVerified(true);
        user.setVerificationCode(null);
        user.setVerificationCodeExpiry(null);
        userRepository.save(user);

        // Log the verification for debugging
        log.info("Email verified for user: {}, emailVerified status: {}", user.getUsername(), user.isEmailVerified());
    }

    public void resendVerificationEmail(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new LudoNovaException("User not found"));

        if (user.isEmailVerified()) {
            throw new LudoNovaException("Email is already verified");
        }

        // Generate new verification code
        String verificationCode = String.format("%06d", (int) (Math.random() * 1000000));
        LocalDateTime codeExpiry = LocalDateTime.now().plusHours(24);

        user.setVerificationCode(verificationCode);
        user.setVerificationCodeExpiry(codeExpiry);
        userRepository.save(user);

        try {
            emailService.sendVerificationEmail(user.getEmail(), verificationCode);
        } catch (MessagingException e) {
            throw new LudoNovaException("Failed to send verification email");
        }
    }

    public User getOrCreateSteamUser(SteamUser steamUser) {
        return userRepository.findBySteamUser_SteamId(steamUser.getSteamId())
                .orElseGet(() -> createSteamUser(steamUser));
    }

    private User createSteamUser(SteamUser steamUser) {
        User user = new User();
        user.setUsername("steam_" + steamUser.getSteamId());
        user.setPassword(passwordEncoder.encode(UUID.randomUUID().toString()));
        user.setRole(Role.USER);
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
        dto.setRole(user.getRole());

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

    public List<UserDTO> getAllUsers() {
        checkAdminAccess();
        return userRepository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public UserDTO getUserById(Long id) {
        checkAdminAccess();
        return userRepository.findById(id)
                .map(this::convertToDTO)
                .orElseThrow(() -> new AuthenticationException("User not found: " + id));
    }

    public UserDTO updateUser(Long id, UserUpdateDTO updateDTO) {
        User currentUser = getCurrentUser();
        User userToUpdate = userRepository.findById(id)
                .orElseThrow(() -> new AuthenticationException("User not found: " + id));

        if (!currentUser.getRole().equals(Role.ADMIN) && !currentUser.getId().equals(id)) {
            throw new UnauthorizedException("You can only update your own profile");
        }

        if (updateDTO.getRole() != null) {
            if (!currentUser.getRole().equals(Role.ADMIN)) {
                throw new UnauthorizedException("Only admins can update roles");
            }
            userToUpdate.setRole(updateDTO.getRole());
        }

        if (updateDTO.getEmail() != null) {
            userToUpdate.setEmail(updateDTO.getEmail());
        }

        return convertToDTO(userRepository.save(userToUpdate));
    }

    public void deleteUser(Long id) {
        checkAdminAccess();
        userRepository.deleteById(id);
    }

    public User getCurrentUser() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new AuthenticationException("User not found"));
    }

    private void checkAdminAccess() {
        User currentUser = getCurrentUser();
        if (!currentUser.getRole().equals(Role.ADMIN)) {
            throw new UnauthorizedException("Admin access required");
        }
    }
}