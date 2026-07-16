package com.checkpoint.modules.auth.service;

import com.checkpoint.exception.AppException;
import com.checkpoint.modules.auth.dto.*;
import com.checkpoint.modules.auth.entity.*;
import com.checkpoint.modules.auth.repository.*;
import com.checkpoint.modules.user.entity.User;
import com.checkpoint.modules.user.repository.UserRepository;
import com.checkpoint.security.JwtProperties;
import com.checkpoint.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.UUID;

@Service
@Transactional
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final JwtProperties jwtProperties;   // for refresh TTL calculation

    @Override
    public AuthResponse register(RegisterRequest req) {
        if (userRepository.existsByEmail(req.email())) {
            throw new AppException(HttpStatus.CONFLICT, "EMAIL_TAKEN",
                    "An account with this email already exists");
        }
        if (userRepository.existsByUsername(req.username())) {
            throw new AppException(HttpStatus.CONFLICT, "USERNAME_TAKEN",
                    "This username is already taken");
        }

        User user = new User();
        user.setUsername(req.username());
        user.setEmail(req.email());
        user.setPasswordHash(passwordEncoder.encode(req.password()));
        user.setDisplayName(req.username()); // sensible default
        User savedUser = userRepository.save(user);

        System.out.println("Saved user id = " + savedUser.getId());

        return issueTokenPair(savedUser);
    }

    @Override
    public AuthResponse login(LoginRequest req) {
        User user = userRepository.findByEmailAndDeletedAtIsNull(req.email())
                .orElseThrow(() -> new AppException(HttpStatus.UNAUTHORIZED,
                        "INVALID_CREDENTIALS", "User does not exist"));

        if (!passwordEncoder.matches(req.password(), user.getPasswordHash())) {
            throw new AppException(HttpStatus.UNAUTHORIZED,
                    "INVALID_CREDENTIALS", "Invalid password");
        }

        return issueTokenPair(user);
    }

    @Override
    public AuthResponse refresh(RefreshRequest req) {
        String tokenHash = hash(req.refreshToken());

        RefreshToken stored = refreshTokenRepository
                .findByTokenHashAndRevokedFalse(tokenHash)
                .orElseThrow(() -> new AppException(HttpStatus.UNAUTHORIZED,
                        "INVALID_REFRESH_TOKEN", "Refresh token is invalid or expired"));

        if (stored.getExpiresAt().isBefore(LocalDateTime.now())) {
            stored.setRevoked(true);
            throw new AppException(HttpStatus.UNAUTHORIZED,
                    "REFRESH_TOKEN_EXPIRED", "Refresh token has expired, please log in again");
        }

        // One-time-use: revoke the old token before issuing a new pair
        stored.setRevoked(true);

        return issueTokenPair(stored.getUser());
    }

    @Override
    public void logout(String rawRefreshToken) {
        // Best-effort: if token not found, we still treat it as a successful logout
        String tokenHash = hash(rawRefreshToken);
        refreshTokenRepository.findByTokenHashAndRevokedFalse(tokenHash)
                .ifPresent(rt -> {
                    rt.setRevoked(true);
                    // Optionally: revokeAllByUserId to log out all devices
                });
    }

    @Override
    public void changePassword(UUID userId, ChangePasswordRequest req) {
        User user = userRepository.findById(userId)
                .filter(u -> u.getDeletedAt() == null)
                .orElseThrow(() -> new AppException(HttpStatus.NOT_FOUND,
                        "USER_NOT_FOUND", "User not found"));

        if (!passwordEncoder.matches(req.currentPassword(), user.getPasswordHash())) {
            throw new AppException(HttpStatus.UNAUTHORIZED,
                    "INVALID_CREDENTIALS", "Current password is incorrect");
        }

        user.setPasswordHash(passwordEncoder.encode(req.newPassword()));
        userRepository.save(user);

        // Log out every other session — force re-login with the new password
        revokeAllTokensForUser(userId);
    }

    @Override
    public void revokeAllTokensForUser(UUID userId) {
        refreshTokenRepository.revokeAllByUserId(userId);
    }

    // --- private helpers ---

    private AuthResponse issueTokenPair(User user) {
        String accessToken  = jwtUtil.generateAccessToken(user.getId(), user.getUsername());
        String refreshToken = jwtUtil.generateRefreshToken(user.getId());

        // Persist hashed refresh token
        RefreshToken rt = new RefreshToken();
        rt.setUser(user);
        rt.setTokenHash(hash(refreshToken));
        rt.setExpiresAt(LocalDateTime.now().plusSeconds(
                jwtProperties.getRefreshExpiryMs() / 1000));
        refreshTokenRepository.save(rt);

        return new AuthResponse(
                accessToken,
                refreshToken,
                new AuthResponse.UserInfo(user.getId(), user.getUsername(), user.getEmail())
        );
    }

    // SHA-256 hash of the raw token string — we never store the plaintext
    private String hash(String raw) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] bytes = digest.digest(raw.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(bytes);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 not available", e); // never happens on JVM
        }
    }
}