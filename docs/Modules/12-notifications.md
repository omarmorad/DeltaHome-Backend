# Delta Homes — 12 · Stage 11 · Notifications

> **Stage 11.** In-app notification system.

**Status:** Parity · **Dependencies:** Stage 0, 1 · **Effort:** S

---

## 1. Endpoints

| Method & Path | Auth | Response |
|---|---|---|
| `GET /api/v1/notifications` | Authenticated | `Paginated<NotificationSummary>` |
| `PUT /api/v1/notifications/{id}/read` | Authenticated | `200` |

---

## 2. Entity

```java
@Entity
@Table(name = "notifications")
public class Notification extends BaseEntity {
    
    @Column(name = "user_id", nullable = false)
    private UUID userId;
    
    @Column(name = "title", length = 200, nullable = false)
    private String title;
    
    @Column(name = "body", columnDefinition = "nvarchar(max)", nullable = false)
    private String body;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "type", length = 50, nullable = false)
    private NotificationType type;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "entity_type", length = 50)
    private EntityType entityType;
    
    @Column(name = "entity_id")
    private UUID entityId;
    
    @Column(name = "is_read", nullable = false)
    private Boolean isRead = false;
}
```

---

## 3. Definition of Done

- [ ] Notification listing
- [ ] Mark as read