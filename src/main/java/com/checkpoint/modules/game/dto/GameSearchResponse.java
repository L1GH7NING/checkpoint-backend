package com.checkpoint.modules.game.dto;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

// Lean projection — only what the Flutter search result card needs
public record GameSearchResponse(
        UUID id,
        Long igdbId,
        String name,
        String coverUrl,
        BigDecimal rating,
        Long firstReleaseDate,
        List<String> genres,
        List<String> platforms
) {}