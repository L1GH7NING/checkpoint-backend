package com.checkpoint.modules.user.service;

import com.checkpoint.exception.AppException;
import com.checkpoint.modules.auth.service.AuthService;
import com.checkpoint.modules.user.dto.DeleteAccountRequest;
import com.checkpoint.modules.user.dto.UpdateProfileRequest;
import com.checkpoint.modules.user.dto.UserResponse;
import com.checkpoint.modules.user.entity.User;
import com.checkpoint.modules.user.exception.UserNotFoundException;
import com.checkpoint.modules.user.mapper.UserMapper;
import com.checkpoint.modules.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

// UserServiceImpl — needs PasswordEncoder + AuthService injected now
@Service
@Transactional
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final AuthService authService;

    @Override
    @Transactional(readOnly = true)
    public UserResponse getById(UUID userId) {
        User user = userRepository.findById(userId)
                .filter(u -> u.getDeletedAt() == null)
                .orElseThrow(() -> new UserNotFoundException(userId));
        return userMapper.toResponse(user);
    }

    @Override
    public UserResponse updateProfile(UUID userId, UpdateProfileRequest req) {
        User user = userRepository.findById(userId)
                .filter(u -> u.getDeletedAt() == null)
                .orElseThrow(() -> new UserNotFoundException(userId));

        if (req.displayName() != null) {
            user.setUsername(req.displayName());
        }
        if (req.bio() != null) {
            user.setBio(req.bio());
        }
        if (req.isPrivate() != null) {
            user.setIsPrivate(req.isPrivate());
        }

        User saved = userRepository.save(user);
        return userMapper.toResponse(saved);
    }

    @Override
    public void deleteAccount(UUID userId, DeleteAccountRequest req) {
        User user = userRepository.findById(userId)
                .filter(u -> u.getDeletedAt() == null)
                .orElseThrow(() -> new UserNotFoundException(userId));

        if (!passwordEncoder.matches(req.password(), user.getPasswordHash())) {
            throw new AppException(HttpStatus.UNAUTHORIZED,
                    "INVALID_CREDENTIALS", "Password is incorrect");
        }

        user.setDeletedAt(LocalDateTime.now());

        // 1. Generate a short unique suffix, e.g., "_del_a1b2c3d4" (13 chars)
        String suffix = "_del_" + UUID.randomUUID().toString().substring(0, 8);

        // 2. Mangle Email (usually safe to just append since default length is 255)
        user.setEmail(user.getEmail() + suffix);

        // 3. Mangle Username (safeguard against the 30 character limit)
        String oldUsername = user.getUsername();
        int maxOriginalLength = 30 - suffix.length();

        if (oldUsername.length() > maxOriginalLength) {
            // Truncate the original username so the suffix fits within 30 chars
            oldUsername = oldUsername.substring(0, maxOriginalLength);
        }
        user.setUsername(oldUsername + suffix);

        userRepository.save(user);

        // Kill every active session immediately
        authService.revokeAllTokensForUser(userId);
    }
}