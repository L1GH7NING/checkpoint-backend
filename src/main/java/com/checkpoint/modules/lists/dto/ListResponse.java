package com.checkpoint.modules.lists.dto;

import com.checkpoint.modules.lists.entity.ListVisibility;
import com.checkpoint.modules.user.dto.UserSummaryResponse;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record ListResponse(
        UUID id,
        UserSummaryResponse owner,
        String title,
        String description,
        ListVisibility visibility,
        long gameCount,
        List<ListItemResponse> items,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {}