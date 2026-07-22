package com.checkpoint.modules.game.service;

import com.checkpoint.exception.AppException;
import com.checkpoint.modules.game.client.IgdbClient;
import com.checkpoint.modules.game.client.dto.IgdbGameDto;
import com.checkpoint.modules.game.dto.GameResponse;
import com.checkpoint.modules.game.dto.GameSearchRequest;
import com.checkpoint.modules.game.dto.GameSearchResponse;
import com.checkpoint.modules.game.entity.Game;
import com.checkpoint.modules.game.mapper.GameMapper;
import com.checkpoint.modules.game.repository.GameRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class GameServiceImpl implements GameService {

    private final GameRepository gameRepository;
    private final IgdbClient igdbClient;
    private final GameMapper gameMapper;

    // ── Search ─────────────────────────────────────────────────────────────

    @Override
    @Transactional
    @Cacheable(value = "searchCache", key = "#request")
    public List<GameSearchResponse> search(GameSearchRequest request) {
        List<IgdbGameDto> results = igdbClient.search(request);
        return results.stream()
                .map(this::saveIfAbsent)
                .map(gameMapper::toSearchResponse)
                .toList();
    }

    // ── Get by IGDB id ─────────────────────────────────────────────────────

    @Override
    @Transactional
    @Cacheable(value = "gameDetails", key = "#igdbId")
    public GameResponse getByIgdbId(Long igdbId) {
        Game game = getOrFetch(igdbId);
        return gameMapper.toGameResponse(game);
    }

    @Override
    @Transactional
    @Cacheable(value = "popularGames", key = "#limit")
    public List<GameSearchResponse> fetchPopularGames(int limit) {
        List<IgdbGameDto> popularGames = igdbClient.fetchTrendingGames(limit);
        return popularGames.stream()
                .map(this::saveIfAbsent)
                .map(gameMapper::toSearchResponse)
                .toList();
    }

    @Override
    @Transactional
    @Cacheable(value = "upcomingGames", key = "#limit + '-' + #offset")
    public List<GameSearchResponse> fetchTopUpcomingGames(int limit, int offset) {
        List<IgdbGameDto> upcomingGames = igdbClient.fetchTopUpcomingGames(limit, offset);
        return upcomingGames.stream()
                .map(this::saveIfAbsent)
                .map(gameMapper::toSearchResponse)
                .toList();
    }

    // ── Internal: called by library, review, and other modules ────────────

    @Override
    @Transactional
    public Game getOrFetch(Long igdbId) {
        return gameRepository.findByIgdbId(igdbId)
                .map(existing -> {
                    if (existing.getLastSyncedAt() == null) {
                        return fullFetchAndUpdate(existing);
                    }
                    return existing;
                })
                .orElseGet(() -> fetchFromIgdbAndSave(igdbId));
    }



    // ── Private helpers ────────────────────────────────────────────────────

    /**
     * Saves a lightweight game from search results.
     * lastSyncedAt is intentionally left null to mark this as incomplete.
     * If the game already exists (from a previous search or detail fetch),
     * we return the existing row untouched.
     */
    private Game saveIfAbsent(IgdbGameDto dto) {
        return gameRepository.findByIgdbId(dto.id())
                .orElseGet(() -> {
                    Game game = gameMapper.fromIgdbGame(dto);
                    // lastSyncedAt stays null — marks this as a partial cache entry
                    return gameRepository.save(game);
                });
    }

    /**
     * Full fetch from IGDB /games by id.
     * Used when a game doesn't exist in our DB at all.
     * Sets lastSyncedAt to mark the game as fully cached.
     */
    private Game fetchFromIgdbAndSave(Long igdbId) {
        log.info("Game igdbId={} not found locally — fetching full details from IGDB", igdbId);
        return igdbClient.fetchById(igdbId)
                .map(dto -> {
                    Game game = gameMapper.fromIgdbGame(dto);
                    game.setLastSyncedAt(LocalDateTime.now());
                    return gameRepository.save(game);
                })
                .orElseThrow(() -> new AppException(
                        HttpStatus.NOT_FOUND,
                        "GAME_NOT_FOUND",
                        "Game not found with IGDB id: " + igdbId
                ));
    }

    /**
     * Game exists in DB from a previous search but is missing detail fields.
     * Fetches full data from IGDB and updates the existing row in place.
     * Sets lastSyncedAt so subsequent calls skip this and return immediately.
     */
    private Game fullFetchAndUpdate(Game existing) {
        log.info("Game igdbId={} is incomplete — fetching full details from IGDB",
                existing.getIgdbId());

        return igdbClient.fetchById(existing.getIgdbId())
                .map(dto -> {
                    existing.setName(dto.name());
                    existing.setSummary(dto.summary());
                    existing.setStoryline(dto.storyline());
                    existing.setCoverUrl(gameMapper.normaliseCoverUrl(dto.cover()));
                    existing.setRating(dto.rating() != null
                            ? BigDecimal.valueOf(dto.rating()) : null);
                    existing.setRatingCount(dto.ratingCount());
                    existing.setFirstReleaseDate(dto.firstReleaseDate());
                    existing.setGenres(gameMapper.extractGenres(dto.genres()));
                    existing.setPlatforms(gameMapper.extractPlatforms(dto.platforms()));
                    existing.setDevelopers(gameMapper.extractDevelopers(dto.involvedCompanies()));
                    existing.setScreenshots(gameMapper.extractScreenshots(dto.screenshots()));
                    existing.setSimilarIgdbIds(dto.similarGames() != null
                            ? dto.similarGames().toArray(Long[]::new) : new Long[0]);
                    existing.setLastSyncedAt(LocalDateTime.now());
                    return gameRepository.save(existing);
                })
                // If IGDB no longer has this game, return what we have rather than 404ing
                .orElse(existing);
    }
}