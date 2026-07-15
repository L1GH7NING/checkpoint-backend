package com.checkpoint.common;

import org.springframework.data.domain.Page;
import java.util.List;

/**
 * Wraps Spring's Page<T> into a flat, JSON-friendly structure.
 * Avoids sending Spring's internal page metadata to the client.
 */
public record PageResponse<T>(
        List<T> content,
        int page,
        int size,
        long totalElements,
        int totalPages,
        boolean last
) {
    public static <T> PageResponse<T> of(Page<T> page) {
        return new PageResponse<>(
                page.getContent(),
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages(),
                page.isLast()
        );
    }
}