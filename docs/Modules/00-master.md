# Delta Homes — 00 · Master Spec (Global Contract, NFRs, Build Order)

> **Read this file once, fully, before touching any code.** Every stage spec in this
> folder references the rules defined here. This file is the contract; stage files are
> the blueprint.

**Status:** Contract · **Applies to:** every stage · **Dependencies:** none

---

## 1. Product summary

Multi-sided Egyptian home-lifecycle marketplace. Users discover properties, hire
companies, book viewings, chat, follow, save, review, and receive broadcasts. Companies
pay for plans granting listing/broadcast quotas. Admin console manages users, reports,
verifications, fraud, coupons, payments, subscriptions, and audit logs.

The target is **parity with the existing Java backend's API surface and data model** (Microsoft SQL Server database, `uniqueidentifier` PKs, phone+email OTP auth, JWT, SQL Server Full-Text Catalogs), with **bilingual localization (Arabic & English)** for mobile apps and an **Arabic-first Admin Dashboard (`ar-EG`, RTL)**.

---

## 2. Architecture (deliberately simple)

### 2.1 Project Structure

```
deltahomes-backend/
├── src/main/java/com/deltahomes/
│   ├── controller/           # REST Controllers (@RestController)
│   ├── service/              # Business Logic Services (@Service)
│   ├── repository/           # Spring Data JPA Repositories
│   ├── entity/               # JPA Entities
│   ├── dto/                  # Data Transfer Objects (records/POJOs)
│   ├── enums/                # Enumerations
│   ├── security/             # Security config, JWT, User Context
│   ├── exception/            # Custom Exceptions
│   ├── config/               # Configuration Classes
│   └── util/                 # Utilities (Paging, Sorting)
├── src/main/resources/
│   ├── messages_ar.properties # Arabic messages
│   ├── messages_en.properties # English messages
│   ├── application.yml        # Configuration
│   └── db/migration/          # Flyway/Liquibase migrations
└── src/test/java/com/deltahomes/ # Tests
```

### 2.2 Layering rules (keep it simple)

- **Controllers are thin:** no repository access, no business rules in controllers. Parse/bind
  input → call one service method → shape the result. Controllers may do trivial
  mapping only.
- **Services own the logic:** validation of business state, state machines, quota
  enforcement, OTP flows, transactions. Services are Spring-managed beans with `@Service`,
  prototype or request scope as needed.
- **Spring Data JPA repositories** are accessed directly from services. No generic
  repository abstraction, no UoW wrapper, no mediator/CQRS.
- **Entities are JPA entities** (`@Entity`), grouped in packages by domain. Use field access
  and keep annotations on fields. Use `@MappedSuperclass` for `BaseEntity`.
- **DTOs are Java records** in the `dto` package, named for their wire purpose
  (`LoginRequest`, `AuthResponse`, `PropertySummary`, ...).
- **Enums are Java enums** in the `enums` package; all mapped as string in JPA.

### 2.3 Package layout (inside `com.deltahomes`)

```
com.deltahomes/
├── controller/               # one controller per module
├── service/                  # one service per module
├── repository/               # one repository per entity
├── entity/                   # BaseEntity + 40 entities, grouped by domain
├── enums/                    # all 21 enums
├── dto/                      # records: request + response + summary projections
├── config/                   # SecurityConfig, LocaleConfig, JpaConfig, etc.
├── security/                 # JwtService, JwtConfig, CurrentUserAccessor
├── exception/                # BusinessException, ResourceNotFoundException, GlobalExceptionHandler
├── util/                     # PageResponse, SortNormalizer, constants
└── DeltaHomesApplication.java # Main application class
```

> If a new stage proposes a *new* package, convention, or infrastructure piece,
> it must be justified in the stage spec or it is rejected.

---

## 3. Technology stack

| Concern | Choice |
|---|---|
| Runtime | Java 17 (LTS), OpenJDK |
| API framework | Spring Boot 3.x, Spring Web MVC |
| ORM | Spring Data JPA + Hibernate |
| Migrations | Flyway or Liquibase |
| Validation | Jakarta Bean Validation (`@NotNull`, `@Pattern`, `@Size`, `@Email`) |
| JSON | Jackson: camelCase, ISO-8601, `JsonInclude.Include.NON_NULL` |
| Auth | Spring Security + jjwt library |
| OTP hashing | SHA-256 hex (`MessageDigest`), constant-time compare |
| Email | Spring Mail (JavaMail) with dev-mode log fallback |
| SMS | Twilio Java SDK with dev-mode log fallback |
| Dashboard | React or Vue.js (standalone, calls the API) |
| Tests | JUnit 5, Mockito, Testcontainers (SQL Server) |
| Caching | None in v1 (Redis is optional / future) |

