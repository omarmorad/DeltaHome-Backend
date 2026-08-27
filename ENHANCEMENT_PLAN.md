# DeltaHome-Backend — Enhancement Plan

> Scope: architecture, correctness, scalability, and code-quality improvements.
> Out of scope (intentionally deferred until production prep): hardcoded credentials,
> dev backdoors (admin OTP, default admin password, seeder), and profile separation.
> These are acceptable for the current testing phase.

---

## Phase 1 — Database Foundation (do first)

Everything else builds on a stable schema strategy. Doing this later means rewriting
migrations for every new module.

### 1.1 Introduce Flyway migrations
- [ ] Create `src/main/resources/db/migration/V1__baseline.sql`
      - Generate from current schema: `pg_dump --schema-only` against a DB created by `ddl-auto: update`.
- [ ] Add explicit constraints that Hibernate doesn't create:
      - Unique index on `conversation (user_one_id, user_two_id)` ordered pair (fixes chat race, see 4.2)
      - Indexes on all FK columns used in filters: `property(city_id)`, `property(district_id)`, `property(status)`, `otp_code(identifier, purpose)`, `saved_item(user_id)`
- [ ] Switch `spring.jpa.ddl-auto` → `validate`, set `spring.flyway.enabled: true`
- [ ] Verify app boots clean on an empty database using only migrations

**Acceptance:** fresh DB + `mvn spring-boot:run` produces full schema via Flyway only; Hibernate validate passes.

---

## Phase 2 — Auth & OTP Correctness

### 2.1 Fix OTP send-rate limit (broken)
**File:** `service/OtpService.java` (`issueOtp`, ~lines 178–202)
- **Bug:** sends are counted *before* previous codes are deleted; each issue deletes its
  predecessors, so at most one row ever exists → `maxSendsPerWindow=5` is unreachable.
- **Fix options (pick one):**
  - A (minimal): count sends in a separate table / separate column that isn't deleted with codes.
  - B (better): keep a per-identifier counter in Redis with a 15-min TTL (`otp:sends:{identifier}:{purpose}`).
- [ ] Implement chosen fix
- [ ] Enforce `resendCooldownSeconds` the same way (it has the same counting bug)
- [ ] Add test: issuing 6th OTP within window returns 429/BusinessException

### 2.2 Consume OTP on successful verification
**File:** `service/OtpService.java` (`verify*` methods ~lines 228–247)
- [ ] On successful verify, mark code consumed (or delete it) instead of leaving it replayable
- [ ] Increment attempt counter even when the code was already verified (currently guesses are free)
- [ ] Test: same code cannot verify twice

### 2.3 Fix `changePassword` identifier lookup
**File:** `service/AuthService.java:228`
- [ ] Replace `findByPhone` with `findByPhoneOrEmail` (same as `me()` uses, line ~270)
- [ ] Add newPassword length/strength validation (min 8 chars) in `RegisterRequest` + change/reset flows

### 2.4 Refresh token rotation
**File:** `service/AuthService.java:207–216`
- [ ] Issue a new refresh token on every `/refresh` (rotation); embed a `jti` claim
- [ ] Store active refresh `jti` per user (DB or Redis); reject reused/old jti → detects theft
- [ ] Reduce access-token lifetime from 24h → 15–60 min (config only, safe change)
- [ ] `/logout` clears the stored jti so refresh dies immediately (access token still dies by expiry)

### 2.5 Revoke tokens on password reset
**Files:** `AuthService.resetPasswordByEmail`, `resetPassword`
- [ ] Bump a per-user `tokenVersion` (add claim to JWT; filter compares against DB) or delete stored refresh jti
- [ ] Old access tokens become invalid immediately after password change

### 2.6 Minor auth cleanups
- [ ] Equalize timing on email-login failure (run BCrypt against a dummy hash when user not found) — `AuthService.java:104–113`
- [ ] Remove duplicated `sendEmailOtp/verifyEmail/consumeEmail` wrappers (`OtpService.java:152–174`, `AuthService.java:44–70`) — keep unified methods only
- [ ] Return typed result instead of `null` sentinel in admin OTP path (`OtpService.java:103`)
- [ ] Log auth-filter failures at DEBUG instead of silent catch (`JwtAuthenticationFilter.java:52–56`)

---

## Phase 3 — DTO Layer Completion (entities never cross controllers)

Rule going forward: every request body is a validated DTO, every response is a DTO/projection.

