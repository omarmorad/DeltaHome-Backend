package com.deltahomes.backend.repository;

import com.deltahomes.backend.entity.Review;
import com.deltahomes.backend.entity.enums.EntityType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ReviewRepository extends JpaRepository<Review, UUID> {
    List<Review> findByEntityTypeAndEntityId(EntityType entityType, UUID entityId);
    boolean existsByReviewerIdAndSourceAppointmentId(UUID reviewerId, UUID sourceAppointmentId);
}
