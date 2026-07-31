package com.deltahomes.backend.service;

import com.deltahomes.backend.dto.common.PaginatedResponse;
import com.deltahomes.backend.dto.summary.ReviewSummary;
import com.deltahomes.backend.entity.Review;
import com.deltahomes.backend.entity.enums.EntityType;
import com.deltahomes.backend.exception.BusinessException;
import com.deltahomes.backend.repository.AppointmentRepository;
import com.deltahomes.backend.repository.ReviewRepository;
import com.deltahomes.backend.util.PageUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final AppointmentRepository appointmentRepository;

    public ReviewService(ReviewRepository reviewRepository,
                         AppointmentRepository appointmentRepository) {
        this.reviewRepository = reviewRepository;
        this.appointmentRepository = appointmentRepository;
    }

    @Transactional(readOnly = true)
    public PaginatedResponse<ReviewSummary> index(String q, EntityType entityType, UUID entityId,
                                                  Integer minRating, Pageable pageable) {
        Page<ReviewSummary> page = reviewRepository.searchIndex(
                q == null ? "" : q.trim(),
                entityType == null ? null : entityType.name(),
                entityId,
                minRating,
                PageUtils.normalizeSort(pageable));
        return PaginatedResponse.from(page);
    }

    @Transactional
    public Review createReview(Review review) {
        // Interaction verification gate — must have a real booking/request/viewing
        if (review.getSourceAppointmentId() != null) {
            boolean hasInteraction = appointmentRepository.existsById(review.getSourceAppointmentId());
            if (!hasInteraction) {
                throw new BusinessException("Cannot review without a verified interaction");
            }
            review.setInteractionVerified(true);
        } else {
            review.setInteractionVerified(false);
        }

        return reviewRepository.save(review);
    }
}
