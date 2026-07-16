// modules/auth/dto/ChangePasswordRequest.java
package com.checkpoint.modules.auth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record ChangePasswordRequest(
        @NotBlank(message = "Current password is required")
        String currentPassword,

        @NotBlank
        @Pattern(
                regexp = "^(?=.*[A-Z])(?=.*\\d).{8,}$",
                message = "Password must be at least 8 characters, with 1 uppercase letter and 1 number"
        )
        String newPassword
) {}