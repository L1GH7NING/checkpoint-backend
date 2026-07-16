package com.checkpoint.modules.user.exception;

import com.checkpoint.exception.AppException;
import org.springframework.http.HttpStatus;

import java.util.UUID;

public class UserNotFoundException extends AppException {
    public UserNotFoundException(UUID userId) {
        super(HttpStatus.NOT_FOUND, "USER_NOT_FOUND", "User not found: " + userId);
    }
}