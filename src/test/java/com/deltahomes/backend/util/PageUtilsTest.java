package com.deltahomes.backend.util;

import org.junit.jupiter.api.Test;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PageUtilsTest {

    @Test
    void clampsOversizedPageRequests() {
        Pageable clamped = PageUtils.clamp(PageRequest.of(0, 100_000));
        assertEquals(PageUtils.MAX_PAGE_SIZE, clamped.getPageSize());
    }

    // Note: Spring's PageRequest.of rejects negative pages / zero sizes at
    // construction time, so PageUtils only needs to guard against oversized sizes.

    @Test
    void normalizeSortConvertsCamelCaseToSnakeCase() {
        Pageable pageable = PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "createdAt"));
        Sort.Order order = PageUtils.normalizeSort(pageable).getSort().iterator().next();
        assertEquals("created_at", order.getProperty());
        assertEquals(Sort.Direction.DESC, order.getDirection());
    }

    @Test
    void normalizeSortAlsoClampsSize() {
        Pageable normalized = PageUtils.normalizeSort(
                PageRequest.of(0, 50_000, Sort.by("updatedAt")));
        assertEquals(PageUtils.MAX_PAGE_SIZE, normalized.getPageSize());
        assertEquals("updated_at", normalized.getSort().iterator().next().getProperty());
    }
}
