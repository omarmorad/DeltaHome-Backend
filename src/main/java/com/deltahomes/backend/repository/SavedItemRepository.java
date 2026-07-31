package com.deltahomes.backend.repository;

import com.deltahomes.backend.entity.SavedItem;
import com.deltahomes.backend.entity.enums.EntityType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface SavedItemRepository extends JpaRepository<SavedItem, UUID> {
    List<SavedItem> findByUserIdAndEntityType(UUID userId, EntityType entityType);
    Page<SavedItem> findByUserIdAndEntityType(UUID userId, EntityType entityType, Pageable pageable);
    boolean existsByUserIdAndEntityTypeAndEntityId(UUID userId, EntityType entityType, UUID entityId);
    void deleteByUserIdAndEntityTypeAndEntityId(UUID userId, EntityType entityType, UUID entityId);
}
