package com.checkpoint.modules.game.controller;

import com.checkpoint.common.ApiResponse;
import com.checkpoint.modules.game.dto.GameResponse;
import com.checkpoint.modules.game.dto.GameSearchRequest;
import com.checkpoint.modules.game.dto.GameSearchResponse;
import com.checkpoint.modules.game.service.GameService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Validated  // needed for @NotBlank/@Size on @RequestParam
@RestController
@RequestMapping("/api/v1/games")
@RequiredArgsConstructor
public class GameController {

    private final GameService gameService;

    /**
     * GET /api/v1/games/search?q=sonic
     * Public endpoint — no JWT required (permitted in SecurityConfig)
     */
    @PostMapping("/search")
    public ResponseEntity<ApiResponse<List<GameSearchResponse>>> search(
            @Valid @RequestBody GameSearchRequest request
    ) {
        return ResponseEntity.ok(ApiResponse.ok(gameService.search(request)));
    }

    @GetMapping("/{igdbId}")
    public ResponseEntity<ApiResponse<GameResponse>> getGame(
            @PathVariable Long igdbId
    ) {
        return ResponseEntity.ok(ApiResponse.ok(gameService.getByIgdbId(igdbId)));
    }

    @GetMapping("/popular")
    public ResponseEntity<ApiResponse<List<GameSearchResponse>>> getPopularGames(
            @RequestParam(defaultValue = "10") int limit
    ) {
        return ResponseEntity.ok(ApiResponse.ok(gameService.fetchPopularGames(limit)));
    }

    @GetMapping("/upcoming")
    public ResponseEntity<ApiResponse<List<GameSearchResponse>>> getUpcomingGames(
            @RequestParam(defaultValue = "10") int limit,
            @RequestParam(defaultValue = "0") int offset
    ) {
        return ResponseEntity.ok(ApiResponse.ok(gameService.fetchTopUpcomingGames(limit, offset)));
    }
}