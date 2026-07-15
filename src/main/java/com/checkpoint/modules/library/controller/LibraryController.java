package com.checkpoint.modules.library.controller;

import com.checkpoint.common.ApiResponse;
import com.checkpoint.common.PageResponse;
import com.checkpoint.modules.library.dto.LibraryDTOs.*;
import com.checkpoint.modules.library.entity.GameStatus;
import com.checkpoint.modules.library.service.LibraryService;
import com.checkpoint.security.UserPrincipal;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/library")
@RequiredArgsConstructor
public class LibraryController {

    private final LibraryService libraryService;

    // ── POST /api/v1/library ─────────────────────────────────────────────────

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<LibraryEntryResponse> addToLibrary(
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody AddToLibraryRequest request
    ) {
        return ApiResponse.ok("Game added to library",
                libraryService.addToLibrary(principal.id(), request));
    }

    // ── PATCH /api/v1/library/{userGameId} ───────────────────────────────────

    @PatchMapping("/{userGameId}")
    public ApiResponse<LibraryEntryResponse> updateEntry(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable UUID userGameId,
            @Valid @RequestBody UpdateLibraryRequest request
    ) {
        return ApiResponse.ok("Library entry updated",
                libraryService.updateEntry(principal.id(), userGameId, request));
    }

    // ── DELETE /api/v1/library/{userGameId} ──────────────────────────────────

    @DeleteMapping("/{userGameId}")
    public ApiResponse<Void> removeFromLibrary(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable UUID userGameId
    ) {
        libraryService.removeFromLibrary(principal.id(), userGameId);
        return ApiResponse.ok("Game removed from library", null);
    }

    // GET /api/v1/library/{userGameId} ───────────────────────────────────────────

    @GetMapping("/{userGameId}")
    public ApiResponse<LibraryEntryResponse> getEntry(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable UUID userGameId
    ) {
        return ApiResponse.ok(libraryService.getLibraryEntry(principal.id(), userGameId));
    }

    @GetMapping("/me/games/{igdbId}/status")
    public ApiResponse<GameStatusResponse> getGameStatus(
            @AuthenticationPrincipal UserPrincipal principal,
            @PathVariable Long igdbId
    ) {
        return ApiResponse.ok(libraryService.getGameStatus(principal.id(), igdbId));
    }

    // ── GET /api/v1/library/me ───────────────────────────────────────────────

    @GetMapping("/me")
    public ApiResponse<PageResponse<LibraryEntryResponse>> getMyLibrary(
            @AuthenticationPrincipal UserPrincipal principal,
            @RequestParam(required = false) GameStatus status,
            @RequestParam(defaultValue = "0")  int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        var pageable = PageRequest.of(page, size, Sort.by("updatedAt").descending());
        Page<LibraryEntryResponse> result = libraryService.getMyLibrary(principal.id(), status, pageable);
        return ApiResponse.ok(PageResponse.of(result));
    }

    // ── GET /api/v1/library/me/stats ─────────────────────────────────────────

    @GetMapping("/me/stats")
    public ApiResponse<LibraryStatsResponse> getMyStats(
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        return ApiResponse.ok(libraryService.getLibraryStats(principal.id()));
    }

    // ── GET /api/v1/library/users/{targetUserId} ─────────────────────────────

    @GetMapping("/users/{targetUserId}")
    public ApiResponse<PageResponse<LibraryEntryResponse>> getUserLibrary(
            @PathVariable UUID targetUserId,
            @RequestParam(required = false) GameStatus status,
            @RequestParam(defaultValue = "0")  int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        var pageable = PageRequest.of(page, size, Sort.by("updatedAt").descending());
        Page<LibraryEntryResponse> result = libraryService.getUserLibrary(targetUserId, status, pageable);
        return ApiResponse.ok(PageResponse.of(result));
    }

    // ── GET /api/v1/library/users/{targetUserId}/stats ───────────────────────

    @GetMapping("/users/{targetUserId}/stats")
    public ApiResponse<LibraryStatsResponse> getUserStats(
            @PathVariable UUID targetUserId
    ) {
        return ApiResponse.ok(libraryService.getLibraryStats(targetUserId));
    }
}