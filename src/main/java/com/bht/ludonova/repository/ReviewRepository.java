package com.bht.ludonova.repository;

import com.bht.ludonova.model.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {
    List<Review> findByGameId(Long gameId);
    List<Review> findByUserId(Long userId);
    Optional<Review> findByUserIdAndGameId(Long userId, Long gameId);
}
