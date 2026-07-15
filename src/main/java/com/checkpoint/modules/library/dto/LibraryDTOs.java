package com.checkpoint.modules.library.dto;

import com.checkpoint.modules.library.entity.GameStatus;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

public class LibraryDTOs {

    // ── Request: Add a game to library ───────────────────────────────────────

    public record AddToLibraryRequest(

            @NotNull(message = "igdbId is required")
            Long igdbId,

            @NotNull(message = "status is required")
            GameStatus status,

            @DecimalMin(value = "0.0", message = "Rating must be at least 0.0")
            @DecimalMax(value = "10.0", message = "Rating must be at most 10.0")
            BigDecimal rating,

            @Min(value = 0, message = "Hours played cannot be negative")
            Integer hoursPlayed,

            @Min(value = 0, message = "Completion percentage cannot be negative")
            @Max(value = 100, message = "Completion percentage cannot be greater than 100")
            Integer storyCompletion,

            @Min(value = 0, message = "Completion percentage cannot be negative")
            @Max(value = 100, message = "Completion percentage cannot be greater than 100")
            Integer totalCompletion,

            @Min(value = 0, message = "Completion percentage cannot be negative")
            @Max(value = 100, message = "Completion percentage cannot be greater than 100")
            Integer achievementsCompletion,

            @Min(value = 0, message = "Replays cannot be negative")
            Integer replays,

            String notes,

            String platform,
            LocalDate startedAt,
            LocalDate completedAt,
            Boolean isFavorite

    ) {}

    // ── Request: Update a library entry (all fields optional) ────────────────

    public record UpdateLibraryRequest(

            GameStatus status,

            @DecimalMin(value = "0.0", message = "Rating must be at least 0.0")
            @DecimalMax(value = "10.0", message = "Rating must be at most 10.0")
            BigDecimal rating,

            @Min(value = 0, message = "Hours played cannot be negative")
            Integer hoursPlayed,

            @Min(value = 0, message = "Completion percentage cannot be negative")
            @Max(value = 100, message = "Completion percentage cannot be greater than 100")
            Integer storyCompletion,

            @Min(value = 0, message = "Completion percentage cannot be negative")
            @Max(value = 100, message = "Completion percentage cannot be greater than 100")
            Integer totalCompletion,

            @Min(value = 0, message = "Completion percentage cannot be negative")
            @Max(value = 100, message = "Completion percentage cannot be greater than 100")
            Integer achievementsCompletion,

            @Min(value = 0, message = "Replays cannot be negative")
            Integer replays,

            String notes,
            String platform,
            LocalDate startedAt,
            LocalDate completedAt,
            Boolean isFavorite

    ) {}

    // ── Response: A single library entry ─────────────────────────────────────

    public record LibraryEntryResponse(
            UUID id,
            GameSummary game,
            GameStatus status,
            String platform,
            BigDecimal rating,
            Integer hoursPlayed,
            Integer storyCompletion,
            Integer totalCompletion,
            Integer achievementsCompletion,
            Integer replays,
            String notes,
            LocalDate startedAt,
            LocalDate completedAt,
            boolean isFavorite,
            LocalDateTime createdAt,
            LocalDateTime updatedAt
    ) {}

    // ── Nested: Game summary embedded in library response ────────────────────

    public record GameSummary(
            UUID id,
            Long igdbId,
            String name,
            String coverUrl,
            Integer releaseYear
    ) {}

    public record GameStatusResponse(
            boolean inLibrary,
            UUID userGameId,      // null when inLibrary = false
            GameStatus status,    // null when inLibrary = false
            BigDecimal rating,    // null when not yet rated
            boolean isFavorite,
            String platform,
            Integer hoursPlayed,
            String notes,
            LocalDate startedAt,
            LocalDate completedAt
    ) {
        public static GameStatusResponse notInLibrary() {
            return new GameStatusResponse(false, null, null, null, false, null, null, null, null, null);
        }
    }

    // ── Response: Library stats for a user's profile ─────────────────────────

    public record LibraryStatsResponse(
            long totalGames,
            long playing,
            long completed,
            long backlog,
            long wishlist,
            long dropped,
            long paused,
            long favorites
    ) {}
}