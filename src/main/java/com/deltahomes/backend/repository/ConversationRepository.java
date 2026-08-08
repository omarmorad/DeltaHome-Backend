package com.deltahomes.backend.repository;

import com.deltahomes.backend.dto.summary.ConversationSummary;
import com.deltahomes.backend.entity.communication.Conversation;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
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

    /**
     * Index query with eager fetching of userOne and userTwo relationships.
     * Uses JPQL with @EntityGraph to avoid LazyInitializationException.
     */
    @EntityGraph(attributePaths = {"userOne", "userTwo"})
    @Query("SELECT c FROM Conversation c " +
           "WHERE c.userOne.id = :userId OR c.userTwo.id = :userId")
    Page<Conversation> searchIndex(@Param("userId") UUID userId, Pageable pageable);
}
