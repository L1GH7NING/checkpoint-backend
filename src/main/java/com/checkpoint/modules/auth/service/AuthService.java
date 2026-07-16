package com.checkpoint.modules.auth.service;

import com.checkpoint.modules.auth.dto.*;

import java.util.UUID;

public interface AuthService {
    AuthResponse register(RegisterRequest request);
    AuthResponse login(LoginRequest request);
    AuthResponse refresh(RefreshRequest request);
    void logout(String rawRefreshToken);
    // modules/auth/service/AuthService.java (interface)
    void changePassword(UUID userId, ChangePasswordRequest req);
    void revokeAllTokensForUser(UUID userId);
}