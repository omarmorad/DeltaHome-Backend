# Delta Homes — 09 · Stage 8 · Chat

> **Stage 8.** Real-time messaging between users and property managers.

**Status:** Parity · **Dependencies:** Stage 0, 1, 3, 4 · **Effort:** M

---

## 1. Endpoints

| Method & Path | Auth | Response |
|---|---|---|
| `GET /api/v1/chat/conversations` | Authenticated | `Paginated<ConversationSummary>` |
| `POST /api/v1/chat/conversations` | Authenticated | `201 Conversation` |
| `GET /api/v1/chat/conversations/{id}/messages` | Authenticated | `Paginated<MessageSummary>` |
| `POST /api/v1/chat/conversations/{id}/messages` | Authenticated | `201 Message` |

---

## 2. Entities

```java
@Entity
@Table(name = "conversations")
public class Conversation extends BaseEntity {
    
    @Column(name = "participant_one_id", nullable = false)
    private UUID participantOneId;
    
    @Column(name = "participant_two_id", nullable = false)
    private UUID participantTwoId;
    
    @Column(name = "property_id")
    private UUID propertyId;
    
    @Column(name = "last_message_preview", length = 255)
    private String lastMessagePreview;
}

@Entity
@Table(name = "messages")
public class Message extends BaseEntity {
    
    @Column(name = "conversation_id", nullable = false)
    private UUID conversationId;
    
    @Column(name = "sender_id", nullable = false)
    private UUID senderId;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "type", length = 50, nullable = false)
    private MessageType type = MessageType.TEXT;
    
    @Column(name = "text_body", columnDefinition = "nvarchar(max)")
    private String textBody;
    
    @Column(name = "media_url", length = 1000)
    private String mediaUrl;
    
    @Column(name = "is_read", nullable = false)
    private Boolean isRead = false;
    
    @Column(name = "read_at")
    private OffsetDateTime readAt;
}
```

---

## 3. Definition of Done

- [ ] Conversation management
- [ ] Message sending/receiving
- [ ] Read status tracking