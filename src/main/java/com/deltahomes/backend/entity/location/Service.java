package com.deltahomes.backend.entity.location;

import com.deltahomes.backend.entity.base.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "services")
public class Service extends BaseEntity {

    @Column(name = "name", length = 100, nullable = false)
    private String name;

    @Column(name = "name_ar", length = 150, nullable = false)
    private String nameAr;

    @Column(name = "name_en", length = 150)
    private String nameEn;

    @Column(name = "category", length = 50, nullable = false)
    private String category;

    @Column(name = "icon_url", length = 500)
    private String iconUrl;
}
