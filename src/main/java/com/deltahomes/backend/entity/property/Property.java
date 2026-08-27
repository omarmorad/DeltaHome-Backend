package com.deltahomes.backend.entity.property;

import com.deltahomes.backend.entity.base.BaseEntity;
import com.deltahomes.backend.entity.enums.*;
import com.deltahomes.backend.entity.location.City;
import com.deltahomes.backend.entity.location.District;
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
@Table(name = "properties")
public class Property extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id", nullable = false)
    private User owner;

    @Column(name = "title", length = 200, nullable = false)
    private String title;

    /** Arabic title — optional; responses fall back to {@link #title} when empty. */
    @Column(name = "title_ar", length = 200)
    private String titleAr;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    /** Arabic description — optional; responses fall back to {@link #description} when empty. */
    @Column(name = "description_ar", columnDefinition = "TEXT")
    private String descriptionAr;

    @Column(name = "price", precision = 12, scale = 2, nullable = false)
    private BigDecimal price;

    @Enumerated(EnumType.STRING)
    @Column(name = "purpose", length = 10, nullable = false)
    private PropertyPurpose purpose;

    @Column(name = "category", length = 30, nullable = false)
    private String category;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "city_id", nullable = false)
    private City city;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "district_id", nullable = false)
    private District district;

    @Column(name = "street", length = 200)
    private String street;

    @Column(name = "latitude", precision = 10, scale = 7)
    private BigDecimal latitude;

    @Column(name = "longitude", precision = 10, scale = 7)
    private BigDecimal longitude;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 20, nullable = false)
    private PropertyStatus status = PropertyStatus.DRAFT;

    @Enumerated(EnumType.STRING)
    @Column(name = "hide_reason", length = 30)
    private HideReason hideReason;

    @Column(name = "is_featured", nullable = false)
    private Boolean isFeatured = false;

    @Enumerated(EnumType.STRING)
    @Column(name = "finishing_level", length = 20)
    private FinishingLevel finishingLevel;

    @Enumerated(EnumType.STRING)
    @Column(name = "readiness", length = 20, nullable = false)
    private Readiness readiness;
    @JdbcTypeCode(SqlTypes.JSON)    @Column(name = "features", columnDefinition = "JSONB DEFAULT '{}'")
    private String features;
}
