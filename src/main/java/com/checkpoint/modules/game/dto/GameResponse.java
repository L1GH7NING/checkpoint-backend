package com.checkpoint.modules.game.dto;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public record GameResponse(
        UUID id,
        Long igdbId,
        String name,
        String summary,
        String storyline,
        String coverUrl,
        String igdbUrl,
        String trailerUrl,
        BigDecimal rating,
        Integer ratingCount,
        Long firstReleaseDate,
        List<String> genres,
        List<String> platforms,
        List<String> developers,
        List<String> screenshots,
        List<Long> similarIgdbIds
) {}