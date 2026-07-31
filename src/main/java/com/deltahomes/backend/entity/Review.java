package com.deltahomes.backend.entity;

import com.deltahomes.backend.entity.base.BaseEntity;
import com.deltahomes.backend.entity.enums.EntityType;
import com.deltahomes.backend.entity.user.User;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "reviews")
public class Review extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reviewer_id", nullable = false)
    private User reviewer;

    @Enumerated(EnumType.STRING)
    @Column(name = "entity_type", length = 30, nullable = false)
    private EntityType entityType;

    @Column(name = "entity_id", nullable = false)
    private Long entityId;

    @Column(name = "rating", nullable = false)
    private Byte rating;

    @Column(name = "comment", columnDefinition = "TEXT")
    private String comment;

    @Column(name = "images", columnDefinition = "JSONB")
    private String images;

    @Column(name = "video_url", length = 255)
    private String videoUrl;

    @Column(name = "interaction_verified", nullable = false)
    private Boolean interactionVerified = false;

    @Column(name = "source_appointment_id")
    private Long sourceAppointmentId;
}
