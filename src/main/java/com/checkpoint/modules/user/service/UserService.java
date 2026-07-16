package com.checkpoint.modules.user.service;

import com.checkpoint.modules.user.dto.DeleteAccountRequest;
import com.checkpoint.modules.user.dto.UpdateProfileRequest;
import com.checkpoint.modules.user.dto.UserResponse;

import java.util.UUID;

public interface UserService {
    UserResponse getById(UUID userId);
    UserResponse updateProfile(UUID userId, UpdateProfileRequest req);
    void deleteAccount(UUID userId, DeleteAccountRequest req);
}