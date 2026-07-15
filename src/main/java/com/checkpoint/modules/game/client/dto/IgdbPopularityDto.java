package com.checkpoint.modules.game.client.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class IgdbPopularityDto {

    private Long id;

    @JsonProperty("game_id")
    private Long gameId;

    @JsonProperty("popularity_type")
    private Integer popularityType;

    private Double value;

    @JsonProperty("calculated_at")
    private Long calculatedAt;
}