package com.deltahomes.backend.repository;

import com.deltahomes.backend.dto.summary.MessageSummary;
import com.deltahomes.backend.entity.communication.Message;
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
public interface MessageRepository extends JpaRepository<Message, UUID> {
    List<Message> findByConversationIdOrderByCreatedAtAsc(UUID conversationId);

    /**
     * Index query with eager fetching of sender relationship.
     * Uses JPQL with @EntityGraph to avoid LazyInitializationException.
     */
    @EntityGraph(attributePaths = {"sender"})
    @Query("SELECT m FROM Message m " +
           "WHERE m.conversation.id = :conversationId " +
           "AND (:q = '' OR LOWER(m.textBody) LIKE LOWER(CONCAT('%', :q, '%')))")
    Page<Message> searchIndex(@Param("conversationId") UUID conversationId,
                              @Param("q") String q,
                              Pageable pageable);
}
