package com.deltahomes.backend.repository;

import com.deltahomes.backend.entity.SavedItem;
import com.deltahomes.backend.entity.enums.EntityType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SavedItemRepository extends JpaRepository<SavedItem, Long> {
    List<SavedItem> findByUserIdAndEntityType(Long userId, EntityType entityType);
    boolean existsByUserIdAndEntityTypeAndEntityId(Long userId, EntityType entityType, Long entityId);
    void deleteByUserIdAndEntityTypeAndEntityId(Long userId, EntityType entityType, Long entityId);
}
