package com.deltahomes.backend.repository;

import com.deltahomes.backend.dto.summary.AppointmentSummary;
import com.deltahomes.backend.entity.communication.Appointment;
import com.deltahomes.backend.entity.enums.AppointmentStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface AppointmentRepository extends JpaRepository<Appointment, UUID> {
    List<Appointment> findByCustomerId(UUID customerId);
    List<Appointment> findByOwnerId(UUID ownerId);

    /**
     * Index query with eager fetching of property, customer, and owner relationships.
     * Uses JPQL with @EntityGraph to avoid LazyInitializationException.
     */
    @EntityGraph(attributePaths = {"property", "customer", "owner"})
    @Query("SELECT a FROM Appointment a " +
           "WHERE (a.customer.id = :userId OR a.owner.id = :userId) " +
           "AND (:status IS NULL OR a.status = :status)")
    Page<Appointment> searchIndex(@Param("userId") UUID userId,
                                  @Param("status") AppointmentStatus status,
                                  Pageable pageable);
}
