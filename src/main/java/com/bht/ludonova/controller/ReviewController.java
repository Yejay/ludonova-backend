package com.bht.ludonova.controller;

import com.bht.ludonova.dto.review.ReviewCreateDTO;
import com.bht.ludonova.dto.review.ReviewResponseDTO;
import com.bht.ludonova.dto.review.ReviewUpdateDTO;
import com.bht.ludonova.service.ReviewService;
import com.bht.ludonova.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/reviews")
@RequiredArgsConstructor
public class ReviewController {
    private final ReviewService reviewService;
    private final UserService userService;

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ReviewResponseDTO> createReview(@RequestBody @Valid ReviewCreateDTO dto) {
        Long userId = userService.getCurrentUser().getId();
        return ResponseEntity.ok(reviewService.createReview(userId, dto));
    }

    @PutMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ReviewResponseDTO> updateReview(@PathVariable Long id, @RequestBody @Valid ReviewUpdateDTO dto) {
        Long userId = userService.getCurrentUser().getId();
        return ResponseEntity.ok(reviewService.updateReview(userId, id, dto));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> deleteReview(@PathVariable Long id) {
        Long userId = userService.getCurrentUser().getId();
        reviewService.deleteReview(userId, id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/game/{gameId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<ReviewResponseDTO>> getReviewsByGame(@PathVariable Long gameId) {
        return ResponseEntity.ok(reviewService.getReviewsByGame(gameId));
    }

    @GetMapping("/user/{userId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<ReviewResponseDTO>> getReviewsByUser(@PathVariable Long userId) {
        return ResponseEntity.ok(reviewService.getReviewsByUser(userId));
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ReviewResponseDTO> getReviewById(@PathVariable Long id) {
        return ResponseEntity.ok(reviewService.getReviewByIdResponse(id));
    }

}

