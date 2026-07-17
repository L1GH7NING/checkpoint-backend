package com.checkpoint.modules.lists.dto;

import jakarta.validation.constraints.NotEmpty;

import java.util.List;
import java.util.UUID;

// Client sends the full ordered list of item IDs after a drag-and-drop reorder
public record ReorderListItemsRequest(
        @NotEmpty(message = "Item order is required")
        List<UUID> orderedItemIds
) {}