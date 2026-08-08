package com.deltahomes.backend.service;

import com.deltahomes.backend.dto.common.PaginatedResponse;
import com.deltahomes.backend.dto.summary.ReviewAggregate;
import com.deltahomes.backend.dto.summary.ReviewSummary;
import com.deltahomes.backend.entity.Review;
import com.deltahomes.backend.entity.communication.Appointment;
import com.deltahomes.backend.entity.enums.AppointmentStatus;
import com.deltahomes.backend.entity.enums.EntityType;
import com.deltahomes.backend.entity.user.User;
import com.deltahomes.backend.exception.BusinessException;
import com.deltahomes.backend.repository.AppointmentRepository;
import com.deltahomes.backend.repository.ReviewRepository;
import com.deltahomes.backend.util.PageUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashMap;
import java.util.Map;
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
                entityType,
                entityId,
                minRating,
                PageUtils.normalizeSort(pageable))
            .map(this::toSummary);
        return PaginatedResponse.from(page);
    }

    @Transactional
    public Review createReview(User reviewer, Review review) {
        if (review.getEntityType() == null || review.getEntityId() == null) {
            throw new BusinessException("entityType and entityId are required");
        }
        if (review.getRating() == null || review.getRating() < 1 || review.getRating() > 5) {
            throw new BusinessException("Rating must be between 1 and 5");
        }
        if (reviewRepository.existsByReviewerIdAndEntityTypeAndEntityId(
                reviewer.getId(), review.getEntityType(), review.getEntityId())) {
            throw new BusinessException("You have already reviewed this entity");
        }

        // Interaction verification gate — the appointment must belong to the reviewer
        // and be accepted/completed (a real, confirmed interaction).
        if (review.getSourceAppointmentId() != null) {
            Appointment appointment = appointmentRepository.findById(review.getSourceAppointmentId())
                    .orElseThrow(() -> new BusinessException("Cannot review without a verified interaction"));
            boolean belongsToReviewer = appointment.getCustomer().getId().equals(reviewer.getId());
            boolean confirmed = appointment.getStatus() == AppointmentStatus.ACCEPTED
                    || appointment.getStatus() == AppointmentStatus.COMPLETED;
            if (!belongsToReviewer || !confirmed) {
                throw new BusinessException("Cannot review without a verified interaction");
            }
            review.setInteractionVerified(true);
        } else {
            review.setInteractionVerified(false);
        }

        review.setId(null);
        review.setReviewer(reviewer);
        return reviewRepository.save(review);
    }

    @Transactional(readOnly = true)
    public ReviewAggregate getAggregate(EntityType entityType, UUID entityId) {
        long count = reviewRepository.countByEntityTypeAndEntityId(entityType, entityId);
        Double average = reviewRepository.averageRating(entityType, entityId);

        Map<Integer, Long> distribution = new LinkedHashMap<>();
        for (int i = 1; i <= 5; i++) {
            distribution.put(i, 0L);
        }
        reviewRepository.ratingDistribution(entityType, entityId).forEach(row -> {
            int rating = ((Number) row[0]).intValue();
            long total = ((Number) row[1]).longValue();
            distribution.put(rating, total);
        });

        return new ReviewAggregate(count, average == null ? 0.0 : average, distribution);
    }

    private ReviewSummary toSummary(Review r) {
        return new ReviewSummary() {
            @Override public UUID getId() { return r.getId(); }
            @Override public String getEntityType() { return r.getEntityType() != null ? r.getEntityType().name() : null; }
            @Override public UUID getEntityId() { return r.getEntityId(); }
            @Override public Byte getRating() { return r.getRating(); }
            @Override public String getComment() { return r.getComment(); }
            @Override public Boolean getInteractionVerified() { return r.getInteractionVerified(); }
            @Override public String getReviewerName() { return r.getReviewer() != null ? r.getReviewer().getName() : null; }
            @Override public java.time.OffsetDateTime getCreatedAt() { return r.getCreatedAt(); }
        };
    }
}
