package com.bht.ludonova.controller;

import com.bht.ludonova.dto.user.UserUpdateDTO;
import com.bht.ludonova.model.User;
import com.bht.ludonova.model.enums.Role;
import com.bht.ludonova.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/user")
public class UserController {

    @Autowired
    private UserRepository userRepository;

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
    
    // Get all users (admin only)
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<User>> getAllUsers() {
        List<User> users = userRepository.findAll();
        users.forEach(user -> user.setPassword(null));
        return ResponseEntity.ok(users);
    }

    // Get specific user (admin only)
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
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
    @PreAuthorize("hasRole('ADMIN')")
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
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        if (userRepository.existsById(id)) {
            userRepository.deleteById(id);
            return ResponseEntity.ok().build();
        }
        return ResponseEntity.notFound().build();
    }
}