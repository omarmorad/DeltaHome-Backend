package com.deltahomes.backend.entity.location;

import com.deltahomes.backend.entity.base.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "districts")
public class District extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "city_id", nullable = false)
    private City city;

    @Column(name = "name", length = 100, nullable = false)
    private String name;

    @Column(name = "name_ar", length = 100, nullable = false)
    private String nameAr;
}
