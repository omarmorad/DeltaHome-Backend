package com.deltahomes.backend.entity.communication;

import com.deltahomes.backend.entity.base.BaseEntity;
import com.deltahomes.backend.entity.user.User;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "conversations")
public class Conversation extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_one_id", nullable = false)
    private User userOne;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_two_id", nullable = false)
    private User userTwo;

    @Column(name = "last_message_preview", length = 255)
    private String lastMessagePreview;

    @Column(name = "last_seen_user_one")
    private Long lastSeenUserOne;

    @Column(name = "last_seen_user_two")
    private Long lastSeenUserTwo;
}
