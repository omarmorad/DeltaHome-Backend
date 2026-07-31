package com.deltahomes.backend.entity;

import com.deltahomes.backend.entity.base.BaseEntity;
import com.deltahomes.backend.entity.company.Company;
import com.deltahomes.backend.entity.user.User;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "followers", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"user_id", "company_id"})
})
public class Follower extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id", nullable = false)
    private Company company;
}
