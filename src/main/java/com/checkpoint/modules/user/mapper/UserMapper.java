package com.checkpoint.modules.user.mapper;

import com.checkpoint.modules.user.dto.UserResponse;
import com.checkpoint.modules.user.entity.User;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {
    public UserResponse toResponse(User user) {
        return new UserResponse(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getDisplayName(),
                user.getBio(),
                user.getAvatarUrl(),
                user.getIsPrivate()
        );
    }
}