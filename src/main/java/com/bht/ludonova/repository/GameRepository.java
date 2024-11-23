package com.bht.ludonova.repository;

import com.bht.ludonova.model.Game;
import com.bht.ludonova.model.enums.GameSource;
import com.bht.ludonova.model.enums.Platform;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface GameRepository extends JpaRepository<Game, Long> {
    Optional<Game> findByApiIdAndSource(String apiId, GameSource source);
    List<Game> findByPlatform(Platform platform);
}