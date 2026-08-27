package com.deltahomes.backend.dto.common;

import com.fasterxml.jackson.annotation.JsonInclude;
import org.springframework.data.domain.Page;

import java.time.OffsetDateTime;
import java.util.List;

/**
 * The single unified response envelope for EVERY successful endpoint in the system.
 *
 * <pre>{@code
 * // single resource
 * { "success": true, "data": {...}, "message": null }
 *
 * // lists (pagination metadata attached)
 * { "success": true,
 *   "data": [ ... ],
 *   "pagination": { "page": 0, "size": 20, "totalElements": 42,
 *                   "totalPages": 3, "hasNext": true } }
 *
 * // action acknowledgement
 * { "success": true, "data": null, "message": "Logged out" }
 * }</pre>
 *
 * Errors use a mirrored shape produced by {@code GlobalExceptionHandler}:
 * {@code { "success": false, "status": ..., "error": "...", "fields": {...}, "timestamp": ... }}.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record ApiResponse<T>(
        boolean success,
        T data,
        String message,
        PageMeta pagination,
        OffsetDateTime timestamp
) {

    /** Pagination metadata block — present only on list endpoints. */
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record PageMeta(
            int page,
            int size,
            long totalElements,
            int totalPages,
            boolean hasNext
    ) {
        public static PageMeta from(Page<?> page) {
            return new PageMeta(
                    page.getNumber(),
                    page.getSize(),
                    page.getTotalElements(),
                    page.getTotalPages(),
                    page.hasNext());
        }
    }

    // ---------- Factories ----------

    /** Single-resource success (also used for 201 CREATED bodies). */
    public static <T> ApiResponse<T> ok(T data) {
        return new ApiResponse<>(true, data, null, null, OffsetDateTime.now());
    }

    /** Success with a human-readable message (acknowledgements, state changes). */
    public static <T> ApiResponse<T> ok(T data, String message) {
        return new ApiResponse<>(true, data, message, null, OffsetDateTime.now());
    }

    /** Action acknowledgement without payload (e.g. logout, password changed). */
    public static ApiResponse<Void> message(String message) {
        return new ApiResponse<>(true, null, message, null, OffsetDateTime.now());
    }

    /** List endpoint success — wraps any Spring Data page. */
    public static <T> ApiResponse<List<T>> page(org.springframework.data.domain.Page<T> page) {
        return new ApiResponse<>(true, page.getContent(), null, PageMeta.from(page), OffsetDateTime.now());
    }

    /** List endpoint success when the service already built a {@link PaginatedResponse}. */
    public static <T> ApiResponse<List<T>> page(PaginatedResponse<T> page) {
        return new ApiResponse<>(
                true,
                page.content(),
                null,
                new PageMeta(page.page(), page.size(), page.totalElements(), page.totalPages(), page.hasNext()),
                OffsetDateTime.now());
    }

    /** List endpoint success with an explicit item collection (non-paged arrays). */
    public static <T> ApiResponse<List<T>> list(List<T> items) {
        return new ApiResponse<>(true, items, null, null, OffsetDateTime.now());
    }
}
