package com.checkpoint.modules.lists.dto;

import com.checkpoint.modules.lists.entity.ListVisibility;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.util.List;

public record CreateListRequest(
        @NotBlank(message = "Title is required")
        @Size(max = 150, message = "Title must not exceed 150 characters")
        String title,

        @Size(max = 2000, message = "Description must not exceed 2000 characters")
        String description,

        ListVisibility visibility,  // defaults to PUBLIC if null

        List<Long> igdbIds  // optional — games to add at creation time, in order
) {}