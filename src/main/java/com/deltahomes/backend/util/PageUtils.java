package com.deltahomes.backend.util;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.util.List;

public final class PageUtils {

    private PageUtils() {
    }

    /**
     * Normalizes a Spring {@link Pageable} for use with NATIVE queries, where
     * the sort property is appended to SQL verbatim. Camel-case Java property
     * names (createdAt) are converted to snake-case column names (created_at).
     */
    public static Pageable normalizeSort(Pageable pageable) {
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
