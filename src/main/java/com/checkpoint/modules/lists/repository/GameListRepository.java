package com.checkpoint.modules.lists.repository;

import com.checkpoint.modules.lists.entity.GameList;
import com.checkpoint.modules.lists.entity.ListVisibility;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface GameListRepository extends JpaRepository<GameList, UUID> {

    List<GameList> findByUserIdOrderByUpdatedAtDesc(UUID userId);

    List<GameList> findByUserIdAndVisibilityOrderByUpdatedAtDesc(UUID userId, ListVisibility visibility);
}