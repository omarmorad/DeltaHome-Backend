package com.deltahomes.backend.dto.common;

import org.springframework.data.domain.Page;

import java.util.List;

/**
 * Uniform pagination envelope used by every resource index endpoint.
 */
public record PaginatedResponse<T>(
        List<T> content,
        int page,
        int size,
        long totalElements,
        int totalPages,
        boolean hasNext
) {
    public static <T> PaginatedResponse<T> from(Page<T> page) {
        return new PaginatedResponse<>(
                page.getContent(),
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages(),
                page.hasNext());
    }
}
