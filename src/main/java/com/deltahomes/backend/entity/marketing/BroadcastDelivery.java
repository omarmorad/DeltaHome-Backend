package com.deltahomes.backend.entity.marketing;

import com.deltahomes.backend.entity.base.BaseEntity;
import com.deltahomes.backend.entity.user.User;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.OffsetDateTime;

@Getter
@Setter
@Entity
@Table(name = "broadcast_deliveries")
public class BroadcastDelivery extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "broadcast_id", nullable = false)
    private Broadcast broadcast;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "opened", nullable = false)
    private Boolean opened = false;

    @Column(name = "clicked", nullable = false)
    private Boolean clicked = false;

    @Column(name = "opened_at")
    private OffsetDateTime openedAt;
}
