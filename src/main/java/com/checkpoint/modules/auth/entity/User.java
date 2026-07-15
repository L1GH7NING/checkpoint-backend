package com.checkpoint.modules.auth.entity;

import com.checkpoint.common.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
public class User extends BaseEntity {

//    @Id
//    @GeneratedValue(strategy = GenerationType.UUID)
//    @Column(updatable = false, nullable = false)
//    private UUID id;

    @Column(nullable = false, unique = true, length = 30)
    private String username;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(name = "password_hash", nullable = false)
    private String passwordHash;

    @Column(name = "display_name", length = 100)
    private String displayName;

    @Column(columnDefinition = "TEXT")
    private String bio;

    @Column(name = "avatar_url", length = 500)
    private String avatarUrl;

    @Column(name = "is_private")
    private boolean isPrivate = false;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;  // soft delete
}