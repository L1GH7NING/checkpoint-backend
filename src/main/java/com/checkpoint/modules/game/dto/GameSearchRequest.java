package com.checkpoint.modules.game.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.util.List;

public record GameSearchRequest(

        // Text search — game name
        String query,

        // Lookup by specific IGDB ids (supports multiple)
        @Size(max = 50, message = "Cannot request more than 50 ids at once")
        List<Long> igdbIds,

        // Genre filter — matches your IGDB genre ids
        List<Integer> genreIds,

        // Platform filter
        List<Integer> platformIds,

        // Release year range
        Integer releasedAfter,   // year e.g. 2020
        Integer releasedBefore,  // year e.g. 2024

        // Minimum IGDB rating threshold
        Double minRating,

        // --- NEW SORTING FIELDS ---
        @Pattern(regexp = "^(first_release_date|rating|rating_count|name)$", message = "Invalid sort field")
        String sortBy,

        @Pattern(regexp = "^(asc|desc)$", message = "Invalid sort direction. Use 'asc' or 'desc'")
        String sortDir,

        // Pagination
        @Min(1) @Max(50)
        Integer limit,

        Integer offset
) {
    // Sensible defaults so callers don't have to pass everything
    public int effectiveLimit()  { return limit  != null ? limit  : 50; }
    public int effectiveOffset() { return offset != null ? offset : 0;  }

    // Default sorting to rating_count descending if nothing is provided
    public String effectiveSortBy() {
        return (sortBy != null && !sortBy.isBlank()) ? sortBy : "rating_count";
    }
    public String effectiveSortDir() {
        return (sortDir != null && !sortDir.isBlank()) ? sortDir.toLowerCase() : "desc";
    }
}