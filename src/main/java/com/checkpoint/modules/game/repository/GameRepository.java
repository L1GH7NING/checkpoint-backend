package com.checkpoint.modules.game.repository;

import com.checkpoint.modules.game.entity.Game;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface GameRepository extends JpaRepository<Game, UUID> {

    // Primary lookup — used by IgdbSyncService on every game request
    Optional<Game> findByIgdbId(Long igdbId);

    boolean existsByIgdbId(Long igdbId);
}