package com.deltahomes.backend.service;

import com.deltahomes.backend.entity.Review;
import com.deltahomes.backend.exception.BusinessException;
import com.deltahomes.backend.repository.AppointmentRepository;
import com.deltahomes.backend.repository.ReviewRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final AppointmentRepository appointmentRepository;

    public ReviewService(ReviewRepository reviewRepository,
                         AppointmentRepository appointmentRepository) {
        this.reviewRepository = reviewRepository;
        this.appointmentRepository = appointmentRepository;
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
