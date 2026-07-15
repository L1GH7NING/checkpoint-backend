package com.checkpoint.modules.game.client.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

// IgdbMultiQueryResult.java
@Data
public class IgdbMultiQueryResult<T> {
    @JsonProperty("name")
    private String name;

    @JsonProperty("count")
    private Integer count;

    @JsonProperty("result")
    private List<T> result;
}
