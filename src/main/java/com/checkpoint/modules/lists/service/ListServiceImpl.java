package com.checkpoint.modules.lists.service;

import com.checkpoint.modules.game.entity.Game;
import com.checkpoint.modules.game.service.GameService;
import com.checkpoint.modules.lists.dto.*;
import com.checkpoint.modules.lists.entity.GameList;
import com.checkpoint.modules.lists.entity.ListItem;
import com.checkpoint.modules.lists.entity.ListVisibility;
import com.checkpoint.modules.lists.exception.ListAccessDeniedException;
import com.checkpoint.modules.lists.exception.ListNotFoundException;
import com.checkpoint.modules.lists.mapper.ListMapper;
import com.checkpoint.modules.lists.repository.GameListRepository;
import com.checkpoint.modules.lists.repository.ListItemRepository;
import com.checkpoint.modules.user.entity.User;
import com.checkpoint.modules.user.exception.UserNotFoundException;
import com.checkpoint.modules.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class ListServiceImpl implements ListService {

    private final GameListRepository gameListRepository;
    private final ListItemRepository listItemRepository;
    private final UserRepository userRepository;
    private final GameService gameService;
    private final ListMapper listMapper;
    // private final ActivityService activityService; // wire up in Phase 2

    @Override
    public ListResponse createList(UUID userId, CreateListRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));

        GameList list = new GameList();
        list.setUser(user);
        list.setTitle(request.title());
        list.setDescription(request.description());
        list.setVisibility(request.visibility() != null ? request.visibility() : ListVisibility.PUBLIC);
        GameList saved = gameListRepository.save(list);

        List<ListItem> items = List.of();
        if (request.igdbIds() != null && !request.igdbIds().isEmpty()) {
            items = insertGames(saved, request.igdbIds(), 0);
        }

        return listMapper.toResponse(saved, items, items.size());
    }

    @Override
    public ListResponse updateList(UUID userId, UUID listId, UpdateListRequest request) {
        GameList list = requireOwnedList(userId, listId);

        if (request.title() != null) list.setTitle(request.title());
        if (request.description() != null) list.setDescription(request.description());
        if (request.visibility() != null) list.setVisibility(request.visibility());

        GameList saved = gameListRepository.save(list);
        List<ListItem> items = listItemRepository.findByListIdOrderByPosition(listId);
        return listMapper.toResponse(saved, items, items.size());
    }

    @Override
    public void deleteList(UUID userId, UUID listId) {
        requireOwnedList(userId, listId);
        listItemRepository.deleteByListId(listId);
        gameListRepository.deleteById(listId);
    }

    @Override
    @Transactional(readOnly = true)
    public ListResponse getList(UUID requestingUserId, UUID listId) {
        GameList list = gameListRepository.findById(listId)
                .orElseThrow(() -> new ListNotFoundException(listId));

        checkViewAccess(requestingUserId, list);

        List<ListItem> items = listItemRepository.findByListIdOrderByPosition(listId);
        return listMapper.toResponse(list, items, items.size());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ListSummaryResponse> getUserLists(UUID requestingUserId, UUID targetUserId) {

        List<GameList> lists = gameListRepository.findByUserIdAndVisibilityOrderByUpdatedAtDesc(targetUserId, ListVisibility.PUBLIC);

        return lists.stream()
                .map(list -> {
                    List<ListItem> preview = listItemRepository.findPreviewItems(list.getId());
                    long count = listItemRepository.countByListId(list.getId());
                    return listMapper.toSummary(list, preview, count);
                })
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<ListSummaryResponse> getCurrentUserLists(UUID userId) {

        List<GameList> lists = gameListRepository.findByUserIdOrderByUpdatedAtDesc(userId);

        return lists.stream()
                .map(list -> {
                    List<ListItem> preview = listItemRepository.findPreviewItems(list.getId());
                    long count = listItemRepository.countByListId(list.getId());
                    return listMapper.toSummary(list, preview, count);
                })
                .toList();
    }

    @Override
    public List<ListItemResponse> addGamesToList(UUID userId, UUID listId, AddGamesToListRequest request) {
        GameList list = requireOwnedList(userId, listId);
        int startPosition = listItemRepository.findMaxPosition(listId) + 1;

        List<ListItem> inserted = insertGames(list, request.igdbIds(), startPosition);

        return inserted.stream().map(listMapper::toItemResponse).toList();
    }

    // ListServiceImpl.java
    // ListServiceImpl.java
    @Override
    public int removeGamesFromList(UUID userId, UUID listId, List<UUID> gameIds) {
        requireOwnedList(userId, listId);
        List<UUID> deduped = gameIds.stream().distinct().toList();
        return listItemRepository.deleteByListIdAndGameIdIn(listId, deduped);
        // returns count actually removed — lets the controller report partial success
        // (e.g. client asked to remove 5, only 3 were in the list)
    }

    @Override
    public void removeGameFromList(UUID userId, UUID listId, UUID gameId) {
        removeGamesFromList(userId, listId, List.of(gameId));
    }

    @Override
    public ListResponse reorderItems(UUID userId, UUID listId, ReorderListItemsRequest request) {
        GameList list = requireOwnedList(userId, listId);

        List<ListItem> items = listItemRepository.findByListIdOrderByPosition(listId);
        Map<UUID, ListItem> itemsById = items.stream()
                .collect(Collectors.toMap(ListItem::getId, i -> i));

        List<UUID> order = request.orderedItemIds();
        for (int i = 0; i < order.size(); i++) {
            ListItem item = itemsById.get(order.get(i));
            if (item == null) {
                throw new com.checkpoint.exception.AppException(
                        HttpStatus.BAD_REQUEST,
                        "INVALID_ITEM_ID",
                        "Item " + order.get(i) + " does not belong to this list"
                );
            }
            item.setPosition(i);
        }
        listItemRepository.saveAll(itemsById.values());

        List<ListItem> reordered = listItemRepository.findByListIdOrderByPosition(listId);
        return listMapper.toResponse(list, reordered, reordered.size());
    }

    @Override
    @Transactional(readOnly = true)
    public ListMembershipResponse getListMembership(UUID userId, Long igdbId) {
        Game game = gameService.getOrFetch(igdbId);
        List<UUID> listIds = listItemRepository.findListIdsByUserIdAndGameId(userId, game.getId());
        return new ListMembershipResponse(listIds);
    }

    // --- helpers ---

    /**
     * Shared batch-insert for createList (initial games) and addGamesToList (later additions).
     * Dedupes igdbIds (preserving order), skips games already in the list, resolves each
     * through GameService.getOrFetch (local-first IGDB flow), and assigns sequential
     * positions starting at startPosition.
     */
    private List<ListItem> insertGames(GameList list, List<Long> igdbIds, int startPosition) {
        List<Long> deduped = igdbIds.stream().distinct().toList();

        List<ListItem> toSave = new ArrayList<>();
        int position = startPosition;

        for (Long igdbId : deduped) {
            Game game = gameService.getOrFetch(igdbId);

            if (listItemRepository.existsByListIdAndGameId(list.getId(), game.getId())) {
                continue; // silently skip dupes rather than failing the whole batch
            }

            ListItem item = new ListItem();
            item.setList(list);
            item.setGame(game);
            item.setPosition(position++);
            toSave.add(item);
        }

        return listItemRepository.saveAll(toSave);
    }

    private GameList requireOwnedList(UUID userId, UUID listId) {
        GameList list = gameListRepository.findById(listId)
                .orElseThrow(() -> new ListNotFoundException(listId));
        if (!list.getUser().getId().equals(userId)) {
            throw new ListAccessDeniedException();
        }
        return list;
    }

    private void checkViewAccess(UUID requestingUserId, GameList list) {
        if (list.getVisibility() == ListVisibility.PRIVATE
                && !list.getUser().getId().equals(requestingUserId)) {
            throw new ListAccessDeniedException();
        }
        // PUBLIC and UNLISTED are both viewable by direct ID lookup
    }
}