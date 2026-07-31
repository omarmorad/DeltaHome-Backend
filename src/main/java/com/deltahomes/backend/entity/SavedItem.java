package com.deltahomes.backend.entity;

import com.deltahomes.backend.entity.base.BaseEntity;
import com.deltahomes.backend.entity.enums.EntityType;
import com.deltahomes.backend.entity.user.User;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "saved_items", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"user_id", "entity_type", "entity_id"})
})
public class SavedItem extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(name = "entity_type", length = 30, nullable = false)
    private EntityType entityType;

    @Column(name = "entity_id", nullable = false)
    private UUID entityId;
}