**Base Package:** `com.deltahomes`

---

## 4. Project inventory

Controllers: `AdminController`, `AppointmentController`, `AuthController`, `BroadcastController`,
`ChatController`, `CompanyController`, `LookupController`, `PropertyController`,
`ReviewController`, `SavedItemController`, `SearchController`.

Services: `AuthService`, `OtpService`, `PropertyService`, `CompanyService`,
`FollowService`, `SavedItemService`, `ReviewService`, `LookupService`, `ChatService`,
`AppointmentService`, `BroadcastService`, `AdminService`, `NotificationService`,
`EmailService`, `SmsService`, `CurrentUserAccessor`.

Entities: ~40 JPA entities (see data model documentation).

---

## 5. Global wire contract (mandatory, everywhere)

### 5.1 Routes
- All routes under `/api/v1/...` — standard REST conventions.
- Base URL configured via `app.base-url` (default `http://localhost:8080`).

### 5.2 Pagination (0-based, Spring-style)
Request:
```
?page=0&size=20&sort=createdAt,desc&sort=price,asc
```
- `page` is **0-based**. `size` default is 20 (chat messages 50). Multiple `sort` params
  allowed. Sort property names are camelCase → normalized to snake_case
  (`SortNormalizer`) and **whitelisted** against the target column before SQL.
- Response envelope (exact field names):
```json
{ "content": [ ... ], "page": 0, "size": 20, "totalElements": 132, "totalPages": 7, "hasNext": true }
```
- Java:
```java
public record PageResponse<T>(
    List<T> content,
    int page,
    int size,
    long totalElements,
    int totalPages,
    boolean hasNext
) {
    public static <T> PageResponse<T> from(Page<T> page) {
        return new PageResponse<>(
            page.getContent(),
            page.getNumber(),
            page.getSize(),
            page.getTotalElements(),
            page.getTotalPages(),
            page.hasNext()
        );
    }
}
```

### 5.3 Error response shapes (exact)
| HTTP | Trigger | Body |
|---|---|---|
| 400 | `BusinessException` | `{"timestamp":"...","status":400,"error":"<message>"}` |
| 400 | Validation error | `{"timestamp":"...","status":400,"error":"VALIDATION_ERROR","validationErrors":{"<field>":"<msg>"}}` |
| 400 | Malformed JSON body | `{"timestamp":"...","status":400,"error":"Malformed request body: <detail>"}` |
| 400 | Type mismatch on query/path param | `{"timestamp":"...","status":400,"error":"Invalid value for parameter '<name>': <value>"}` |
| 401 | No/invalid token | `{"timestamp":"...","status":401,"error":"Unauthorized","message":"Authentication is required to access this resource"}` |
| 403 | Forbidden | `{"timestamp":"...","status":403,"error":"Forbidden","message":"You do not have permission to perform this action"}` |
| 404 | `ResourceNotFoundException` | `{"timestamp":"...","status":404,"error":"<Resource> not found with id: <id>"}` |
| 405 | Method not allowed | `{"timestamp":"...","status":405,"error":"<message>"}` |
| 500 | Unhandled | `{"timestamp":"...","status":500,"error":"An unexpected error occurred"}` |

- **Never** expose stack traces in 500s.
- Implemented by `GlobalExceptionHandler` with `@RestControllerAdvice`.

### 5.4 JSON serialization
- camelCase property names.
- ISO-8601 datetimes (`OffsetDateTime` or `Instant`), **no** unix timestamps.
- Omit null fields on responses; request DTOs still accept missing optional fields.
- Enums serialize/deserialize by **name** (uppercase).

### 5.5 Status codes
- POST create → 200 (exceptions: saved-items 201, register 201); DELETE → 204 unless
  noted; PATCH/PUT → 200.
- IDs are `UUID` everywhere.

---

## 6. Security model (global)

