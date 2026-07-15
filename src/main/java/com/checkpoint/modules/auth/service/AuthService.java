package com.checkpoint.modules.auth.service;

import com.checkpoint.modules.auth.dto.*;

public interface AuthService {
    AuthResponse register(RegisterRequest request);
    AuthResponse login(LoginRequest request);
    AuthResponse refresh(RefreshRequest request);
    void logout(String rawRefreshToken);
}