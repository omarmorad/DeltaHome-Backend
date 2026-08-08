package com.deltahomes.backend.util;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

/**
 * Pagination parameters matching the specification in 01-foundation.md.
 */
public class PagingParams {
    private int page = 0;
    private int size = 20;
    private String sort = "createdAt,desc";

    public Pageable toPageable() {
        String[] sortParts = sort.split(",");
        String property = sortParts[0];
        Sort.Direction direction = sortParts.length > 1
                ? Sort.Direction.fromString(sortParts[1])
                : Sort.Direction.DESC;
        return PageRequest.of(page, size, Sort.by(direction, property));
    }

    // Getters and setters
    public int getPage() { return page; }
    public void setPage(int page) { this.page = Math.max(0, page); }
    public int getSize() { return size; }
    public void setSize(int size) { this.size = Math.min(100, Math.max(1, size)); }
    public String getSort() { return sort; }
    public void setSort(String sort) { this.sort = sort; }
}