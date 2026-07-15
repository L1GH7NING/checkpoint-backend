package com.checkpoint.modules.library.service;

import com.checkpoint.modules.auth.entity.User;
import com.checkpoint.modules.auth.repository.UserRepository;
import com.checkpoint.modules.game.entity.Game;
import com.checkpoint.modules.game.service.GameService;
import com.checkpoint.modules.library.dto.LibraryDTOs.*;
import com.checkpoint.modules.library.entity.GameStatus;
import com.checkpoint.modules.library.entity.UserGame;
import com.checkpoint.modules.library.exception.DuplicateLibraryEntryException;
import com.checkpoint.modules.library.exception.LibraryEntryNotFoundException;
import com.checkpoint.modules.library.mapper.LibraryMapper;
import com.checkpoint.modules.library.repository.UserGameRepository;
import com.checkpoint.modules.library.repository.UserGameRepository.LibraryStatsProjection;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class LibraryServiceImpl implements LibraryService {

    private final UserGameRepository userGameRepository;
    private final UserRepository     userRepository;
    private final GameService        gameService;
    private final LibraryMapper      libraryMapper;

    // ── Add to Library ────────────────────────────────────────────────────────

    @Override
    @Transactional
    public LibraryEntryResponse addToLibrary(UUID userId, AddToLibraryRequest request) {
        User userRef = userRepository.getReferenceById(userId);
        Game game    = gameService.getOrFetch(request.igdbId());

        if (userGameRepository.existsByUser_IdAndGame_Id(userId, game.getId())) {
            throw new DuplicateLibraryEntryException(
                    "Game already in library. Use PATCH to update status."
            );
        }

        UserGame entry = new UserGame(userRef, game, request.status());
        applyOptionalFields(entry, request);

        return libraryMapper.toResponse(userGameRepository.save(entry));
    }

    // ── Update Entry ──────────────────────────────────────────────────────────

    @Override
    @Transactional
    public LibraryEntryResponse updateEntry(UUID userId, UUID userGameId, UpdateLibraryRequest request) {
        UserGame entry = userGameRepository.findByIdAndUser_Id(userGameId, userId)
                .orElseThrow(() -> new LibraryEntryNotFoundException(userGameId));

        // status and isFavorite are never "cleared", so keep null guard
        if (request.status()     != null) entry.setStatus(request.status());
        if (request.isFavorite() != null) entry.setFavorite(request.isFavorite());

        // These can be intentionally cleared — always apply
        entry.setRating(request.rating());
        entry.setHoursPlayed(request.hoursPlayed());
        entry.setPlatform(request.platform());
        entry.setNotes(request.notes());
        entry.setStartedAt(request.startedAt());
        entry.setCompletedAt(request.completedAt());
        entry.setStoryCompletion(request.storyCompletion());
        entry.setTotalCompletion(request.totalCompletion());
        entry.setAchievementsCompletion(request.achievementsCompletion());
        entry.setReplays(request.replays());

        return libraryMapper.toResponse(userGameRepository.save(entry));
    }

    // ── Remove from Library ───────────────────────────────────────────────────

    @Override
    @Transactional
    public void removeFromLibrary(UUID userId, UUID userGameId) {
        UserGame entry = userGameRepository.findByIdAndUser_Id(userGameId, userId)
                .orElseThrow(() -> new LibraryEntryNotFoundException(userGameId));
        userGameRepository.delete(entry);
    }

    // ── Get Library Entry ─────────────────────────────────────────────────────
    @Override
    public LibraryEntryResponse getLibraryEntry(UUID userId, UUID userGameId) {
        UserGame entry = userGameRepository.findByIdAndUser_Id(userGameId, userId)
                .orElseThrow(() -> new LibraryEntryNotFoundException(userGameId));
        return libraryMapper.toResponse(entry);
    }

    // ── Get My Library ────────────────────────────────────────────────────────

    @Override
    public Page<LibraryEntryResponse> getMyLibrary(UUID userId, GameStatus status, Pageable pageable) {
        return userGameRepository
                .findByUserId(userId, status, pageable)
                .map(libraryMapper::toResponse);
    }

    // ── Get Another User's Library ────────────────────────────────────────────

    @Override
    public Page<LibraryEntryResponse> getUserLibrary(UUID targetUserId, GameStatus status, Pageable pageable) {
        if (!userRepository.existsById(targetUserId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found");
        }
        return userGameRepository
                .findByUserId(targetUserId, status, pageable)
                .map(libraryMapper::toResponse);
    }

    // ── Game Detail Status ────────────────────────────────────────────────────

    @Override
    public GameStatusResponse getGameStatus(UUID userId, Long igdbId) {
        return userGameRepository
                .findByUserIdAndIgdbId(userId, igdbId)
                .map(entry -> new GameStatusResponse(
                        true,
                        entry.getId(),
                        entry.getStatus(),
                        entry.getRating(),
                        entry.isFavorite(),
                        entry.getPlatform(),
                        entry.getHoursPlayed(),
                        entry.getNotes(),
                        entry.getStartedAt(),
                        entry.getCompletedAt()
                ))
                .orElse(GameStatusResponse.notInLibrary());
    }

    // ── Library Stats ─────────────────────────────────────────────────────────

    @Override
    public LibraryStatsResponse getLibraryStats(UUID userId) {
        LibraryStatsProjection p = userGameRepository.getLibraryStatsByUserId(userId);
        return new LibraryStatsResponse(
                p.getTotal(), p.getPlaying(), p.getCompleted(),
                p.getBacklog(), p.getWishlist(), p.getDropped(),
                p.getPaused(), p.getFavorites()
        );
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private void applyOptionalFields(UserGame entry, AddToLibraryRequest req) {
        if (req.rating()      != null) entry.setRating(req.rating());
        if (req.hoursPlayed() != null) entry.setHoursPlayed(req.hoursPlayed());
        if (req.startedAt()   != null) entry.setStartedAt(req.startedAt());
        if (req.completedAt() != null) entry.setCompletedAt(req.completedAt());
        if (req.isFavorite()  != null) entry.setFavorite(req.isFavorite());
        if (req.platform()    != null) entry.setPlatform(req.platform());
        if (req.storyCompletion() != null) entry.setStoryCompletion(req.storyCompletion());
        if (req.totalCompletion() != null) entry.setTotalCompletion(req.totalCompletion());
        if (req.achievementsCompletion() != null) entry.setAchievementsCompletion(req.achievementsCompletion());
        if (req.replays() != null) entry.setReplays(req.replays());
    }
}