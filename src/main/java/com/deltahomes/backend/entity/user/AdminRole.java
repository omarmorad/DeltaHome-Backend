package com.deltahomes.backend.entity.user;

import com.deltahomes.backend.entity.base.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "admin_roles")
public class AdminRole extends BaseEntity {

    @Column(name = "name", length = 50, nullable = false, unique = true)
    private String name;
}
