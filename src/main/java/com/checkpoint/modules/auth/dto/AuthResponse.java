// AuthResponse.java — returned by register, login, and refresh
package com.checkpoint.modules.auth.dto;

import java.util.UUID;

public record AuthResponse(
        String accessToken,
        String refreshToken,
        UserInfo user
) {
    public record UserInfo(UUID id, String username, String email) {}
}