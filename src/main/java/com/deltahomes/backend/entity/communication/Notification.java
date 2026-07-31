package com.deltahomes.backend.entity.communication;

import com.deltahomes.backend.entity.base.BaseEntity;
import com.deltahomes.backend.entity.enums.EntityType;
import com.deltahomes.backend.entity.enums.NotificationType;
import com.deltahomes.backend.entity.user.User;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "notifications")
public class Notification extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "title", length = 255, nullable = false)
    private String title;

    @Column(name = "body", columnDefinition = "TEXT")
    private String body;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", length = 20, nullable = false)
    private NotificationType type;

    @Enumerated(EnumType.STRING)
    @Column(name = "entity_type", length = 30)
    private EntityType entityType;

    @Column(name = "entity_id")
    private Long entityId;

    @Column(name = "is_read", nullable = false)
    private Boolean isRead = false;
}