### 3.1 Property module
- [ ] `PropertyDetailResponse` (replaces raw `Property` in `getProperty`) with owner/city/district mapped explicitly
- [ ] `CreatePropertyRequest` with `@Valid` + Bean Validation annotations; map to entity in service
- [ ] Whitelist server-controlled fields (status, isFeatured, owner) — kills mass assignment (`PropertyService.java:52–57`)
- [ ] Add `updateProperty`/`deleteProperty` endpoints **with ownership check** (`owner == currentUser || ADMIN`) — currently dead code without check

### 3.2 Chat module
- [ ] `ConversationDetailResponse`, `MessageResponse`; stop returning `Conversation`/`Message` entities (`ChatController.java:49,58,76`)
- [ ] Fix "other user" logic: pick `userTwo` when viewer is `userOne`, else `userOne` (`ChatService.java:157–158`)

### 3.3 Admin module
- [ ] `FraudFlagSummary`, `CouponSummary` projections to match existing summary pattern (`AdminController.java:69–81`)
- [ ] CompanyController/SavedItemController: replace inline user lookup with shared `UserContext.currentUser` (dedupe)

### 3.4 Pagination guard
**File:** `util/PageUtils.java`
- [ ] Clamp page size (e.g., max 100) in `normalizeSort`/normalization path; apply everywhere `Pageable` is accepted

---

## Phase 4 — Service-Layer Robustness

### 4.1 Move external I/O out of transactions
**File:** `service/OtpService.java:214`
- [ ] Send email/SMS **after commit** via `TransactionSynchronizationManager.registerSynchronization(afterCommit)` or split issue/persist/send steps
- Prevents holding Hikari connections during SMTP latency under load.

### 4.2 Fix conversation creation race
**File:** `service/ChatService.java:41–49`
- [ ] Normalize pair ordering (lower UUID first), rely on unique constraint from Phase 1.1, catch constraint violation → re-fetch

### 4.3 Transaction annotations hygiene
- [ ] Add `readOnly = true` to read-path service methods (`AuthService.me()`, `CompanyService.index`, etc.)

### 4.4 Search scalability
**File:** `repository/PropertyRepository.java:33` — `LIKE '%q%'` won't scale
- [ ] Short term: pg_trgm GIN index on title/description (add to migration)
- [ ] Medium term: wire Meilisearch (already in config) as the search backend; keep Postgres FTS (`SearchVectorInitializer` already exists) as fallback

---

## Phase 5 — Finish Stubs & Half-Built Features

- [ ] **Admin verification decision**: implement real flow — load Verification, apply approve/reject, persist, audit-log the admin action (`AdminController.decideVerification:114–122` currently echoes input)
- [ ] **Company property/company update+delete endpoints**: add with ownership checks (currently missing entirely)
- [ ] Remove dead code: `PropertyService.updateProperty` (or wire it per above), `CompanyService.getVerifiedCompanies`
- [ ] Verify integrations that exist in config but may not be wired: Redis cache, Twilio SMS, S3 storage, Firebase push — list which are wired vs placeholder before building features that depend on them

---

## Phase 6 — Testing

Currently 2 test files total. Target minimum before building next modules:

- [ ] **Auth integration tests** (Testcontainers + Postgres): register-email, login, refresh rotation, wrong-password lockout behavior, password reset invalidates old tokens
- [ ] **OTP unit tests**: rate limit enforced, cooldown enforced, expiry, max attempts, consume-on-verify
- [ ] **Property CRUD integration tests**: create (auth required), list filters + pagination cap, detail 404
- [ ] **Chat tests**: other-user correctness, duplicate-conversation prevention
- [ ] Add `@ActiveProfiles("test")` + `application-test.yml` (H2/Testcontainers, no mail, no seeder)
- [ ] Wire into CI later (out of scope now)

---

## Suggested Execution Order

| Order | Phase | Why this order |
|---|---|---|
| 1 | Phase 1 | Schema strategy blocks everything; cheapest to do while modules are few |
| 2 | Phase 2 | Security-correctness bugs compound as more flows use OTP/JWT |
| 3 | Phase 3 | New modules should copy the finished DTO pattern |
| 4 | Phase 4 | Can be done incrementally alongside Phase 3 |
| 5 | Phase 5 | Feature completion once patterns are stable |
| 6 | Phase 6 | Tests written alongside phases 2–5 ideally; at minimum before next feature modules |

## Definition of Done (per task)
- Code follows existing conventions (summary projections, PageUtils sorting, BusinessException + GlobalExceptionHandler)
- No new raw-entity controller returns
- Relevant test added/updated
- `mvn clean verify` green
