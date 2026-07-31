package com.deltahomes.backend.entity.location;

import com.deltahomes.backend.entity.base.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "cities")
public class City extends BaseEntity {

    @Column(name = "name", length = 100, nullable = false)
    private String name;

    @Column(name = "name_ar", length = 100, nullable = false)
    private String nameAr;

    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;
}
