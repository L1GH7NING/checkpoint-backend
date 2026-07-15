package com.checkpoint.modules.game.client;

import com.checkpoint.config.IgdbProperties;
import com.checkpoint.modules.game.client.dto.IgdbGameDto;
import com.checkpoint.modules.game.client.dto.IgdbMultiQueryResult;
import com.checkpoint.modules.game.client.dto.IgdbPopularityDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import com.checkpoint.modules.game.dto.GameSearchRequest;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class IgdbClient {

    private final RestTemplate restTemplate;
    private final IgdbAuthService igdbAuthService;
    private final IgdbProperties igdbProperties;

    // Fields to fetch for a full game — covers everything we store in the games table
    private static final String GAME_FIELDS = """
            fields id, name, summary, storyline,
            url,
            cover.url,
            rating, rating_count, first_release_date,
            genres.name,
            themes.name,
            platforms.name,
            tags,
            videos.name, videos.video_id,
            involved_companies.company.name, involved_companies.developer,
            screenshots.url,
            similar_games;
            """;

    private static final String GAME_SEARCH_FIELDS = """
            fields id, name, cover.url, first_release_date;
            """;

    public List<IgdbGameDto> search(GameSearchRequest req) {
        String body = buildQuery(req);
        log.debug("IGDB query:\n{}", body);

        List<IgdbGameDto> results = exchange(
                "/games",
                body,
                new ParameterizedTypeReference<>() {}
        );

        if (results == null || results.isEmpty()) {
            return List.of();
        }

        // --- Local Sorting for Text Searches ---
        // Because IGDB prohibits using 'sort' alongside 'search', we let IGDB fetch
        // the most relevant matches (which handles acronyms like 're9'), and then
        // we apply your custom sorting manually in Java before returning to Flutter.
        if (req.query() != null && !req.query().isBlank()) {
            // exchange() returns an unmodifiable list, so we copy it first
            results = new ArrayList<>(results);
            sortResultsLocally(results, req.effectiveSortBy(), req.effectiveSortDir());
        }

        return results;
    }

    private String buildQuery(GameSearchRequest req) {
        StringBuilder sb = new StringBuilder();

        // Fields — always the same lightweight set for search results
        sb.append(GAME_SEARCH_FIELDS);

        // WHERE clause — built conditionally
        List<String> conditions = new ArrayList<>();

        if (req.igdbIds() != null && !req.igdbIds().isEmpty()) {
            String ids = req.igdbIds().stream()
                    .map(String::valueOf)
                    .collect(Collectors.joining(","));
            conditions.add("id = (" + ids + ")");
        }

        if (req.genreIds() != null && !req.genreIds().isEmpty()) {
            String ids = req.genreIds().stream()
                    .map(String::valueOf)
                    .collect(Collectors.joining(","));
            conditions.add("genres = (" + ids + ")");
        }

        if (req.platformIds() != null && !req.platformIds().isEmpty()) {
            String ids = req.platformIds().stream()
                    .map(String::valueOf)
                    .collect(Collectors.joining(","));
            conditions.add("platforms = (" + ids + ")");
        }

        if (req.releasedAfter() != null) {
            long timestamp = LocalDate.of(req.releasedAfter(), 1, 1)
                    .atStartOfDay(ZoneOffset.UTC).toEpochSecond();
            conditions.add("first_release_date >= " + timestamp);
        }

        if (req.releasedBefore() != null) {
            long timestamp = LocalDate.of(req.releasedBefore(), 12, 31)
                    .atStartOfDay(ZoneOffset.UTC).toEpochSecond();
            conditions.add("first_release_date <= " + timestamp);
        }

        if (req.minRating() != null) {
            conditions.add("rating >= " + req.minRating().intValue());
            conditions.add("rating_count > 0"); // exclude unrated games
        }

        // version_parent = null filters out regional editions cluttering results
        conditions.add("version_parent = null");

        // Append WHERE clauses
        if (!conditions.isEmpty()) {
            sb.append("where ")
                    .append(String.join(" & ", conditions))
                    .append(";\n");
        }

        // --- SEARCH vs SORT LOGIC ---
        if (req.query() != null && !req.query().isBlank()) {
            // If text is present, use IGDB's native search so 're9' works perfectly.
            // DO NOT append the sort clause here. (Avoids the EOL error).
            sb.append("search \"")
                    .append(req.query().trim().replace("\"", ""))
                    .append("\";\n");
        } else {
            // No text search? We can safely use IGDB's native sorting!
            sb.append("sort ")
                    .append(req.effectiveSortBy())
                    .append(" ")
                    .append(req.effectiveSortDir())
                    .append(";\n");
        }

        sb.append("limit ").append(req.effectiveLimit()).append(";\n");
        sb.append("offset ").append(req.effectiveOffset()).append(";\n");

        return sb.toString();
    }

    /**
     * Sorts the results in-memory. Guarantees that null values always sit at
     * the bottom of the list, preventing NullPointerExceptions.
     */
    private void sortResultsLocally(List<IgdbGameDto> results, String sortBy, String sortDir) {
        boolean isDesc = "desc".equalsIgnoreCase(sortDir);

        results.sort((g1, g2) -> {
            Object v1, v2;
            switch (sortBy) {
                case "first_release_date" -> { v1 = g1.firstReleaseDate(); v2 = g2.firstReleaseDate(); }
                case "rating"             -> { v1 = g1.rating();           v2 = g2.rating(); }
                case "name"               -> { v1 = g1.name();             v2 = g2.name(); }
                default                   -> { v1 = g1.ratingCount();      v2 = g2.ratingCount(); }
            }

            // Always push nulls to the very end of the list
            if (v1 == null && v2 == null) return 0;
            if (v1 == null) return 1;
            if (v2 == null) return -1;

            int cmp;
            if (v1 instanceof String s1 && v2 instanceof String s2) {
                cmp = s1.compareToIgnoreCase(s2);
            } else {
                @SuppressWarnings("unchecked")
                Comparable<Object> c1 = (Comparable<Object>) v1;
                cmp = c1.compareTo(v2);
            }

            // Reverse the comparison integer if descending
            return isDesc ? -cmp : cmp;
        });
    }

    public Optional<IgdbGameDto> fetchById(Long igdbId) {
        String body = GAME_FIELDS + String.format("where id = %d; limit 1;", igdbId);

        log.debug("IGDB fetchById: {}", igdbId);

        List<IgdbGameDto> results = exchange(
                "/games",
                body,
                new ParameterizedTypeReference<>() {}
        );

        return (results == null || results.isEmpty())
                ? Optional.empty()
                : Optional.of(results.get(0));
    }

    public List<IgdbGameDto> fetchTopUpcomingGames(int limit, int offset) {
        String body = GAME_SEARCH_FIELDS + String.format("""
            where first_release_date > %d
            & version_parent = null
            & parent_game = null;
            sort hypes desc;
            limit %d;
            offset %d;
            """, System.currentTimeMillis() / 1000, limit, offset);

        log.debug("IGDB fetchTopUpcomingGames");

        List<IgdbGameDto> results = exchange(
                "/games",
                body,
                new ParameterizedTypeReference<>() {}
        );

        return results == null ? List.of() : results;
    }

    @Deprecated
    public List<IgdbGameDto> fetchPopularGames() {

        String popularityQuery = """
            fields game_id, value;
            where popularity_type = 3 & value != null;
            sort value desc;
            limit 10;
            """;

        log.debug("IGDB fetchPopularGames");

        // Step 1: Fetch popularity primitives
        List<IgdbPopularityDto> popularGames = exchange(
                "/popularity_primitives",
                popularityQuery,
                new ParameterizedTypeReference<>() {}
        );

        if (popularGames == null || popularGames.isEmpty()) {
            return List.of();
        }

        // Extract game IDs
        String ids = popularGames.stream()
                .map(p -> p.getGameId().toString())
                .collect(Collectors.joining(","));

        // Step 2: Fetch actual game objects
        String gamesQuery = GAME_FIELDS + String.format("""
            where id = (%s)
            & version_parent = null
            & parent_game = null;
            limit 10;
            """, ids);

        List<IgdbGameDto> games = exchange(
                "/games",
                gamesQuery,
                new ParameterizedTypeReference<>() {}
        );

        return games == null ? List.of() : games;
    }


    private static final int TRENDING_WINDOW_DAYS = 90;

    // Result name keys used in multi-query response
    private static final String TYPE_VISITS       = "Visits";
    private static final String TYPE_WANT_TO_PLAY = "WantToPlay";
    private static final String TYPE_PLAYING      = "Playing";
    private static final String TYPE_PLAYED       = "Played";

    /**
     * Fetch exactly `limit` trending games using 2 HTTP calls:
     *
     *  Call 1 — /multiquery: fetch all 4 popularity primitive types in one shot.
     *  Call 2 — /games: fetch metadata for top scored games, filtered to
     *            released <= now AND released >= 90 days ago.
     *
     * Recency is enforced in Call 2, not Call 1. This guarantees we always
     * return `limit` games as long as enough were released in the window.
     * If the 90-day window can't fill `limit`, we widen automatically.
     */
    public List<IgdbGameDto> fetchTrendingGames(int limit) {
        long now = Instant.now().getEpochSecond();

        // ── Call 1: all 4 primitives in a single multiquery request ──────────
        // We fetch 500 per type — enough to ensure recent games are included
        // even though all-time giants (GTA V, etc.) sit at the very top.
        String multiQueryBody = buildPopularityMultiQuery(500);
        Map<String, List<IgdbPopularityDto>> primitivesByType = exchangeMultiQuery(multiQueryBody);

        if (primitivesByType.isEmpty()) return List.of();

        // ── Build composite score map: gameId → weighted score ───────────────
        Map<Integer, Double> weights = Map.of(
                1, 0.25,   // Visits
                2, 0.15,   // Want to Play
                3, 0.50,   // Playing
                4, 0.10    // Played
        );
        Map<String, Integer> nameToType = Map.of(
                TYPE_VISITS,       1,
                TYPE_WANT_TO_PLAY, 2,
                TYPE_PLAYING,      3,
                TYPE_PLAYED,       4
        );

        Map<Long, Double> compositeScores = new HashMap<>();
        for (Map.Entry<String, List<IgdbPopularityDto>> entry : primitivesByType.entrySet()) {
            Integer type = nameToType.get(entry.getKey());
            if (type == null) continue;

            double weight = weights.get(type);
            List<IgdbPopularityDto> primitives = entry.getValue();

            double maxValue = primitives.stream()
                    .mapToDouble(IgdbPopularityDto::getValue)
                    .max().orElse(1.0);

            for (IgdbPopularityDto p : primitives) {
                double normalised = maxValue > 0 ? p.getValue() / maxValue : 0.0;
                compositeScores.merge(p.getGameId(), weight * normalised, Double::sum);
            }
        }

        // ── Call 2: fetch games with recency gate, widen window if needed ─────
        // We pass ALL scored IDs and let the recency filter do the culling.
        // This is the key change: we're not pre-limiting by score rank first,
        // so the DB can return as many recent games as exist in the pool.
        List<Long> allScoredIds = compositeScores.entrySet().stream()
                .sorted(Map.Entry.<Long, Double>comparingByValue().reversed())
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());

        // Try 90 days first, widen to 180 → 365 if we can't fill `limit`
        int[] windowOptions = {TRENDING_WINDOW_DAYS, 180, 365};
        List<IgdbGameDto> results = List.of();

        for (int window : windowOptions) {
            long recencyCutoff = Instant.now().minus(window, ChronoUnit.DAYS).getEpochSecond();
            results = fetchGamesWithRecencyGate(allScoredIds, compositeScores, recencyCutoff, now, limit);
            if (results.size() >= limit) break;
            log.info("Trending: only {} results in {}-day window, widening...", results.size(), window);
        }

        return results;
    }

    /**
     * Build the multi-query body for all 4 popularity primitive types.
     * Single POST to /multiquery — counts as 1 request against rate limit.
     */
    private String buildPopularityMultiQuery(int perTypeLimit) {
        return String.format("""
            query popularity_primitives "%s" {
              fields game_id, value;
              where popularity_type = 1 & value != null;
              sort value desc;
              limit %d;
            };
            query popularity_primitives "%s" {
              fields game_id, value;
              where popularity_type = 2 & value != null;
              sort value desc;
              limit %d;
            };
            query popularity_primitives "%s" {
              fields game_id, value;
              where popularity_type = 3 & value != null;
              sort value desc;
              limit %d;
            };
            query popularity_primitives "%s" {
              fields game_id, value;
              where popularity_type = 4 & value != null;
              sort value desc;
              limit %d;
            };
            """,
                TYPE_VISITS,       perTypeLimit,
                TYPE_WANT_TO_PLAY, perTypeLimit,
                TYPE_PLAYING,      perTypeLimit,
                TYPE_PLAYED,       perTypeLimit
        );
    }

    /**
     * POST to /multiquery and parse the response envelope.
     * Response shape: [ { "name": "Playing", "result": [ {...}, ... ] }, ... ]
     */
    private Map<String, List<IgdbPopularityDto>> exchangeMultiQuery(String body) {
        HttpHeaders headers = buildHeaders();
        HttpEntity<String> entity = new HttpEntity<>(body, headers);

        try {
            ResponseEntity<List<IgdbMultiQueryResult<IgdbPopularityDto>>> response =
                    restTemplate.exchange(
                            igdbProperties.getBaseUrl() + "/multiquery",
                            HttpMethod.POST,
                            entity,
                            new ParameterizedTypeReference<>() {}
                    );

            List<IgdbMultiQueryResult<IgdbPopularityDto>> results = response.getBody();
            if (results == null) return Map.of();

            Map<String, List<IgdbPopularityDto>> byName = new LinkedHashMap<>();
            for (IgdbMultiQueryResult<IgdbPopularityDto> r : results) {
                if (r.getName() != null && r.getResult() != null) {
                    byName.put(r.getName(), r.getResult());
                }
            }
            return byName;

        } catch (HttpClientErrorException e) {
            log.error("IGDB multiquery error {}: {}", e.getStatusCode(), e.getResponseBodyAsString());
            throw new RuntimeException("IGDB multiquery failed: " + e.getStatusCode(), e);
        }
    }

    /**
     * Fetch game metadata for the given IDs, filtered to the recency window,
     * then re-rank by composite score and return top `limit`.
     */
    private List<IgdbGameDto> fetchGamesWithRecencyGate(
            List<Long> allScoredIds,
            Map<Long, Double> compositeScores,
            long recencyCutoff,
            long now,
            int limit) {

        // IGDB's where clause has a practical ID list limit around 500
        // Pass only as many as needed — top scored are already first
        int batchSize = Math.min(allScoredIds.size(), 500);
        String ids = allScoredIds.stream()
                .limit(batchSize)
                .map(String::valueOf)
                .collect(Collectors.joining(","));

        String gamesQuery = GAME_FIELDS + String.format("""
            where id = (%s)
            & version_parent = null
            & parent_game = null
            & first_release_date >= %d
            & first_release_date <= %d;
            limit %d;
            """, ids, recencyCutoff, now, limit);

        List<IgdbGameDto> games = exchange(
                "/games",
                gamesQuery,
                new ParameterizedTypeReference<>() {}
        );

        if (games == null || games.isEmpty()) return List.of();

        games.sort(Comparator.comparingDouble(
                (IgdbGameDto g) -> compositeScores.getOrDefault(g.id(), 0.0)
        ).reversed());

        return games.stream().limit(limit).collect(Collectors.toList());
    }

    // ── private ─────────────────────────────────────────────────────────────

    private <T> T exchange(String path, String body,
                           ParameterizedTypeReference<T> responseType) {
        HttpHeaders headers = buildHeaders();
        HttpEntity<String> entity = new HttpEntity<>(body, headers);

        try {
            ResponseEntity<T> response = restTemplate.exchange(
                    igdbProperties.getBaseUrl() + path,
                    HttpMethod.POST,
                    entity,
                    responseType
            );
            return response.getBody();
        } catch (HttpClientErrorException.Unauthorized e) {
            log.warn("IGDB returned 401 — token may be stale");
            throw new RuntimeException("IGDB authentication failed. Check your credentials.", e);
        } catch (HttpClientErrorException e) {
            log.error("IGDB API error {}: {}", e.getStatusCode(), e.getResponseBodyAsString());
            throw new RuntimeException("IGDB API error: " + e.getStatusCode(), e);
        }
    }

    private HttpHeaders buildHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.TEXT_PLAIN);
        headers.set("Client-ID",     igdbProperties.getClientId());
        headers.set("Authorization", "Bearer " + igdbAuthService.getAccessToken());
        return headers;
    }
}