package com.deltahomes.backend.entity.company;

import com.deltahomes.backend.entity.base.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@Entity
@Table(name = "company_portfolio")
public class CompanyPortfolio extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id", nullable = false)
    private Company company;

    @Column(name = "before_url", length = 255)
    private String beforeUrl;

    @Column(name = "after_url", length = 255)
    private String afterUrl;

    @Column(name = "caption", length = 255)
    private String caption;

    @Column(name = "project_date")
    private LocalDate projectDate;
}
