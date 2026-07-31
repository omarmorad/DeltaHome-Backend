package com.deltahomes.backend.entity.company;

import com.deltahomes.backend.entity.base.BaseEntity;
import com.deltahomes.backend.entity.location.Service;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "company_services", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"company_id", "service_id"})
})
public class CompanyService extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id", nullable = false)
    private Company company;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "service_id", nullable = false)
    private Service service;
}
