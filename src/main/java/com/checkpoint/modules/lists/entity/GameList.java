package com.checkpoint.modules.lists.entity;

import com.checkpoint.common.BaseEntity;
import com.checkpoint.modules.user.entity.User;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(
        name = "lists",
        indexes = {
                @Index(name = "idx_lists_user_id", columnList = "user_id"),
                @Index(name = "idx_lists_user_visibility", columnList = "user_id, visibility")
        }
)
@Getter
@Setter
@NoArgsConstructor
public class GameList extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false, length = 150)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ListVisibility visibility = ListVisibility.PUBLIC;
}