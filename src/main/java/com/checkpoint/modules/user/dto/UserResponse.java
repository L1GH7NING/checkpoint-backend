package com.checkpoint.modules.user.dto;

import java.util.UUID;

public record UserResponse(
        UUID id,
        String username,
        String email,
        String displayName,
        String bio,
        String avatarUrl,
        boolean isPrivate
) {}