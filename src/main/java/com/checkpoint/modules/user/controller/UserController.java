package com.checkpoint.modules.user.controller;

import com.checkpoint.common.ApiResponse;
import com.checkpoint.modules.user.dto.DeleteAccountRequest;
import com.checkpoint.modules.user.dto.UpdateProfileRequest;
import com.checkpoint.modules.user.dto.UserResponse;
import com.checkpoint.modules.user.service.UserService;
import com.checkpoint.security.UserPrincipal;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

// modules/user/controller/UserController.java
@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/me")
    public ApiResponse<UserResponse> getCurrentUser(
            @AuthenticationPrincipal UserPrincipal principal
    ) {
        return ApiResponse.ok(userService.getById(principal.id()));
    }

    @PatchMapping("/me")
    public ApiResponse<UserResponse> updateProfile(
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody UpdateProfileRequest request
    ) {
        return ApiResponse.ok("Profile updated", userService.updateProfile(principal.id(), request));
    }

    @DeleteMapping("/me")
    public ResponseEntity<ApiResponse<Void>> deleteAccount(
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody DeleteAccountRequest request
    ) {
        userService.deleteAccount(principal.id(), request);
        return ResponseEntity.noContent().build();
    }
}