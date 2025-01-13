package com.bht.ludonova.controller;

import com.bht.ludonova.dto.ErrorResponse;
import com.bht.ludonova.dto.user.CreateUserDTO;
import com.bht.ludonova.dto.user.UserDTO;
import com.bht.ludonova.dto.user.UserUpdateDTO;
import com.bht.ludonova.model.User;
import com.bht.ludonova.model.enums.Role;
import com.bht.ludonova.repository.UserRepository;
import com.bht.ludonova.service.UserService;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/user")
public class UserController {

    private final UserRepository userRepository;
    private final UserService userService;

    @Autowired
    public UserController(UserRepository userRepository, UserService userService) {
        this.userRepository = userRepository;
        this.userService = userService;
    }



    // Get current user's profile
    @GetMapping("/current")
    public ResponseEntity<User> getCurrentUser(@AuthenticationPrincipal UserDetails userDetails) {
        return userRepository.findByUsername(userDetails.getUsername())
                .map(user -> {
                    user.setPassword(null);
                    return ResponseEntity.ok(user);
                })
                .orElse(ResponseEntity.notFound().build());
    }

    // Update current user's profile
    @PutMapping("/current")
    public ResponseEntity<User> updateCurrentUser(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestBody UserUpdateDTO updateDTO) {
        return userRepository.findByUsername(userDetails.getUsername())
                .map(user -> {
                    if (updateDTO.getEmail() != null) {
                        user.setEmail(updateDTO.getEmail());
                    }
                    // Don't allow role updates through this endpoint
                    User updatedUser = userRepository.save(user);
                    updatedUser.setPassword(null);
                    return ResponseEntity.ok(updatedUser);
                })
                .orElse(ResponseEntity.notFound().build());
    }

    // Admin endpoints below

    @PostMapping("/create")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<?> createUser(@Valid @RequestBody CreateUserDTO createUserDTO) {
        try {
            UserDTO createdUser = userService.createUser(createUserDTO);
            return ResponseEntity.status(HttpStatus.CREATED).body(createdUser);
        } catch (IllegalArgumentException e) {
            return ResponseEntity
                    .badRequest()
                    .body(new ErrorResponse("USER_EXISTS", e.getMessage(), HttpStatus.BAD_REQUEST.value()));
        }
    }

    // Get all users (admin only)
    @GetMapping
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<List<User>> getAllUsers() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        log.debug("Current user authorities: {}", auth.getAuthorities());
        log.debug("Is authenticated: {}", auth.isAuthenticated());
        log.debug("Principal: {}", auth.getPrincipal());
        
        List<User> users = userRepository.findAll();
        users.forEach(user -> user.setPassword(null));
        return ResponseEntity.ok(users);
    }

    // Get specific user (admin only)
    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<User> getUser(@PathVariable Long id) {
        return userRepository.findById(id)
                .map(user -> {
                    user.setPassword(null);
                    return ResponseEntity.ok(user);
                })
                .orElse(ResponseEntity.notFound().build());
    }

    // Update any user (admin only)
    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<User> updateUser(
            @PathVariable Long id,
            @RequestBody UserUpdateDTO updateDTO) {
        return userRepository.findById(id)
                .map(user -> {
                    if (updateDTO.getEmail() != null) {
                        user.setEmail(updateDTO.getEmail());
                    }
                    if (updateDTO.getRole() != null) {
                        user.setRole(updateDTO.getRole());
                    }
                    User updatedUser = userRepository.save(user);
                    updatedUser.setPassword(null);
                    return ResponseEntity.ok(updatedUser);
                })
                .orElse(ResponseEntity.notFound().build());
    }

    // Delete user (admin only)
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        if (userRepository.existsById(id)) {
            userRepository.deleteById(id);
            return ResponseEntity.ok().build();
        }
        return ResponseEntity.notFound().build();
    }
}