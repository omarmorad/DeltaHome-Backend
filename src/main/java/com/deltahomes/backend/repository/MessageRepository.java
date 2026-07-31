package com.deltahomes.backend.repository;

import com.deltahomes.backend.dto.summary.MessageSummary;
import com.deltahomes.backend.entity.communication.Message;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface MessageRepository extends JpaRepository<Message, UUID> {
    List<Message> findByConversationIdOrderByCreatedAtAsc(UUID conversationId);

    @Query(value = """
            SELECT m.id, m.type, m.text_body AS textBody, m.media_url AS mediaUrl, m.payload,
                   s.name AS senderName, m.created_at AS createdAt
            FROM messages m
            JOIN users s ON s.id = m.sender_id
            WHERE m.conversation_id = CAST(:conversationId AS uuid)
              AND (CAST(:q AS text) = '' OR websearch_to_tsquery('simple', :q) @@ m.search_vector)
            """,
            countQuery = """
            SELECT count(*) FROM messages m
            WHERE m.conversation_id = CAST(:conversationId AS uuid)
              AND (CAST(:q AS text) = '' OR websearch_to_tsquery('simple', :q) @@ m.search_vector)
            """,
            nativeQuery = true)
    Page<MessageSummary> searchIndex(@Param("conversationId") UUID conversationId,
                                     @Param("q") String q,
                                     Pageable pageable);
}
