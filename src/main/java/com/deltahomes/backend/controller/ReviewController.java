package com.deltahomes.backend.controller;

import com.deltahomes.backend.dto.common.PaginatedResponse;
import com.deltahomes.backend.dto.summary.ReviewSummary;
import com.deltahomes.backend.entity.Review;
import com.deltahomes.backend.entity.enums.EntityType;
import com.deltahomes.backend.service.ReviewService;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/reviews")
public class ReviewController {

    private final ReviewService reviewService;

    public ReviewController(ReviewService reviewService) {
        this.reviewService = reviewService;
    }

    @GetMapping
    public ResponseEntity<PaginatedResponse<ReviewSummary>> index(
            @RequestParam(defaultValue = "") String q,
            @RequestParam(required = false) EntityType entityType,
            @RequestParam(required = false) UUID entityId,
            @RequestParam(required = false) Integer minRating,
            @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(reviewService.index(q, entityType, entityId, minRating, pageable));
    }

    @PostMapping
    public ResponseEntity<Review> createReview(@RequestBody Review review) {
        return ResponseEntity.ok(reviewService.createReview(review));
    }
}
