package com.checkpoint.modules.library.repository;

import com.checkpoint.modules.library.entity.GameStatus;
import com.checkpoint.modules.library.entity.UserGame;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

public interface UserGameRepository extends JpaRepository<UserGame, UUID> {

    boolean existsByUser_IdAndGame_Id(UUID userId, UUID gameId);

    Optional<UserGame> findByIdAndUser_Id(UUID id, UUID userId);

    @Query("""
        SELECT ug FROM UserGame ug
        WHERE ug.user.id = :userId AND ug.game.igdbId = :igdbId
    """)
    Optional<UserGame> findByUserIdAndIgdbId(
            @Param("userId") UUID userId,
            @Param("igdbId") Long igdbId
    );

    @Query(
            value = """
            SELECT ug FROM UserGame ug
            JOIN FETCH ug.game g
            WHERE ug.user.id = :userId
              AND (:status IS NULL OR ug.status = :status)
            ORDER BY ug.updatedAt DESC
        """,
            countQuery = """
            SELECT COUNT(ug) FROM UserGame ug
            WHERE ug.user.id = :userId
              AND (:status IS NULL OR ug.status = :status)
        """
    )
    Page<UserGame> findByUserId(
            @Param("userId") UUID userId,
            @Param("status") GameStatus status,
            Pageable pageable
    );

    @Query("""
        SELECT ug FROM UserGame ug
        JOIN FETCH ug.game
        WHERE ug.user.id = :userId AND ug.favorite = true
        ORDER BY ug.updatedAt DESC
    """)
    Page<UserGame> findFavoritesByUserId(@Param("userId") UUID userId, Pageable pageable);

    /**
     * Lightweight projection for the game detail status check.
     * Fetches only the 4 fields the UI needs — no full entity load.
     */
    @Query("""
        SELECT ug.id       AS userGameId,
               ug.status   AS status,
               ug.rating   AS rating,
               ug.favorite AS favorite
        FROM UserGame ug
        WHERE ug.user.id = :userId AND ug.game.igdbId = :igdbId
    """)
    Optional<GameStatusProjection> findStatusByUserIdAndIgdbId(
            @Param("userId") UUID userId,
            @Param("igdbId") Long igdbId
    );

    /**
     * Single aggregation query for profile stats — avoids 6 separate COUNT queries.
     */
    @Query("""
        SELECT
            COUNT(ug)                                                  AS total,
            SUM(CASE WHEN ug.status = 'PLAYING'   THEN 1 ELSE 0 END) AS playing,
            SUM(CASE WHEN ug.status = 'COMPLETED' THEN 1 ELSE 0 END) AS completed,
            SUM(CASE WHEN ug.status = 'BACKLOG'   THEN 1 ELSE 0 END) AS backlog,
            SUM(CASE WHEN ug.status = 'WISHLIST'  THEN 1 ELSE 0 END) AS wishlist,
            SUM(CASE WHEN ug.status = 'DROPPED'   THEN 1 ELSE 0 END) AS dropped,
            SUM(CASE WHEN ug.status = 'PAUSED'    THEN 1 ELSE 0 END) AS paused,
            SUM(CASE WHEN ug.favorite = true      THEN 1 ELSE 0 END) AS favorites
        FROM UserGame ug
        WHERE ug.user.id = :userId
    """)
    LibraryStatsProjection getLibraryStatsByUserId(@Param("userId") UUID userId);

    // ── Projections ───────────────────────────────────────────────────────────

    interface GameStatusProjection {
        UUID getUserGameId();
        GameStatus getStatus();
        BigDecimal getRating();
        boolean getFavorite();
    }

    interface LibraryStatsProjection {
        long getTotal();
        long getPlaying();
        long getCompleted();
        long getBacklog();
        long getWishlist();
        long getDropped();
        long getPaused();
        long getFavorites();
    }
}