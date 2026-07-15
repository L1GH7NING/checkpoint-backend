package com.checkpoint.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtUtil {

    private final JwtProperties props;

    private SecretKey signingKey() {
        // Key must be ≥256 bits for HS256 — enforced by jjwt
        return Keys.hmacShaKeyFor(props.getSecret().getBytes(StandardCharsets.UTF_8));
    }

    public String generateAccessToken(UUID userId, String username) {
        return Jwts.builder()
                .subject(userId.toString())
                .claim("username", username)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + props.getAccessExpiryMs()))
                .signWith(signingKey())
                .compact();
    }

    // Refresh token is just a signed JWT — we also store its hash in the DB
    // to allow invalidation (one-time-use via revoked flag)
    public String generateRefreshToken(UUID userId) {
        return Jwts.builder()
                .subject(userId.toString())
                .claim("type", "refresh")
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + props.getRefreshExpiryMs()))
                .signWith(signingKey())
                .compact();
    }

    /**
     * Validates token and throws detailed exception if invalid.
     * @throws JwtException with specific error type
     */
    public void validateAndParseClaims(String token) {
        if (token == null || token.isEmpty()) {
            throw new JwtException(JwtException.ErrorType.TOKEN_MISSING);
        }

        try {
            parseClaims(token);
        } catch (ExpiredJwtException ex) {
            log.debug("Token expired: {}", ex.getMessage());
            throw new JwtException(JwtException.ErrorType.TOKEN_EXPIRED, ex);
        } catch (UnsupportedJwtException ex) {
            log.debug("Token unsupported: {}", ex.getMessage());
            throw new JwtException(JwtException.ErrorType.TOKEN_UNSUPPORTED, ex);
        } catch (MalformedJwtException ex) {
            log.debug("Token malformed: {}", ex.getMessage());
            throw new JwtException(JwtException.ErrorType.TOKEN_MALFORMED, ex);
        } catch (SignatureException ex) {
            log.debug("Token signature invalid: {}", ex.getMessage());
            throw new JwtException(JwtException.ErrorType.TOKEN_INVALID_SIGNATURE, ex);
        } catch (IllegalArgumentException ex) {
            log.debug("Token claims empty: {}", ex.getMessage());
            throw new JwtException(JwtException.ErrorType.TOKEN_INVALID, ex);
        } catch (JwtException ex) {
            log.debug("Token invalid: {}", ex.getMessage());
            throw new JwtException(JwtException.ErrorType.TOKEN_INVALID, ex);
        }
    }

    public boolean isValid(String token) {
        try {
            validateAndParseClaims(token);
            return true;
        } catch (JwtException ex) {
            log.debug("Invalid JWT: {}", ex.getMessage());
            return false;
        }
    }

    public UUID extractUserId(String token) {
        return UUID.fromString(parseClaims(token).getSubject());
    }

    public String extractUsername(String token) {
        return parseClaims(token).get("username", String.class);
    }

    private Claims parseClaims(String token) {
        return Jwts.parser()
                .verifyWith(signingKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}