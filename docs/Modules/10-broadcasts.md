# Delta Homes — 10 · Stage 9 · Broadcasts

> **Stage 9.** Broadcast notifications from companies to followers.

**Status:** Parity · **Dependencies:** Stage 0, 1, 4 · **Effort:** M

---

## 1. Endpoints

| Method & Path | Auth | Response |
|---|---|---|
| `GET /api/v1/broadcasts` | Authenticated | `Paginated<BroadcastSummary>` |
| `POST /api/v1/broadcasts` | Authenticated (Company) | `201 Broadcast` |

---

## 2. Entity

```java
@Entity
@Table(name = "broadcasts")
public class Broadcast extends BaseEntity {
    
    @Column(name = "company_id", nullable = false)
    private UUID companyId;
    
    @Column(name = "title_ar", length = 200, nullable = false)
    private String titleAr;
    
    @Column(name = "title_en", length = 200)
    private String titleEn;
    
    @Column(name = "body_ar", columnDefinition = "nvarchar(max)", nullable = false)
    private String bodyAr;
    
    @Column(name = "body_en", columnDefinition = "nvarchar(max)")
    private String bodyEn;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "type", length = 50, nullable = false)
    private BroadcastType type;
    
    @Column(name = "recipients_count", nullable = false)
    private Integer recipientsCount = 0;
}
```

---

## 3. Business Rules

- **Quota Deduction**: Creating broadcast deducts from subscription quota
- **Bilingual**: Companies can compose in Arabic, English, or both

---

## 4. Definition of Done

- [ ] Broadcast creation with quota check
- [ ] Delivery to followers