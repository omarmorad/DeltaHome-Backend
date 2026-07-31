package com.deltahomes.backend.repository;

import com.deltahomes.backend.dto.summary.AppointmentSummary;
import com.deltahomes.backend.entity.communication.Appointment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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

    @Query(value = """
            SELECT a.id, a.status, a.requested_slot AS requestedSlot, a.note, a.created_at AS createdAt,
                   p.id AS propertyId, p.title AS propertyTitle,
                   cu.name AS customerName, ow.name AS ownerName
            FROM appointments a
            JOIN properties p ON p.id = a.property_id
            JOIN users cu ON cu.id = a.customer_id
            JOIN users ow ON ow.id = a.owner_id
            WHERE (a.customer_id = CAST(:userId AS uuid) OR a.owner_id = CAST(:userId AS uuid))
              AND (CAST(:status AS text) IS NULL OR a.status = CAST(:status AS text))
            """,
            countQuery = """
            SELECT count(*) FROM appointments a
            WHERE (a.customer_id = CAST(:userId AS uuid) OR a.owner_id = CAST(:userId AS uuid))
              AND (CAST(:status AS text) IS NULL OR a.status = CAST(:status AS text))
            """,
            nativeQuery = true)
    Page<AppointmentSummary> searchIndex(@Param("userId") UUID userId,
                                         @Param("status") String status,
                                         Pageable pageable);
}
