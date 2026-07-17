package com.checkpoint.modules.lists.entity;

import com.checkpoint.common.BaseEntity;
import com.checkpoint.modules.game.entity.Game;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(
        name = "list_items",
        uniqueConstraints = @UniqueConstraint(
                name = "uq_list_items_list_game",
                columnNames = {"list_id", "game_id"}
        ),
        indexes = {
                @Index(name = "idx_list_items_list_id", columnList = "list_id"),
                @Index(name = "idx_list_items_list_position", columnList = "list_id, position")
        }
)
@Getter
@Setter
@NoArgsConstructor
@AttributeOverride(name = "createdAt", column = @Column(name = "added_at", nullable = false, updatable = false))
public class ListItem extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "list_id", nullable = false)
    private GameList list;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "game_id", nullable = false)
    private Game game;

    @Column(nullable = false)
    private Integer position;
}