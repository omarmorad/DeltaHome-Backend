# Delta Homes — 08 · Stage 7 · Appointments

> **Stage 7.** Property viewing appointments with state machine.

**Status:** Parity · **Dependencies:** Stage 0, 1, 3 · **Effort:** M

---

## 1. Endpoints

| Method & Path | Auth | Response |
|---|---|---|
| `GET /api/v1/appointments` | Authenticated | `Paginated<AppointmentSummary>` |
| `POST /api/v1/appointments` | Authenticated | `201 Appointment` |
| `PUT /api/v1/appointments/{id}/status` | Authenticated | `200 Appointment` |

---

## 2. State Machine

```
PENDING → ACCEPTED → COMPLETED
    ↓         ↓
  REJECTED  CANCELLED
```

---

## 3. Entity

```java
@Entity
@Table(name = "appointments")
public class Appointment extends BaseEntity {
    
    @Column(name = "user_id", nullable = false)
    private UUID userId;
    
    @Column(name = "property_id", nullable = false)
    private UUID propertyId;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 50, nullable = false)
    private AppointmentStatus status = AppointmentStatus.PENDING;
    
    @Column(name = "requested_slot", nullable = false)
    private OffsetDateTime requestedSlot;
    
    @Column(name = "note", columnDefinition = "nvarchar(500)")
    private String note;
    
    @Column(name = "rejection_reason", length = 500)
    private String rejectionReason;
}
```

---

## 4. Definition of Done

- [ ] Appointment CRUD with state transitions
- [ ] Status change notifications