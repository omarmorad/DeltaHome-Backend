# Delta Homes — 07 · Stage 6 · Reviews

> **Stage 6.** Review and rating system with interaction gating.

**Status:** Parity · **Dependencies:** Stage 0, 1 · **Effort:** M

---

## 1. Endpoints

| Method & Path | Auth | Response |
|---|---|---|
| `GET /api/v1/reviews` | Public | `Paginated<ReviewSummary>` |
| `GET /api/v1/reviews/summary/{entityType}/{entityId}` | Public | `ReviewAggregate` |
| `POST /api/v1/reviews` | Authenticated | `201 Review` |

---

## 2. Entity

```java
@Entity
@Table(name = "reviews")
public class Review extends BaseEntity {
    
    @Column(name = "user_id", nullable = false)
    private UUID userId;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "entity_type", length = 50, nullable = false)
    private EntityType entityType;
    
    @Column(name = "entity_id", nullable = false)
    private UUID entityId;
    
    @Column(name = "rating", nullable = false)
    private Integer rating; // 1-5
    
    @Column(name = "comment", columnDefinition = "nvarchar(max)")
    private String comment;
    
    @Column(name = "interaction_verified", nullable = false)
    private Boolean interactionVerified = false;
}
```

---

## 3. Service Logic

- **Interaction Gate**: Users can only review after confirmed appointment
- **One Review Per Entity**: Each user can review each entity only once

---

## 4. Definition of Done

- [ ] Review CRUD with interaction gating
- [ ] Rating aggregation