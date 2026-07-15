package com.checkpoint.modules.game.client.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

// Raw Twitch token response
public record IgdbTokenResponse(
        @JsonProperty("access_token") String accessToken,
        @JsonProperty("expires_in")   long expiresIn,       // seconds
        @JsonProperty("token_type")   String tokenType
) {}