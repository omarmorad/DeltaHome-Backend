# Delta Homes — Strategic Product & Release Roadmap

> **خريطة الطريق، الإصدارات، ما الذي سيأتي لاحقاً.**  
> **Strategic release roadmap, Microsoft SQL Server data platform, bilingual mobile app, Arabic admin, and future capabilities.**

---

## 1. Release Strategy & Release Horizons

Delta Homes follows a 3-tier phased release schedule designed to validate core marketplace operations, establish monetization channels, and expand into advanced product capabilities.

```
┌─────────────────────────┐      ┌─────────────────────────┐      ┌─────────────────────────┐
│     PHASE 1 (v1.0)      │      │     PHASE 2 (v1.x)      │      │     PHASE 3 (v2.0+)     │
│   Core MVP Parity       │ ───► │  Commerce & Growth      │ ───► │ Future Expansion        │
│                         │      │                         │      │                         │
│ • SQL Server Platform   │      │ • Subscription Plans    │      │ • Social OAuth (Google) │
│ • AR/EN Mobile API      │      │ • Broadcast System      │      │ • Construction Projects │
│ • Arabic Admin (RTL)    │      │ • Coupon Engine         │      │ • PostHog Analytics     │
│ • SQL Full-Text Search  │      │ • Commercial Analytics  │      │ • Job Queue & Push      │
└─────────────────────────┘      └─────────────────────────┘      └─────────────────────────┘
```

---

## 2. Phase 1: Core MVP Parity Release (v1.0)

**Objective**: Establish the core Egyptian home-lifecycle marketplace on Microsoft SQL Server with bilingual mobile app API support and an Arabic-first Admin console.

### Key Milestones & Capabilities
- [x] **Data Platform**: Microsoft SQL Server 2022+ Database with `uniqueidentifier` PKs and `nvarchar` Unicode support.
- [x] **Localization Infrastructure**: Spring LocaleResolver and MessageSource handling `ar-EG` and `en-US` headers.
- [x] **Auth & Identity**: Phone & Email OTP verification, JWT access/refresh token cycle, culture preference tracking.
- [x] **Location & Taxonomies**: Governorates, Cities, Districts with bilingual `nameAr` and `nameEn` fields.
- [x] **Property Catalog**: Full property listing CRUD, multi-image upload, bilingual descriptions, status lifecycle.
- [x] **SQL Server Full-Text Search**: Fast Arabic & English text search via SQL Server Full-Text Catalogs (`CONTAINS`).
- [x] **Company Directory**: Verified company profiles for real estate agencies, finishing firms, and maintenance providers.
- [x] **Appointments**: Property viewing booking workflow and status state machine.
- [x] **Reviews & Ratings**: Interaction-gated entity review system with localized error payloads.
- [x] **Real-Time Chat**: Direct messaging rooms between property seekers and listing managers.
- [x] **Arabic Admin Dashboard**: React/Vue.js admin console rendered natively in Arabic with RTL (`dir="rtl"`) styling.

### Technology Stack (Phase 1)

| Component | Technology |
|---|---|
| Backend | Spring Boot 3.x (Java 17+) |
| ORM | Spring Data JPA + Hibernate |
| Database | Microsoft SQL Server 2022+ |
| Security | Spring Security + JWT |
| Localization | Spring MessageSource |
| Migrations | Flyway |
| Testing | JUnit 5, Mockito, Testcontainers |
| Admin Dashboard | React or Vue.js (RTL) |

---

## 3. Phase 2: Monetization & Commercial Growth (v1.1 – v1.2)

**Objective**: Enable revenue generation through tiered subscription plans for real estate and finishing companies, broadcast promotion quotas, and targeted coupon discounts.

### Key Milestones & Capabilities
- [ ] **Tiered Company Subscriptions**: Implementation of Free, Basic, Premium, and Enterprise subscription tiers.
- [ ] **Targeted Broadcast System**: Subscribed companies can publish broadcast announcements to followers in Arabic and/or English within monthly quota limits.
- [ ] **Coupon & Promo Engine**: Discount code generation, percentage/flat discounts, expiration limits.
- [ ] **Arabic Commercial Admin Panel**: Financial auditing, subscription tracking, and revenue analytics.

### Subscription Tiers

| Tier | Listings | Broadcasts | Price (EGP/month) |
|---|---|---|---|
| Free | 2 | 0 | Free |
| Basic | 10 | 5 | 299 |
| Premium | 50 | 20 | 799 |
| Enterprise | Unlimited | Unlimited | 1999 |

