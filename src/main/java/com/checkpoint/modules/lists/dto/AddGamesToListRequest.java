package com.checkpoint.modules.lists.dto;

import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public record AddGamesToListRequest(
        @NotEmpty(message = "At least one game is required")
        List<Long> igdbIds
) {}