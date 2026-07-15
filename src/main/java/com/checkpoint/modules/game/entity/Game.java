package com.checkpoint.modules.game.entity;

import com.checkpoint.common.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "games")
@Getter
@Setter
@NoArgsConstructor
public class Game extends BaseEntity {

//    @Id
//    @GeneratedValue
//    @Column(columnDefinition = "uuid", updatable = false)
//    private UUID id;

    @Column(name = "igdb_id", unique = true, nullable = false)
    private Long igdbId;

    @Column(nullable = false, length = 500)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String summary;

    @Column(columnDefinition = "TEXT")
    private String storyline;

    @Column(name = "cover_url", length = 500)
    private String coverUrl;

    @Column(precision = 6, scale = 2)
    private BigDecimal rating;

    @Column(name = "rating_count")
    private Integer ratingCount;

    @Column(name = "first_release_date")
    private Long firstReleaseDate;     // Unix timestamp from IGDB

    @Column(columnDefinition = "TEXT[]")
    private String[] genres;

    @Column(columnDefinition = "TEXT[]")
    private String[] platforms;

    @Column(columnDefinition = "TEXT[]")
    private String[] developers;

    @Column(columnDefinition = "TEXT[]")
    private String[] screenshots;

    @Column(name = "trailer_url", length = 500)
    private String trailerUrl;

    @Column(name = "igdb_url", length = 500)
    private String igdbUrl;

    @Column(name = "similar_igdb_ids", columnDefinition = "BIGINT[]")
    private Long[] similarIgdbIds;

    @Column(name = "last_synced_at")
    private LocalDateTime lastSyncedAt;
}