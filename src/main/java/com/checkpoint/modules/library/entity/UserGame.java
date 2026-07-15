package com.checkpoint.modules.library.entity;

import com.checkpoint.common.BaseEntity;
import com.checkpoint.modules.game.entity.Game;
import com.checkpoint.modules.auth.entity.User;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Represents a game in a user's library.
 * Design decisions:
 * - Composite unique constraint on (user_id, game_id) — a user can only have
 *   one library entry per game. Status changes update the same row via PATCH.
 * - rating stored as NUMERIC(3,1) → supports 0.0–10.0 with one decimal place.
 * - started_at / completed_at are LocalDate (not Timestamp) — users think in
 *   days, not exact timestamps, and this avoids timezone headaches.
 * - hours_played is nullable — not all users track time.
 * - is_favorite is a boolean flag for quick filtering of favourites without
 *   needing a separate table.
 */
@Getter
@Setter
@Entity
@Table(
        name = "user_games",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_user_games_user_game",
                        columnNames = {"user_id", "game_id"}
                )
        },
        indexes = {
                @Index(name = "idx_user_games_user_id",   columnList = "user_id"),
                @Index(name = "idx_user_games_game_id",   columnList = "game_id"),
                @Index(name = "idx_user_games_status",    columnList = "user_id, status"),
                @Index(name = "idx_user_games_favorite",  columnList = "user_id, is_favorite"),
                @Index(name = "idx_user_games_rating",    columnList = "user_id, rating")
        }
)
public class UserGame extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_user_games_user"))
    private User user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "game_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_user_games_game"))
    private Game game;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private GameStatus status;

    // NUMERIC(3,1) → 0.0 to 10.0, nullable until user explicitly rates
    @Column(name = "rating", precision = 3, scale = 1)
    private BigDecimal rating;

    // Nullable — not every user tracks playtime
    @Column(name = "hours_played")
    private Integer hoursPlayed;

    @Column(name = "story_completion")
    private Integer storyCompletion;

    @Column(name = "total_completion")
    private Integer totalCompletion;

    @Column(name = "achievements_completion")
    private Integer achievementsCompletion;

    @Column(name = "replays")
    private Integer replays;

    @Column(name = "notes", length = 1000)
    private String notes;

    @Column(name = "started_at")
    private LocalDate startedAt;

    @Column(name = "completed_at")
    private LocalDate completedAt;

    @Column(name = "is_favorite", nullable = false)
    private boolean favorite = false;

    @Column(name = "platform", length = 50)
    private String platform;

    protected UserGame() {}

    public UserGame(User user, Game game, GameStatus status) {
        this.user   = user;
        this.game   = game;
        this.status = status;
    }
}