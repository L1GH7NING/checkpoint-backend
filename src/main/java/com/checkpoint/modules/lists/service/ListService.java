package com.checkpoint.modules.lists.service;

import com.checkpoint.modules.lists.dto.*;

import java.util.List;
import java.util.UUID;

public interface ListService {
    ListResponse createList(UUID userId, CreateListRequest request);
    ListResponse updateList(UUID userId, UUID listId, UpdateListRequest request);
    void deleteList(UUID userId, UUID listId);
    ListResponse getList(UUID requestingUserId, UUID listId);
    List<ListSummaryResponse> getUserLists(UUID requestingUserId, UUID targetUserId);
    List<ListSummaryResponse> getCurrentUserLists(UUID userId);
    List<ListItemResponse> addGamesToList(UUID userId, UUID listId, AddGamesToListRequest request);
    void removeGameFromList(UUID userId, UUID listId, UUID gameId);
    ListResponse reorderItems(UUID userId, UUID listId, ReorderListItemsRequest request);
    int removeGamesFromList(UUID userId, UUID listId, List<UUID> gameIds);
}