package com.checkpoint.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "igdb")
public class IgdbProperties {
    private String clientId;
    private String clientSecret;
    private String baseUrl = "https://api.igdb.com/v4";
    private String authUrl = "https://id.twitch.tv/oauth2/token";
}