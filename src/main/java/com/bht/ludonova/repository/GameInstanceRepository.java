package com.bht.ludonova.repository;

import com.bht.ludonova.model.GameInstance;
import com.bht.ludonova.model.enums.GameStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface GameInstanceRepository extends JpaRepository<GameInstance, Long> {
    List<GameInstance> findByUserId(Long userId);
    Optional<GameInstance> findByUserIdAndGameId(Long userId, Long gameId);
    boolean existsByUserIdAndGameId(Long userId, Long gameId);

    @Query("SELECT gi FROM GameInstance gi WHERE gi.user.id = :userId AND gi.status = :status")
    List<GameInstance> findByUserIdAndStatus(Long userId, GameStatus status);
}
