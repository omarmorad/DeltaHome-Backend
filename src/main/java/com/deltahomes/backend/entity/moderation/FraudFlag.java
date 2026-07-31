package com.deltahomes.backend.entity.moderation;

import com.deltahomes.backend.entity.base.BaseEntity;
import com.deltahomes.backend.entity.enums.EntityType;
import com.deltahomes.backend.entity.enums.FraudFlagType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "fraud_flags")
public class FraudFlag extends BaseEntity {

    @Enumerated(EnumType.STRING)
    @Column(name = "entity_type", length = 30, nullable = false)
    private EntityType entityType;

    @Column(name = "entity_id", nullable = false)
    private Long entityId;

    @Enumerated(EnumType.STRING)
    @Column(name = "flag_type", length = 30, nullable = false)
    private FraudFlagType flagType;

    @Column(name = "status", length = 20, nullable = false)
    private String status;
}
