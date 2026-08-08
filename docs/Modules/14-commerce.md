# Delta Homes — 14 · Stage 13 · Commerce

> **Stage 13.** Subscriptions, payments, and coupons.

**Status:** Parity + Aspirational · **Dependencies:** Stage 0, 1, 12 · **Effort:** L

---

## 1. Endpoints

| Method & Path | Auth | Response |
|---|---|---|
| `GET /api/v1/commerce/plans` | Public | `Paginated<PlanSummary>` |
| `POST /api/v1/commerce/subscribe` | Authenticated | `201 Subscription` |
| `GET /api/v1/commerce/subscription` | Authenticated | `Subscription` |
| `POST /api/v1/commerce/coupons/apply` | Authenticated | `CouponDiscount` |

---

## 2. Entities

```java
@Entity
@Table(name = "subscriptions")
public class Subscription extends BaseEntity {
    
    @Column(name = "user_id", nullable = false)
    private UUID userId;
    
    @Column(name = "company_id")
    private UUID companyId;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "plan_id", nullable = false)
    private SubscriptionPlan plan;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 50, nullable = false)
    private SubscriptionStatus status;
    
    @Column(name = "start_date", nullable = false)
    private OffsetDateTime startDate;
    
    @Column(name = "end_date", nullable = false)
    private OffsetDateTime endDate;
}

@Entity
@Table(name = "payments")
public class Payment extends BaseEntity {
    
    @Column(name = "user_id", nullable = false)
    private UUID userId;
    
    @Column(name = "subscription_id", nullable = false)
    private UUID subscriptionId;
    
    @Column(name = "amount", precision = 10, scale = 2, nullable = false)
    private BigDecimal amount;
    
    @Column(name = "method", length = 50)
    private String method;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 50, nullable = false)
    private PaymentStatus status;
    
    @Column(name = "gateway_reference", length = 200)
    private String gatewayReference;
}

@Entity
@Table(name = "coupons")
public class Coupon extends BaseEntity {
    
    @Column(name = "code", length = 50, nullable = false, unique = true)
    private String code;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "discount_type", length = 20, nullable = false)
    private DiscountType discountType; // PERCENTAGE, FIXED
    
    @Column(name = "discount_value", precision = 10, scale = 2, nullable = false)
    private BigDecimal discountValue;
    
    @Column(name = "max_redemptions")
    private Integer maxRedemptions;
    
    @Column(name = "current_redemptions", nullable = false)
    private Integer currentRedemptions = 0;
    
    @Column(name = "expires_at")
    private OffsetDateTime expiresAt;
}
```

---

## 3. Definition of Done

- [ ] Subscription management
- [ ] Coupon application
- [ ] Payment processing