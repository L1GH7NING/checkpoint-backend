package com.checkpoint.modules.lists.dto;

import com.checkpoint.modules.game.dto.GameSearchResponse;

import java.time.LocalDateTime;
import java.util.UUID;

public record ListItemResponse(
        UUID id,
        GameSearchResponse game,
        Integer position,
        LocalDateTime addedAt
) {}