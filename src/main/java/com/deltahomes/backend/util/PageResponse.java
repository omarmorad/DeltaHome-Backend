package com.deltahomes.backend.util;

import org.springframework.data.domain.Page;

import java.util.List;

/**
 * Pagination response wrapper matching the exact format specified in 00-master.md §5.2.
 */
public record PageResponse<T>(
        List<T> content,
        int page,
        int size,
        long totalElements,
        int totalPages,
        boolean hasNext
) {
    public static <T> PageResponse<T> from(Page<T> page) {
        return new PageResponse<>(
                page.getContent(),
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages(),
                page.hasNext()
        );
    }
}