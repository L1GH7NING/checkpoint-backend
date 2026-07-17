package com.checkpoint.modules.lists.repository;

import com.checkpoint.modules.lists.entity.ListItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ListItemRepository extends JpaRepository<ListItem, UUID> {

    // Fetch join to avoid N+1 when returning a full list with games
    @Query("""
        SELECT li FROM ListItem li
        JOIN FETCH li.game g
        WHERE li.list.id = :listId
        ORDER BY li.position ASC
        """)
    List<ListItem> findByListIdOrderByPosition(@Param("listId") UUID listId);

    // First few items only — for grid/cover previews on ListSummaryResponse
    @Query("""
        SELECT li FROM ListItem li
        JOIN FETCH li.game g
        WHERE li.list.id = :listId
        ORDER BY li.position ASC
        LIMIT 4
        """)
    List<ListItem> findPreviewItems(@Param("listId") UUID listId);

    boolean existsByListIdAndGameId(UUID listId, UUID gameId);

    Optional<ListItem> findByListIdAndGameId(UUID listId, UUID gameId);

    long countByListId(UUID listId);

    @Query("SELECT COALESCE(MAX(li.position), -1) FROM ListItem li WHERE li.list.id = :listId")
    Integer findMaxPosition(@Param("listId") UUID listId);

    void deleteByListIdAndGameId(UUID listId, UUID gameId);

    void deleteByListId(UUID listId); // used when a list itself is deleted
}