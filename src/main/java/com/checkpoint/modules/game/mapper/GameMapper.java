package com.checkpoint.modules.game.mapper;

import com.checkpoint.modules.game.client.dto.IgdbGameDto;
import com.checkpoint.modules.game.dto.GameResponse;
import com.checkpoint.modules.game.dto.GameSearchResponse;
import com.checkpoint.modules.game.entity.Game;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@Component
public class GameMapper {

    // ── IGDB DTO → Entity ──────────────────────────────────────────────────

    public Game fromIgdbGame(IgdbGameDto dto) {
        Game game = new Game();
        game.setIgdbId(dto.id());
        game.setName(dto.name());
        game.setSummary(dto.summary());
        game.setStoryline(dto.storyline());
        game.setCoverUrl(normaliseCoverUrl(dto.cover()));
        game.setRating(dto.rating() != null ? BigDecimal.valueOf(dto.rating()) : null);
        game.setRatingCount(dto.ratingCount());
        game.setFirstReleaseDate(dto.firstReleaseDate());
        game.setGenres(extractGenres(dto.genres()));
        game.setPlatforms(extractPlatforms(dto.platforms()));
        game.setDevelopers(extractDevelopers(dto.involvedCompanies()));
        game.setScreenshots(extractScreenshots(dto.screenshots()));
        game.setSimilarIgdbIds(dto.similarGames() != null
                ? dto.similarGames().toArray(Long[]::new)
                : new Long[0]);
        game.setIgdbUrl(dto.url());
        game.setTrailerUrl(dto.videoUrl());
        return game;
    }

    // ── Entity → Response DTOs ─────────────────────────────────────────────

    public GameResponse toGameResponse(Game game) {
        return new GameResponse(
                game.getId(),
                game.getIgdbId(),
                game.getName(),
                game.getSummary(),
                game.getStoryline(),
                game.getCoverUrl(),
                game.getIgdbUrl(),
                game.getTrailerUrl(),
                game.getRating(),
                game.getRatingCount(),
                game.getFirstReleaseDate(),
                toList(game.getGenres()),
                toList(game.getPlatforms()),
                toList(game.getDevelopers()),
                toList(game.getScreenshots()),
                toList(game.getSimilarIgdbIds())
        );
    }

    public GameSearchResponse toSearchResponse(Game game) {
        return new GameSearchResponse(
                game.getId(),
                game.getIgdbId(),
                game.getName(),
                game.getCoverUrl(),
                game.getRating(),
                game.getFirstReleaseDate(),
                toList(game.getGenres()),
                toList(game.getPlatforms())
        );
    }

    // ── Package-accessible helpers (used by GameServiceImpl.fullFetchAndUpdate) ──

    public String normaliseCoverUrl(IgdbGameDto.Cover cover) {
        if (cover == null || cover.url() == null) return null;
        return cover.url()
                .replace("//", "https://")
                .replace("t_thumb", "t_cover_big");
    }

    public String[] extractGenres(List<IgdbGameDto.Genre> list) {
        if (list == null) return new String[0];
        return list.stream()
                .map(IgdbGameDto.Genre::name)
                .filter(n -> n != null && !n.isBlank())
                .toArray(String[]::new);
    }

    public String[] extractPlatforms(List<IgdbGameDto.Platform> list) {
        if (list == null) return new String[0];
        return list.stream()
                .map(IgdbGameDto.Platform::name)
                .filter(n -> n != null && !n.isBlank())
                .toArray(String[]::new);
    }

    public String[] extractDevelopers(List<IgdbGameDto.InvolvedCompany> companies) {
        if (companies == null) return new String[0];
        return companies.stream()
                .filter(IgdbGameDto.InvolvedCompany::developer)
                .map(ic -> ic.company() != null ? ic.company().name() : null)
                .filter(n -> n != null && !n.isBlank())
                .toArray(String[]::new);
    }

    public String[] extractScreenshots(List<IgdbGameDto.Screenshot> screenshots) {
        if (screenshots == null) return new String[0];
        return screenshots.stream()
                .map(IgdbGameDto.Screenshot::url)
                .filter(u -> u != null && !u.isBlank())
                .map(u -> u.replace("//", "https://")
                        .replace("t_thumb", "t_screenshot_big"))
                .toArray(String[]::new);
    }

    // ── Private ───────────────────────────────────────────────────────────

    private <T> List<T> toList(T[] arr) {
        return arr != null ? Arrays.asList(arr) : Collections.emptyList();
    }
}