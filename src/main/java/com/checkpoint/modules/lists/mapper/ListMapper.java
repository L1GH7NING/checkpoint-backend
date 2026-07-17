package com.checkpoint.modules.lists.mapper;

import com.checkpoint.modules.game.dto.GameSearchResponse;
import com.checkpoint.modules.game.mapper.GameMapper;
import com.checkpoint.modules.lists.dto.*;
import com.checkpoint.modules.lists.entity.GameList;
import com.checkpoint.modules.lists.entity.ListItem;
import com.checkpoint.modules.user.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class ListMapper {

    private final GameMapper gameMapper;
    private final UserMapper userMapper;

    public ListItemResponse toItemResponse(ListItem item) {
        return new ListItemResponse(
                item.getId(),
                gameMapper.toSearchResponse(item.getGame()), // <-- reuse existing search mapper method
                item.getPosition(),
                item.getCreatedAt() // mapped from added_at column
        );
    }

    public ListResponse toResponse(GameList list, List<ListItem> items, long gameCount) {
        List<ListItemResponse> itemResponses = items.stream()
                .map(this::toItemResponse)
                .collect(Collectors.toList());

        return new ListResponse(
                list.getId(),
                userMapper.toSummary(list.getUser()),
                list.getTitle(),
                list.getDescription(),
                list.getVisibility(),
                gameCount,
                itemResponses,
                list.getCreatedAt(),
                list.getUpdatedAt()
        );
    }

    public ListSummaryResponse toSummary(GameList list, List<ListItem> previewItems, long gameCount) {
        List<String> covers = previewItems.stream()
                .map(item -> item.getGame().getCoverUrl())
                .filter(java.util.Objects::nonNull)
                .collect(Collectors.toList());

        return new ListSummaryResponse(
                list.getId(),
                list.getTitle(),
                list.getDescription(),
                list.getVisibility(),
                gameCount,
                covers,
                list.getUpdatedAt()
        );
    }
}