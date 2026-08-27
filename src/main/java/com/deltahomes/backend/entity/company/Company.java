package com.deltahomes.backend.entity.company;

import com.deltahomes.backend.entity.base.BaseEntity;
import com.deltahomes.backend.entity.enums.CompanyType;
import com.deltahomes.backend.entity.enums.SubscriptionTier;
import com.deltahomes.backend.entity.user.User;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;

@Getter
@Setter
@Entity
@Table(name = "companies")
public class Company extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id", nullable = false)
    private User owner;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", length = 30, nullable = false)
    private CompanyType type;

    @Column(name = "name", length = 200, nullable = false)
    private String name;

    /** Arabic name — optional; localized responses fall back to {@link #name}. */
    @Column(name = "name_ar", length = 200)
    private String nameAr;

    @Column(name = "logo_url", length = 255)
    private String logoUrl;

    @Column(name = "cover_url", length = 255)
    private String coverUrl;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    /** Arabic description — optional; localized responses fall back to {@link #description}. */
    @Column(name = "description_ar", columnDefinition = "TEXT")
    private String descriptionAr;

    @Column(name = "phone", length = 20)
    private String phone;

    @Column(name = "whatsapp", length = 20)
    private String whatsapp;

    @Column(name = "email", length = 150)
    private String email;

    @Column(name = "website", length = 150)
    private String website;

    @Column(name = "verified", nullable = false)
    private Boolean verified = false;

    @Column(name = "followers_count", nullable = false)
    private Integer followersCount = 0;

    @Column(name = "reputation_score", precision = 3, scale = 2, nullable = false)
    private BigDecimal reputationScore = BigDecimal.ZERO;

    @Enumerated(EnumType.STRING)
    @Column(name = "plan", length = 20, nullable = false)
    private SubscriptionTier plan = SubscriptionTier.BASIC;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "coverage_area", columnDefinition = "JSONB")
    private String coverageArea;
}
