// modules/user/dto/DeleteAccountRequest.java
package com.checkpoint.modules.user.dto;

import jakarta.validation.constraints.NotBlank;

public record DeleteAccountRequest(
        @NotBlank(message = "Password is required to delete your account")
        String password
) {}