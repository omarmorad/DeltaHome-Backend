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

    @Column(name = "name_ar", length = 100, nullable = false)
    private String nameAr;

    @Column(name = "name_en", length = 100)
    private String nameEn;

    @Enumerated(EnumType.STRING)
    @Column(name = "tier", length = 50, nullable = false)
    private SubscriptionTier tier;

    @Column(name = "price", precision = 10, scale = 2, nullable = false)
    private BigDecimal price;

    @Column(name = "listing_quota", nullable = false)
    private Integer listingQuota;

    @Column(name = "broadcast_quota", nullable = false)
    private Integer broadcastQuota;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;
}
