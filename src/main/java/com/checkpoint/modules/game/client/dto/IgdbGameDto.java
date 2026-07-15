package com.checkpoint.modules.game.client.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import java.util.Optional;

public record IgdbGameDto(
        Long id,
        String name,                                          // always present
        String summary,                                       // detail only
        String storyline,                                     // detail only
        Cover cover,
        List<Video> videos,
        String url,
        Double rating,                                        // detail only
        @JsonProperty("rating_count")      Integer ratingCount,
        @JsonProperty("first_release_date") Long firstReleaseDate, // present in both
        List<Genre> genres,
        List<Theme> themes,
        List<Integer> tags,// detail only
        List<Platform> platforms,                             // detail only
        @JsonProperty("involved_companies") List<InvolvedCompany> involvedCompanies,
        List<Screenshot> screenshots,                         // detail only
        @JsonProperty("similar_games")     List<Long> similarGames
) {
    public record Cover(String url) {}
    public record Genre(Long id, String name) {}
    public record Theme(Long id, String name) {}
    public record Platform(String name) {}
    public record InvolvedCompany(
            @JsonProperty("company") Company company,
            boolean developer
    ) {
        public record Company(String name) {}
    }
    @JsonProperty("video_url")
    public String videoUrl() {

        if (videos == null || videos.isEmpty()) {
            return null;
        }

        // Prefer trailer-like videos
        Optional<Video> trailer = videos.stream()
                .filter(v -> v.name() != null)
                .filter(v -> {
                    String name = v.name().toLowerCase();
                    return name.contains("trailer");
                })
                .findFirst();

        Video selected = trailer.orElse(videos.getFirst());

        return selected.videoId() == null
                ? null
                : "https://www.youtube.com/watch?v=" + selected.videoId();
    }
    public record Video(
            String name,
            @JsonProperty("video_id")
            String videoId
    ) {}
    public record Screenshot(String url) {}
}