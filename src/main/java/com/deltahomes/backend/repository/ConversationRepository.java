package com.deltahomes.backend.repository;

import com.deltahomes.backend.entity.communication.Conversation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ConversationRepository extends JpaRepository<Conversation, UUID> {
    List<Conversation> findByUserOneIdOrUserTwoIdOrderByUpdatedAtDesc(UUID userOneId, UUID userTwoId);
    Optional<Conversation> findByUserOneIdAndUserTwoId(UUID userOneId, UUID userTwoId);
}
