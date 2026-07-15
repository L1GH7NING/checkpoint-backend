package com.checkpoint.modules.library.service;

import com.checkpoint.modules.library.dto.LibraryDTOs.*;
import com.checkpoint.modules.library.entity.GameStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface LibraryService {

    LibraryEntryResponse addToLibrary(UUID userId, AddToLibraryRequest request);

    LibraryEntryResponse updateEntry(UUID userId, UUID userGameId, UpdateLibraryRequest request);

    void removeFromLibrary(UUID userId, UUID userGameId);

    LibraryEntryResponse getLibraryEntry(UUID userId, UUID userGameId);

    Page<LibraryEntryResponse> getMyLibrary(UUID userId, GameStatus status, Pageable pageable);

    Page<LibraryEntryResponse> getUserLibrary(UUID targetUserId, GameStatus status, Pageable pageable);

    LibraryStatsResponse getLibraryStats(UUID userId);

    /**
     * Returns the authenticated user's status for a specific game.
     * Used by the game detail page to decide what to render.
     * Never throws 404 — if the game isn't in the library,
     * returns GameStatusResponse.notInLibrary().
     */
    GameStatusResponse getGameStatus(UUID userId, Long igdbId);
}