### 6.1 JWT Bearer
- Header `Authorization: Bearer <accessToken>`.
- Access token lifetime 86400s (24h); refresh 7 days. `expiresInSeconds` = accessMs/1000.
- Claims: `sub` = subject (phone, else email); `userId` (UUID string); `role` (enum name);
  `type` = `"access"` | `"refresh"` (must be validated so they can't be swapped).
- `tokenType` in responses = `"Bearer"`.
- `jwt.secret` from config; key ≥ 32 bytes for HS256.
- Validate: signature, expiry, `type`, and (for access) `sub == principal.username`.
- **No token rotation/blacklist.** Refresh tokens remain valid until expiry.

### 6.2 Principal → user resolution
- Resolve user by phone-or-email: `findByPhone(sub)` else `findByEmail(sub)`; neither →
  `BusinessException("User not found")`. Implemented in `CurrentUserAccessor`.
- JWT principal username = `phone ?? email`; authority = `ROLE_<Role>`; account is
  disabled unless `status == ACTIVE`.

### 6.3 Authorization matrix (exact)
| Route pattern | Rule |
|---|---|
| `POST /api/v1/auth/logout`, `GET /auth/me`, `PUT /auth/password` | Authenticated |
| `GET /api/v1/properties`, `/properties/{id}` | Permit all |
| `GET /api/v1/companies`, `/companies/{id}` | Permit all |
| `GET /api/v1/cities`, `/districts`, `/services`, `/features`, `/plans` | Permit all |
| `GET /api/v1/reviews`, `/reviews/{id}` | Permit all |
| `GET /api/v1/search` | Permit all |
| `/api/v1/admin/**` | Role `ADMIN` |
| everything else | Authenticated |

Role-based auth applies **only** to `/api/v1/admin/**`; other protected endpoints are
plain `@PreAuthorize("isAuthenticated()")`.

### 6.4 Password & OTP hashing
- Passwords: `BCryptPasswordEncoder` with **strength 10** (interops with
  Spring Security default).
- OTP codes: 6-digit random; stored only as **SHA-256 lowercase hex**; verified with
  `MessageDigest.isEqual` for constant-time comparison.

### 6.5 CORS
- Allowed origins from `app.cors.allowed-origins` (default `http://localhost:3000`,
  comma-separated), applied to `/api/**`, allow credentials, methods
  GET/POST/PUT/PATCH/DELETE/OPTIONS, headers `*`.

### 6.6 Secrets
- All secrets via env vars / config, never hard-coded, never logged.

---

## 7. Non-functional requirements (apply to every stage)

### 7.1 Performance
- Read endpoints: P95 < 300 ms; write endpoints: P95 < 500 ms (excluding external
  SMS/email delivery, which must never block the request beyond a timeout).
- Every list/search endpoint is index-backed; FTS uses SQL Server FULLTEXT indexes.
- Guard `size` > 100 → clamp or 400 (choose per endpoint, document it).
- **No N+1:** summaries are produced with projections / single native SQL queries.
- **No lazy-loading on read paths:** use `fetch join` or DTO projections.

### 7.2 Security (OWASP-aligned)
- SQLi: all dynamic SQL is parameterized; `SortNormalizer` whitelist before `ORDER BY`.
- AuthN/AuthZ per §6. XSS: Frontend framework escapes by default.
- CSRF: JWT-in-header auth is CSRF-safe; keep it header-based, not cookies.
- Rate limiting: OTP endpoints (see `02-auth.md`); optional global limiter behind config.
- Input validation on every body; reject unknown enum values with the standard 400
  type-mismatch shape.
- Audit logging for all admin actions (see `13-admin.md`).
- No stack traces, no secrets, no PII in logs beyond masked OTP recipients.

### 7.3 Reliability & resilience
- Startup migrations are **idempotent** (`IF NOT EXISTS`, deterministic seed UUIDs) so
  restarts are safe.
- External channel failure (Twilio/SMTP) degrades to dev-mode logging **or** a 400 with
  the documented message — never a 500 and never a crash.
- Multi-row writes (e.g. follow + counter, register + consume OTP) run in one
  transaction (`@Transactional`).
- Stateless API: horizontal scale is just more instances behind a load balancer.

### 7.4 Observability
- Structured logging (JSON format) with: request id, HTTP method/path/status,
  user id (when authenticated), stage name. OTP codes logged only in dev mode and masked
  otherwise (`xxxx***yyy`).
- Health endpoint `GET /actuator/health` returning 200/503 for container probes.
- (Optional) Micrometer metrics for latency/error rates — config-gated, off by default.

### 7.5 Maintainability
- Stage specs map 1:1 to Java names → a reviewer can diff behavior against the source.
- One responsibility per service; controllers stay thin; no duplicated rule in two
  services.
- DTOs are records; entities are JPA entities; mapping is explicit (no MapStruct dependency —
  keep it simple, write the few-line maps by hand).

### 7.6 Data governance & compliance
- Egyptian phone validation (`^01[0-9]{9}$`) at the boundary.
- PII minimization: summaries never include hashes or sensitive fields.
- OTP rows deleted on consume/expiry (single-use, no replay).
- Soft delete via `status = DELETED` where the model supports it; audit logs retained.

### 7.7 Testability
- Dev-mode OTP/SMS/email fallbacks make the full flow testable with zero credentials.
- Deterministic seed (fixed UUIDs) for repeatable integration tests.
- Tests must not depend on network or external services (Testcontainers spins up
  SQL Server locally).

### 7.8 Extensibility (future-proofing without building it)
- Generic `entity_type` + `entity_id` pattern for saved/review/follow/report/notification/
  broadcast-delivery — adding a new entity type needs no schema change.
- Lookup tables (cities/services/plans) and feature flags are CMS-editable in the admin
  dashboard, not code enums.

---

## 8. Persistence conventions (JPA/Hibernate)

- `BaseEntity`: `UUID id` (generated in `@PrePersist` with `UUID.randomUUID()` — matches
  Java pattern; no `NEWID()` default dependency), `OffsetDateTime createdAt`,
  `OffsetDateTime updatedAt`.
- `createdAt` set on insert, `updatedAt` changed on every save — via JPA
  `@EntityListeners(AuditingEntityListener.class)` with `@CreatedDate` and `@LastModifiedDate`. Never set by application code.
- Every enum property: `@Enumerated(EnumType.STRING)` (TEXT column), name-based.
- JSON columns (`features`, `images`, `payload`, `coverage_area`, `evidence_urls`,
  `rollout_scope`): Java `String`, mapped to `nvarchar(max)`.
- Polymorphic targets (reviews/saved/followers/reports/fraud flags/notifications):
  **no FK constraint** — validated at the application layer.
- Composite uniques: `@Table(uniqueConstraints = {...})` for
  `saved_items (user_id, entity_type, entity_id)`, `followers (user_id, company_id)`,
  `follower_preferences (user_id, company_id)`, `company_services (company_id, service_id)`.
- Lazy loading **disabled** (`fetch = FetchType.LAZY` default). Navigations exist for writes; read paths use projections.

### 8.1 Full-text search
Created idempotently by Flyway/Liquibase migrations: one default FULLTEXT
catalog plus one FULLTEXT index per searchable table:
```sql
IF NOT EXISTS (SELECT * FROM sys.fulltext_catalogs WHERE name = 'DeltaHomesFtsCatalog')
    CREATE FULLTEXT CATALOG DeltaHomesFtsCatalog AS DEFAULT;

IF NOT EXISTS (SELECT * FROM sys.fulltext_indexes WHERE object_id = OBJECT_ID('<table>'))
    CREATE FULLTEXT INDEX ON <table>(<fts_columns>)
        KEY INDEX PK_<table> ON DeltaHomesFtsCatalog;
```
Indexed columns (the text columns that made up each search expression), for these 15
tables — see data model documentation for the full list:
`properties`, `companies`, `cities`, `districts`, `services`, `features`,
`subscription_plans`, `users`, `broadcasts`, `reviews`, `notifications`, `coupons`,
`reports`, `audit_logs`, `messages`. Bilingual text is indexed with both `LANGUAGE 1025`
(Arabic) and `LANGUAGE 1033` (English) entries where applicable.

Search query shape (native SQL):
```sql
... WHERE (:q = N'' OR CONTAINS(t.*, :q)) AND <filters>
```
(`CONTAINS(t.*, :q)` searches every FULLTEXT-indexed column of the table; prefix the
term with `"<q>*"` for prefix matching.) plus a matching `SELECT count(*)` for totals.
**`CONTAINS` is T-SQL-specific — keep it.**

---

## 9. Configuration schema (application.yml)

```yaml
server:
  port: 8080

spring:
  application:
    name: delta-homes-backend
  datasource:
    url: jdbc:sqlserver://localhost:1433;databaseName=DeltaHomesDb;encrypt=true;trustServerCertificate=true
    driver-class-name: com.microsoft.sqlserver.jdbc.SQLServerDriver
  jpa:
    database-platform: org.hibernate.dialect.SQLServerDialect
    hibernate:
      ddl-auto: validate
  flyway:
    enabled: true
    locations: classpath:db/migration

jwt:
  secret: ${JWT_SECRET}
  expiration-ms: 86400000
  refresh-expiration-ms: 604800000

app:
  base-url: ${APP_BASE_URL:http://localhost:8080}
  cors:
    allowed-origins: ${CORS_ORIGINS:http://localhost:3000}
  otp:
    expiry-minutes: 5
    max-attempts: 5
  admin:
    phone: ${ADMIN_PHONE:}
    password: ${ADMIN_PASSWORD:admin123}

mail:
  host: ${MAIL_HOST:}
  port: 587
  username: ${MAIL_USERNAME:}
  password: ${MAIL_PASSWORD:}

twilio:
  account-sid: ${TWILIO_ACCOUNT_SID:}
  auth-token: ${TWILIO_AUTH_TOKEN:}
  phone-number: ${TWILIO_PHONE_NUMBER:}
```

Env variables: `JWT_SECRET`, `APP_BASE_URL`, `CORS_ORIGINS`, `ADMIN_PHONE`,
`ADMIN_PASSWORD`, `MAIL_HOST`, `MAIL_USERNAME`, `MAIL_PASSWORD`, `TWILIO_ACCOUNT_SID`,
`TWILIO_AUTH_TOKEN`, `TWILIO_PHONE_NUMBER`.

---

## 10. Enums (21) — values (all stored as string, matched by name)

| Enum | Values |
|---|---|
| `AppointmentStatus` | PENDING, ACCEPTED, REJECTED, COMPLETED, CANCELLED |
| `BroadcastType` | NEW_PROPERTY, OFFER, DISCOUNT, VIDEO, NEWS |
| `CompanyType` | REAL_ESTATE_OFFICE, FINISHING_COMPANY, MAINTENANCE_PROVIDER |
| `EntityType` | PROPERTY, COMPANY, TECHNICIAN, SERVICE_PROVIDER |
| `FinishingLevel` | UNFINISHED, SEMI_FINISHED, FINISHED, LUXURY |
| `FraudFlagType` | DUPLICATE_LISTING, FAKE_REVIEW, SPAM_MESSAGE, ABNORMAL_FOLLOW_ACTIVITY |
| `HideReason` | INAPPROPRIATE_PHOTOS, INCORRECT_DATA, DUPLICATE, FRAUD, ALREADY_SOLD |
| `MessageType` | TEXT, IMAGE, LOCATION, VOICE, PDF, PROPERTY_CARD, APPOINTMENT_CARD |
| `NotificationType` | SYSTEM, MARKETING, PERSONAL, SMART |
| `OtpPurpose` | REGISTRATION, LOGIN, PASSWORD_RESET |
| `PaymentStatus` | PENDING, PAID, FAILED, REFUNDED |
| `PropertyPurpose` | SALE, RENT |
| `PropertyStatus` | DRAFT, PENDING_REVIEW, PUBLISHED, HIDDEN, SOLD, RENTED, ARCHIVED |
| `Readiness` | READY, UNDER_CONSTRUCTION |
| `ReportStatus` | OPEN, REVIEWING, RESOLVED, DISMISSED |
| `SubscriptionStatus` | ACTIVE, EXPIRED, CANCELLED |
| `SubscriptionTier` | BASIC, PREMIUM, ENTERPRISE |
| `UserRole` | CUSTOMER, OWNER, OFFICE, COMPANY, TECHNICIAN, ADMIN |
| `UserStatus` | ACTIVE, SUSPENDED, PENDING, DELETED |
| `VerificationStatus` | PENDING, ACCEPTED, REJECTED |
| `VerificationType` | PHONE, EMAIL, NATIONAL_ID, COMMERCIAL_REGISTRY |

---

## 11. Summary projections (list/search DTOs — flat records, never leak owner/entity)

| DTO | Fields |
|---|---|
| `PropertySummary` | id, title, description, price, purpose, category, status, cityName, districtName, street, readiness, finishingLevel, isFeatured, features, createdAt |
| `CompanySummary` | id, name, type, logoUrl, coverUrl, phone, whatsapp, email, website, verified, followersCount, reputationScore |
| `UserSummary` | id, name, phone, email, role, status, verificationLevel, createdAt, lastLoginAt |
| `ReviewSummary` | id, entityType, entityId, rating, comment, interactionVerified, reviewerName, createdAt |
| `AppointmentSummary` | id, status, requestedSlot, note, createdAt, propertyId, propertyTitle, customerName, ownerName |
| `ConversationSummary` | id, lastMessagePreview, updatedAt, otherUserId, otherUserName |
| `MessageSummary` | id, type, textBody, mediaUrl, payload, senderName, createdAt |
| `NotificationSummary` | id, title, body, type, entityType, entityId, isRead, createdAt |
| `ReportSummary` | id, entityType, entityId, reason, status, decision, reporterName, createdAt |
| `VerificationSummary` | id, type, status, documentUrl, rejectionReason, reviewedAt, createdAt, userName, reviewedByName |
| `PaymentSummary` | id, amount, method, status, gatewayReference, subscriptionId, createdAt |
| `SubscriptionSummary` | id, status, startDate, endDate, createdAt, userId, companyId, planName |
| `AuditLogSummary` | id, action, targetType, targetId, ipAddress, reason, adminName, createdAt |
| `BroadcastSummary` | id, title, body, type, createdAt, companyId, companyName |

---

## 12. Build order & stage map

| # | Stage file | Module | Depends on | Effort |
|---|---|---|---|---|
| 0 | `01-foundation.md` | Foundation & cross-cutting | — | M |
| 1 | `02-auth.md` | Auth & Identity | 0 | L |
| 2 | `03-lookups.md` | Lookups | 0 | S |
| 3 | `04-properties.md` | Properties | 0,1,2 | L |
| 4 | `05-companies.md` | Companies & Follow | 0,1,2 | L |
| 5 | `06-saved-items.md` | Saved Items | 0,1 | S |
| 6 | `07-reviews.md` | Reviews | 0,1 | M |
| 7 | `08-appointments.md` | Appointments | 0,1,3 | M |
| 8 | `09-chat.md` | Chat | 0,1,3,4 | M |
| 9 | `10-broadcasts.md` | Broadcasts | 0,1,4 | M |
| 10 | `11-search.md` | Search | 0 | S |
| 11 | `12-notifications.md` | Notifications | 0,1 | S |
| 12 | `13-admin.md` | Admin API + Dashboard | 0,1,2..11 | L |
| 13 | `14-commerce.md` | Commerce (plans/subscriptions/payments/coupons) | 0,1,12 | L |
| 14 | `15-social-analytics.md` | Social login + PostHog analytics | 1 | L |
| 15 | `16-projects-timeline.md` | Projects + Property Timeline | 3 | L |
| 16 | `17-background-jobs.md` | Background job queue | 12+ | M |

Stages 14–16 are **ASPIRATIONAL** (product spec only, not implemented yet); build them
only after 0–13 are green.

---

## 13. Testing strategy (global)

- **Unit tests:** service logic with Mockito mocks — per stage list. Cover state machines, OTP flows,
  quota checks, validation, exact error messages.
- **Integration tests:** controllers + real SQL Server (Testcontainers). Cover every
  endpoint's happy path + each error shape. Seed via deterministic UUIDs.
- **Contract tests:** assert pagination envelope, JSON casing, null omission, and error
  body shapes on a representative endpoint per stage.
- **Dashboard:** component tests for login flow and each admin page's render
  against a mocked HTTP handler.
- Run: `./mvnw test` (or `./gradlew test`) after every stage; fix before moving on.

---

## 14. Definition of Done (applies to every stage)

1. All endpoints in the stage's spec implemented with the exact paths, methods, auth,
   request/response shapes, and status codes.
2. All entities/columns/indexes/constraints for the stage exist in JPA model + migrations,
   and FULLTEXT indexes are created idempotently where the stage owns a searchable table.
3. All business rules and exact error strings from the stage spec are implemented.
4. Stage tests are written and **green**; no existing tests broken.
5. `./mvnw clean package` (or `./gradlew build`) produces no warnings; tests pass.
6. OpenAPI shows the stage endpoints with correct contracts.
7. No secrets committed; config from env; docs updated only if a spec file says so.

---

## 15. Aspirational features (scope gate — do not build in stages 0–13)

Deferred and isolated in their own stages so the parity core
is never compromised: Google/Apple login, PostHog analytics, payment gateway checkout,
Projects, Property Timeline, background job queue, Meilisearch indexing, Firebase push.
If a parity stage references one of these, it must say "out of scope — see stage 14/15/16".
