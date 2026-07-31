package com.deltahomes.backend.repository;

import com.deltahomes.backend.dto.summary.NotificationSummary;
import com.deltahomes.backend.entity.communication.Notification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, UUID> {
    Page<Notification> findByUserIdOrderByCreatedAtDesc(UUID userId, Pageable pageable);
    long countByUserIdAndIsReadFalse(UUID userId);

    @Query(value = """
            SELECT n.id, n.title, n.body, n.type, n.entity_type AS entityType, n.entity_id AS entityId,
                   n.is_read AS isRead, n.created_at AS createdAt
            FROM notifications n
            WHERE n.user_id = CAST(:userId AS uuid)
              AND (CAST(:isRead AS boolean) IS NULL OR n.is_read = CAST(:isRead AS boolean))
              AND (CAST(:type AS text) IS NULL OR n.type = CAST(:type AS text))
              AND (CAST(:q AS text) = '' OR websearch_to_tsquery('simple', :q) @@ n.search_vector)
            """,
            countQuery = """
            SELECT count(*) FROM notifications n
            WHERE n.user_id = CAST(:userId AS uuid)
              AND (CAST(:isRead AS boolean) IS NULL OR n.is_read = CAST(:isRead AS boolean))
              AND (CAST(:type AS text) IS NULL OR n.type = CAST(:type AS text))
              AND (CAST(:q AS text) = '' OR websearch_to_tsquery('simple', :q) @@ n.search_vector)
            """,
            nativeQuery = true)
    Page<NotificationSummary> searchIndex(@Param("userId") UUID userId,
                                          @Param("q") String q,
                                          @Param("type") String type,
                                          @Param("isRead") Boolean isRead,
                                          Pageable pageable);
}
