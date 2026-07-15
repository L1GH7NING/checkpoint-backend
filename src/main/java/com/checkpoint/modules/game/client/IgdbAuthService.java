package com.checkpoint.modules.game.client;

import com.checkpoint.config.IgdbProperties;
import com.checkpoint.modules.game.client.dto.IgdbTokenResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.Instant;

@Slf4j
@Service
@RequiredArgsConstructor
public class IgdbAuthService {

    private final IgdbProperties igdbProperties;
    private final RestTemplate restTemplate;

    // In-memory token cache — fine for a single-instance monolith
    private String cachedToken;
    private Instant tokenExpiresAt = Instant.EPOCH;

    // 5-minute buffer: refresh before the token actually expires
    private static final long EXPIRY_BUFFER_SECONDS = 300;

    /**
     * Returns a valid Twitch access token.
     * Fetches a new one only when the current token is missing or about to expire.
     * Thread-safe via synchronized — adequate for a solo-dev monolith.
     */
    public synchronized String getAccessToken() {
        if (cachedToken == null || Instant.now().isAfter(tokenExpiresAt)) {
            log.info("IGDB token expired or missing — fetching new token");
            fetchNewToken();
        }
        return cachedToken;
    }

    private void fetchNewToken() {
        String url = igdbProperties.getAuthUrl()
                + "?client_id="     + igdbProperties.getClientId()
                + "&client_secret=" + igdbProperties.getClientSecret()
                + "&grant_type=client_credentials";

        IgdbTokenResponse response = restTemplate.postForObject(
                url, null, IgdbTokenResponse.class
        );

        if (response == null || response.accessToken() == null) {
            throw new RuntimeException("Failed to obtain IGDB access token — check your Client ID and Secret");
        }

        cachedToken    = response.accessToken();
        tokenExpiresAt = Instant.now().plusSeconds(response.expiresIn() - EXPIRY_BUFFER_SECONDS);

        log.info("IGDB token refreshed. Expires in ~{} hours",
                (response.expiresIn() - EXPIRY_BUFFER_SECONDS) / 3600);
    }
}