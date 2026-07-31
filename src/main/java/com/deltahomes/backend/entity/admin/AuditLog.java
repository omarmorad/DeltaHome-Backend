package com.deltahomes.backend.entity.admin;

import com.deltahomes.backend.entity.base.BaseEntity;
import com.deltahomes.backend.entity.user.User;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "audit_logs")
public class AuditLog extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "admin_id", nullable = false)
    private User admin;

    @Column(name = "action", length = 100, nullable = false)
    private String action;

    @Column(name = "target_type", length = 50)
    private String targetType;

    @Column(name = "target_id")
    private UUID targetId;

    @Column(name = "ip_address", length = 45)
    private String ipAddress;

    @Column(name = "reason", length = 255)
    private String reason;
}
