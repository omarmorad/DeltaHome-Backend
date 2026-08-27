package com.deltahomes.backend.util;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.util.List;

public final class PageUtils {

    /** Upper bound for client-requested page sizes to prevent unbounded result sets. */
    public static final int MAX_PAGE_SIZE = 100;

    private PageUtils() {
    }

    /**
     * Clamps the page number (>= 0) and page size (1..{@value MAX_PAGE_SIZE}).
     */
    public static Pageable clamp(Pageable pageable) {
        int page = Math.max(pageable.getPageNumber(), 0);
        int size = Math.min(Math.max(pageable.getPageSize(), 1), MAX_PAGE_SIZE);
        if (page == pageable.getPageNumber() && size == pageable.getPageSize()) {
            return pageable;
        }
        return PageRequest.of(page, size, pageable.getSort());
    }

    /**
     * Normalizes a Spring {@link Pageable} for use with NATIVE queries, where
     * the sort property is appended to SQL verbatim. Camel-case Java property
     * names (createdAt) are converted to snake-case column names (created_at).
     * Also clamps page number/size (see {@link #clamp(Pageable)}).
     */
    public static Pageable normalizeSort(Pageable pageable) {
        return normalizeSortRaw(clamp(pageable));
    }

    private static Pageable normalizeSortRaw(Pageable pageable) {
        if (pageable.getSort().isUnsorted()) {
            return pageable;
        }
        List<Sort.Order> orders = pageable.getSort().stream()
                .map(order -> new Sort.Order(
                        order.getDirection(),
                        toSnakeCase(order.getProperty()),
                        order.getNullHandling()))
                .toList();
        return PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), Sort.by(orders));
    }

    private static String toSnakeCase(String property) {
        return property.replaceAll("([a-z0-9])([A-Z])", "$1_$2").toLowerCase();
    }
}
