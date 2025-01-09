package com.bht.ludonova.service;

import com.bht.ludonova.dto.review.ReviewCreateDTO;
import com.bht.ludonova.dto.review.ReviewResponseDTO;
import com.bht.ludonova.dto.review.ReviewUpdateDTO;
import com.bht.ludonova.exception.GameNotFoundException;
import com.bht.ludonova.exception.ReviewNotFoundException;
import com.bht.ludonova.exception.UnauthorizedException;
import com.bht.ludonova.exception.DuplicateReviewException;
import com.bht.ludonova.model.Game;
import com.bht.ludonova.model.Review;
import com.bht.ludonova.model.User;
import com.bht.ludonova.repository.GameRepository;
import com.bht.ludonova.repository.ReviewRepository;
import com.bht.ludonova.repository.GameInstanceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ReviewService {
    private final ReviewRepository reviewRepository;
    private final GameRepository gameRepository;
    private final GameInstanceRepository gameInstanceRepository;
    private final UserService userService;

    public ReviewResponseDTO createReview(Long userId, ReviewCreateDTO dto) {
        User user = userService.getCurrentUser();
        Game game = gameRepository.findById(dto.getGameId())
                .orElseThrow(() -> new GameNotFoundException("Game not found"));

        // Check if user owns the game
        if (!gameInstanceRepository.existsByUserIdAndGameId(userId, dto.getGameId())) {
            throw new UnauthorizedException("You must own the game to review it");
        }

        // Check if user has already reviewed this game
        if (reviewRepository.findByUserIdAndGameId(userId, dto.getGameId()).isPresent()) {
            throw new DuplicateReviewException("You have already reviewed this game");
        }

        Review review = Review.builder()
                .user(user)
                .game(game)
                .rating(dto.getRating())
                .reviewText(dto.getReviewText())
                .createdAt(LocalDateTime.now())
                .build();
        review = reviewRepository.save(review);

        return mapToDTO(review);
    }

    public ReviewResponseDTO updateReview(Long userId, Long reviewId, ReviewUpdateDTO dto) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ReviewNotFoundException("Review not found"));

        if (!review.getUser().getId().equals(userId)) {
            throw new UnauthorizedException("You cannot edit this review");
        }

        if (dto.getRating() != null) {
            review.setRating(dto.getRating());
        }
        if (dto.getReviewText() != null) {
            review.setReviewText(dto.getReviewText());
        }

        review = reviewRepository.save(review);
        return mapToDTO(review);
    }

    public void deleteReview(Long userId, Long reviewId) {
        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ReviewNotFoundException("Review not found"));

        if (!review.getUser().getId().equals(userId)) {
            throw new UnauthorizedException("You cannot delete this review");
        }

        reviewRepository.delete(review);
    }

    public List<ReviewResponseDTO> getReviewsByGame(Long gameId) {
        return reviewRepository.findByGameId(gameId).stream()
                .map(this::mapToDTO)
                .toList();
    }

    public List<ReviewResponseDTO> getReviewsByUser(Long userId) {
        return reviewRepository.findByUserId(userId).stream()
                .map(this::mapToDTO)
                .toList();
    }

    public ReviewResponseDTO getReviewByIdResponse(Long id) {
        Review review = reviewRepository.findById(id)
                .orElseThrow(() -> new ReviewNotFoundException("Review with ID " + id + " not found"));
        return mapToDTO(review);
    }


    private ReviewResponseDTO mapToDTO(Review review) {
        return ReviewResponseDTO.builder()
                .id(review.getId())
                .gameId(review.getGame().getId())
                .gameTitle(review.getGame().getTitle())
                .userId(review.getUser().getId())
                .username(review.getUser().getUsername())
                .rating(review.getRating())
                .reviewText(review.getReviewText())
                .createdAt(review.getCreatedAt())
                .build();
    }

}

