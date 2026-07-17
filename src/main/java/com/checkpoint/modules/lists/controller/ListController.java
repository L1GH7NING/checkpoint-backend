package com.checkpoint.modules.lists.controller;

import com.checkpoint.common.ApiResponse;
import com.checkpoint.modules.lists.dto.*;
import com.checkpoint.modules.lists.service.ListService;
import com.checkpoint.security.UserPrincipal;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/lists")
@RequiredArgsConstructor
public class ListController {

    private final ListService listService;

    @PostMapping
    public ResponseEntity<ApiResponse<ListResponse>> createList(
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody CreateListRequest request
    ) {
        ListResponse response = listService.createList(principal.id(), request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("List created", response));
    }

    @GetMapping("/{listId}")
    public ResponseEntity<ApiResponse<ListResponse>> getList(
            @AuthenticationPrincipal UserPrincipal principal, // nullable if endpoint allowed unauthenticated later
            @PathVariable UUID listId
    ) {
        UUID requestingUserId = principal != null ? principal.id() : null;
        return ResponseEntity.ok(
                ApiResponse.ok(listService.getList(requestingUserId, listId))
        );
    }

    @PatchMapping("/{listId}")
    public ResponseEntity<ApiResponse<ListResponse>> updateList(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable UUID listId,
            @Valid @RequestBody UpdateListRequest request
    ) {
        return ResponseEntity.ok(
                ApiResponse.ok(listService.updateList(principal.id(), listId, request))
        );
    }

    @DeleteMapping("/{listId}")
    public ResponseEntity<ApiResponse<Void>> deleteList(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable UUID listId
    ) {
        listService.deleteList(principal.id(), listId);
        return ResponseEntity.ok(ApiResponse.ok("List deleted", null));
    }

    // Browse a given user's lists — e.g. GET /api/v1/lists?userId=...
    @GetMapping
    public ResponseEntity<ApiResponse<List<ListSummaryResponse>>> getUserLists(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestParam UUID userId
    ) {
        UUID requestingUserId = principal != null ? principal.id() : null;
        return ResponseEntity.ok(
                ApiResponse.ok(listService.getUserLists(requestingUserId, userId))
        );
    }

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<List<ListSummaryResponse>>> getCurrentUserLists(
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        UUID userId = principal != null ? principal.id() : null;
        return ResponseEntity.ok(
                ApiResponse.ok(listService.getCurrentUserLists(userId))
        );
    }

    @PostMapping("/{listId}/games")
    public ResponseEntity<ApiResponse<List<ListItemResponse>>> addGames(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable UUID listId,
            @Valid @RequestBody AddGamesToListRequest request
    ) {
        List<ListItemResponse> response = listService.addGamesToList(principal.id(), listId, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Games added to list", response));
    }

    @DeleteMapping("/{listId}/games/{gameId}")
    public ResponseEntity<ApiResponse<Void>> removeGame(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable UUID listId,
            @PathVariable UUID gameId
    ) {
        listService.removeGameFromList(principal.id(), listId, gameId);
        return ResponseEntity.ok(ApiResponse.ok("Game removed from list", null));
    }

    @PatchMapping("/{listId}/reorder")
    public ResponseEntity<ApiResponse<ListResponse>> reorderItems(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable UUID listId,
            @Valid @RequestBody ReorderListItemsRequest request
    ) {
        return ResponseEntity.ok(
                ApiResponse.ok(listService.reorderItems(principal.id(), listId, request))
        );
    }
}