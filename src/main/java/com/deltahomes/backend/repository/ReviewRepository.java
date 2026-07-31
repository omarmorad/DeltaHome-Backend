package com.deltahomes.backend.repository;

import com.deltahomes.backend.entity.Review;
import com.deltahomes.backend.entity.enums.EntityType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {
    List<Review> findByEntityTypeAndEntityId(EntityType entityType, Long entityId);
    boolean existsByReviewerIdAndSourceAppointmentId(Long reviewerId, Long sourceAppointmentId);
}
