package com.deltahomes.backend.repository;

import com.deltahomes.backend.dto.summary.ConversationSummary;
import com.deltahomes.backend.entity.communication.Conversation;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ConversationRepository extends JpaRepository<Conversation, UUID> {
    List<Conversation> findByUserOneIdOrUserTwoIdOrderByUpdatedAtDesc(UUID userOneId, UUID userTwoId);
    Optional<Conversation> findByUserOneIdAndUserTwoId(UUID userOneId, UUID userTwoId);

    @Query(value = """
            SELECT c.id, c.last_message_preview AS lastMessagePreview, c.updated_at AS updatedAt,
                   u.id AS otherUserId, u.name AS otherUserName
            FROM conversations c
            JOIN users u ON u.id = CASE WHEN c.user_one_id = CAST(:userId AS uuid) THEN c.user_two_id ELSE c.user_one_id END
            WHERE c.user_one_id = CAST(:userId AS uuid) OR c.user_two_id = CAST(:userId AS uuid)
            """,
            countQuery = """
            SELECT count(*) FROM conversations c
            WHERE c.user_one_id = CAST(:userId AS uuid) OR c.user_two_id = CAST(:userId AS uuid)
            """,
            nativeQuery = true)
    Page<ConversationSummary> searchIndex(@Param("userId") UUID userId, Pageable pageable);
}
