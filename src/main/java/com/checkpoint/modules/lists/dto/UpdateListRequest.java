package com.checkpoint.modules.lists.dto;

import com.checkpoint.modules.lists.entity.ListVisibility;
import jakarta.validation.constraints.Size;

public record UpdateListRequest(
        @Size(max = 150, message = "Title must not exceed 150 characters")
        String title,

        @Size(max = 2000, message = "Description must not exceed 2000 characters")
        String description,

        ListVisibility visibility
) {}