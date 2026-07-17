package com.checkpoint.modules.lists.dto;

import com.checkpoint.modules.lists.entity.ListVisibility;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

// Lightweight version for grid/profile browsing — no full item list, just cover previews
public record ListSummaryResponse(
        UUID id,
        String title,
        String description,
        ListVisibility visibility,
        long gameCount,
        List<String> previewCoverUrls,
        LocalDateTime updatedAt
) {}