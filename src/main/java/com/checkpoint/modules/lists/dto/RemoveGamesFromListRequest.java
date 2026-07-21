package com.checkpoint.modules.lists.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

import java.util.List;
import java.util.UUID;

// RemoveGamesFromListRequest.java
public record RemoveGamesFromListRequest(
        @NotEmpty @Size(max = 100) List<UUID> gameIds
) {}