### Implementation Tasks

```java
@Entity
@Table(name = "subscription_plans")
public class SubscriptionPlan extends BaseEntity {
    
    @Column(name = "name_ar", nullable = false, length = 100)
    private String nameAr;
    
    @Column(name = "name_en", nullable = false, length = 100)
    private String nameEn;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "tier", nullable = false)
    private SubscriptionTier tier;
    
    @Column(name = "price", nullable = false, precision = 10, scale = 2)
    private BigDecimal price;
    
    @Column(name = "listing_quota", nullable = false)
    private Integer listingQuota;
    
    @Column(name = "broadcast_quota", nullable = false)
    private Integer broadcastQuota;
}
```

---

## 4. Phase 3: Aspirational & Future Platform Expansion (v2.0+)

**Objective**: Scale platform capabilities, enhance user onboarding experience, incorporate construction timeline tracking, and deploy asynchronous background job processing.

### Key Deliverables & Future Scope

#### 4.1 Identity & Analytics
- **Social Authentication**: Google OAuth 2.0 and Apple Sign-In integration alongside OTP auth.
- **Product Analytics Pipeline**: PostHog telemetry integration for tracking search conversion rates and user behavior metrics.

```java
// Future: Social Auth Integration
@Service
public class SocialAuthService {
    
    public AuthResponse authenticateWithGoogle(String idToken, Locale locale) {
        // Verify Google ID token
        GoogleIdToken.Payload payload = verifyGoogleToken(idToken);
        
        // Find or create user
        User user = findOrCreateUser(payload);
        
        // Generate JWT
        return jwtService.generateToken(user);
    }
}
```

#### 4.2 Property Construction Projects & Timelines
- **Major Projects Showcase**: Multi-unit compound and commercial project listings.
- **Property Construction Timelines**: Visual milestone progress tracking for buyers purchasing under-construction or finishing properties.

```java
@Entity
@Table(name = "projects")
public class Project extends BaseEntity {
    
    @Column(name = "name_ar", nullable = false, length = 200)
    private String nameAr;
    
    @Column(name = "name_en", length = 200)
    private String nameEn;
    
    @Column(name = "description_ar", columnDefinition = "nvarchar(max)")
    private String descriptionAr;
    
    @OneToMany(mappedBy = "project", cascade = CascadeType.ALL)
    private List<ProjectMilestone> milestones;
    
    @Column(name = "completion_percentage")
    private Integer completionPercentage;
}
```

#### 4.3 Infrastructure & Performance Enhancements
- **Asynchronous Background Job Processing**: Spring Batch or external queue (Redis/RabbitMQ) integration for processing scheduled broadcasts and emails.
- **Push Notification Infrastructure**: Integration with Firebase Cloud Messaging (FCM) and Apple Push Notification service (APNs).
- **Native Mobile Applications**: Mobile SDK integration for iOS and Android clients with bilingual UI switching.

---

## 5. Timeline Overview

| Phase | Version | Target Date | Key Deliverables |
|---|---|---|---|
| Phase 1 | v1.0 | Q1 2026 | Core MVP Parity |
| Phase 2 | v1.1 | Q2 2026 | Subscriptions & Broadcasts |
| Phase 2 | v1.2 | Q3 2026 | Coupons & Analytics |
| Phase 3 | v2.0 | Q4 2026 | Social Auth & Projects |
| Phase 3 | v2.1 | Q1 2027 | Background Jobs & Push |

---

## 6. Success Metrics by Phase

### Phase 1 Metrics
- User Registration Rate
- Property Listing Volume
- Appointment Completion Rate
- Search Response Time (< 400ms)

### Phase 2 Metrics
- Subscription Conversion Rate (>15%)
- Monthly Recurring Revenue (MRR)
- Broadcast Engagement Rate
- Coupon Redemption Rate

### Phase 3 Metrics
- Social Auth Adoption Rate
- Project Listing Volume
- Push Notification Engagement
- Background Job Success Rate

---

## 7. Risk Mitigation

| Risk | Mitigation |
|---|---|
| Database Performance | Index optimization, query tuning, caching |
| OTP Delivery Failures | Fallback providers, retry logic |
| Subscription Fraud | Payment verification, quota enforcement |
| Arabic Text Search | SQL Server FTS with Arabic language support |
| Broadcast Abuse | Quota limits, content moderation |
