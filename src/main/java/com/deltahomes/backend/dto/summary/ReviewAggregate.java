package com.deltahomes.backend.dto.summary;

import java.util.Map;

/**
 * Aggregated rating statistics for one entity, returned by
 * {@code GET /api/v1/reviews/summary/{entityType}/{entityId}}.
 *
 * @param ratingCount  total number of reviews
 * @param averageRating mean rating (0 when there are no reviews)
 * @param distribution  rating value (1-5) -> number of reviews with that rating
 */
public record ReviewAggregate(
        long ratingCount,
        double averageRating,
        Map<Integer, Long> distribution
) {
}
