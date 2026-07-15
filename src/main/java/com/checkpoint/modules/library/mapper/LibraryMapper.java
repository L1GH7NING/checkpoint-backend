package com.checkpoint.modules.library.mapper;

import com.checkpoint.modules.game.entity.Game;
import com.checkpoint.modules.library.dto.LibraryDTOs.*;
import com.checkpoint.modules.library.entity.UserGame;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;

@Component
public class LibraryMapper {

    public LibraryEntryResponse toResponse(UserGame ug) {
        return new LibraryEntryResponse(
                ug.getId(),
                toGameSummary(ug.getGame()),
                ug.getStatus(),
                ug.getPlatform(),
                ug.getRating(),
                ug.getHoursPlayed(),
                ug.getStoryCompletion(),
                ug.getTotalCompletion(),
                ug.getAchievementsCompletion(),
                ug.getReplays(),
                ug.getNotes(),
                ug.getStartedAt(),
                ug.getCompletedAt(),
                ug.isFavorite(),
                ug.getCreatedAt(),
                ug.getUpdatedAt()
        );
    }

    private GameSummary toGameSummary(Game game) {
        Integer releaseYear = null;
        if (game.getFirstReleaseDate() != null) {
            // IGDB stores first_release_date as Unix epoch seconds
            releaseYear = LocalDate.ofInstant(
                    Instant.ofEpochSecond(game.getFirstReleaseDate()), ZoneOffset.UTC
            ).getYear();
        }

        return new GameSummary(
                game.getId(),
                game.getIgdbId(),
                game.getName(),
                game.getCoverUrl(),
                releaseYear
        );
    }
}