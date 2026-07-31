package com.deltahomes.backend.entity.marketing;

import com.deltahomes.backend.entity.base.BaseEntity;
import com.deltahomes.backend.entity.company.Company;
import com.deltahomes.backend.entity.enums.BroadcastType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "broadcasts")
public class Broadcast extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id", nullable = false)
    private Company company;

    @Column(name = "title", length = 255, nullable = false)
    private String title;

    @Column(name = "body", columnDefinition = "TEXT")
    private String body;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", length = 20, nullable = false)
    private BroadcastType type;
}
