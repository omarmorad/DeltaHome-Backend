package com.deltahomes.backend.entity.commerce;

import com.deltahomes.backend.entity.base.BaseEntity;
import com.deltahomes.backend.entity.enums.SubscriptionTier;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@Entity
@Table(name = "subscription_plans")
public class SubscriptionPlan extends BaseEntity {

    @Column(name = "name", length = 100, nullable = false)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(name = "tier", length = 20, nullable = false)
    private SubscriptionTier tier;

    @Column(name = "price", precision = 12, scale = 2, nullable = false)
    private BigDecimal price;

    @Column(name = "listing_cap")
    private Integer listingCap;

    @Column(name = "broadcast_cap")
    private Integer broadcastCap;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;
}
