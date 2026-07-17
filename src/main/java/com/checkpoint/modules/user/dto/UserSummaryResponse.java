package com.checkpoint.modules.user.dto;

import java.util.UUID;

// Public-safe projection for embedding as "owner"/"author" in other modules' responses.
// Never include email, isPrivate, or anything account-related here.
public record UserSummaryResponse(
        UUID id,
        String username,
        String displayName,
        String avatarUrl
) {}