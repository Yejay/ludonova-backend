package com.bht.ludonova.repository;

import com.bht.ludonova.model.GameInstance;
import com.bht.ludonova.model.enums.GameStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface GameInstanceRepository extends JpaRepository<GameInstance, Long> {

    // Find all user's games with pagination and ordering by lastPlayed
    @Query("SELECT gi FROM GameInstance gi WHERE gi.user.id = :userId ORDER BY gi.lastPlayed DESC NULLS LAST")
    Page<GameInstance> findByUserIdOrderByLastPlayedDesc(@Param("userId") Long userId, Pageable pageable);

    @Query("SELECT gi FROM GameInstance gi WHERE gi.user.id = :userId ORDER BY gi.playTime DESC")
    Page<GameInstance> findByUserIdOrderByPlayTimeDesc(@Param("userId") Long userId, Pageable pageable);

    // Find all games with specific status
    @Query("SELECT gi FROM GameInstance gi WHERE gi.user.id = :userId AND gi.status = :status ORDER BY gi.lastPlayed DESC NULLS LAST")
    List<GameInstance> findByUserIdAndStatus(@Param("userId") Long userId, @Param("status") GameStatus status);

    // Check if user already has this game
    boolean existsByUserIdAndGameId(Long userId, Long gameId);

    // Find specific game instance by user and game
    Optional<GameInstance> findByUserIdAndGameId(Long userId, Long gameId);

    // Basic find by user (you might want to deprecate this in favor of the pageable version)
    @Deprecated
    List<GameInstance> findByUserId(Long userId);

    @Query("SELECT gi FROM GameInstance gi WHERE gi.user.id = :userId AND gi.status = :status ORDER BY gi.lastPlayed DESC NULLS LAST")
    Page<GameInstance> findByUserIdAndStatusOrderByLastPlayedDesc(@Param("userId") Long userId, @Param("status") GameStatus status, Pageable pageable);

    @Query("SELECT gi FROM GameInstance gi WHERE gi.user.id = :userId AND gi.status = :status ORDER BY gi.playTime DESC")
    Page<GameInstance> findByUserIdAndStatusOrderByPlayTimeDesc(@Param("userId") Long userId, @Param("status") GameStatus status, Pageable pageable);
}