package com.checkpoint.modules.game.service;

import com.checkpoint.modules.game.dto.GameResponse;
import com.checkpoint.modules.game.dto.GameSearchRequest;
import com.checkpoint.modules.game.dto.GameSearchResponse;
import com.checkpoint.modules.game.entity.Game;

import java.util.List;

public interface GameService {
    List<GameSearchResponse> search(GameSearchRequest request);  // unified
    GameResponse getByIgdbId(Long igdbId);
    Game getOrFetch(Long igdbId);
    List<GameSearchResponse> fetchPopularGames(int limit);
    List<GameSearchResponse> fetchTopUpcomingGames(int limit, int offset);
}