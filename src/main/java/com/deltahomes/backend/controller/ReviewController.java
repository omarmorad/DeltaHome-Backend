package com.deltahomes.backend.controller;

import com.deltahomes.backend.dto.common.ApiResponse;
import com.deltahomes.backend.dto.summary.ReviewAggregate;
import com.deltahomes.backend.dto.summary.ReviewSummary;
import com.deltahomes.backend.entity.Review;
import com.deltahomes.backend.entity.enums.EntityType;
import com.deltahomes.backend.entity.user.User;
import com.deltahomes.backend.service.ReviewService;
import com.deltahomes.backend.service.UserContext;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/reviews")
public class ReviewController {

    private final ReviewService reviewService;
    private final UserContext userContext;

    public ReviewController(ReviewService reviewService, UserContext userContext) {
        this.reviewService = reviewService;
        this.userContext = userContext;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<ReviewSummary>>> index(
            @RequestParam(defaultValue = "") String q,
            @RequestParam(required = false) EntityType entityType,
            @RequestParam(required = false) UUID entityId,
            @RequestParam(required = false) Integer minRating,
            @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.page(
                reviewService.index(q, entityType, entityId, minRating, pageable)));
    }

    @GetMapping("/summary/{entityType}/{entityId}")
    public ResponseEntity<ApiResponse<ReviewAggregate>> getSummary(@PathVariable EntityType entityType,
                                                                   @PathVariable UUID entityId) {
        return ResponseEntity.ok(ApiResponse.ok(reviewService.getAggregate(entityType, entityId)));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<Review>> createReview(@AuthenticationPrincipal UserDetails principal,
                                                            @RequestBody Review review) {
        User reviewer = userContext.currentUser(principal);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok(reviewService.createReview(reviewer, review)));
    }
}
