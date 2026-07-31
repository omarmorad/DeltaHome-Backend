package com.deltahomes.backend.entity.admin;

import com.deltahomes.backend.entity.base.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "feature_flags")
public class FeatureFlag extends BaseEntity {

    @Column(name = "key_name", length = 100, unique = true, nullable = false)
    private String key;

    @Column(name = "is_enabled", nullable = false)
    private Boolean isEnabled = false;

    @Column(name = "rollout_scope", columnDefinition = "JSONB")
    private String rolloutScope;
}
