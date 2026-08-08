# Delta Homes — Scoped Implementation Plan & Developer Roadmap

> **خطة تنفيذ مخصصة للـ API الخلفية ولوحة التحكم الإدارية باستخدام Spring Boot 3.x**  
> **Implementation plan limited to backend API and Arabic admin dashboard with Spring Boot 3.x**

---

## 1. Core Technology Requirements

- **Framework**: Spring Boot 3.x Web Application (Java 17+ LTS)
- **Admin Dashboard**: React or Vue.js, Arabic (`ar‑EG`) default, Right‑to‑Left layout.
- **Database**: Microsoft SQL Server 2022+ (Spring Data JPA + Hibernate).
- **Localization**: Spring LocaleResolver supporting `ar‑EG` and `en‑US`.
- **Authentication**: Spring Security + JWT + BCrypt password hashing, OTP via SMS/Email.
- **Migrations**: Flyway or Liquibase for database schema versioning.
- **Testing**: JUnit 5, Mockito, Testcontainers (SQL Server).

---

## 2. Stage Dependency Graph (Simplified)

```
0 Foundation (SQL Server + Localization) ──┐
│                                         │
├─ 1 Auth & Identity ──────────────────────┤
├─ 2 Lookups (Bilingual) ─────────────────┤
├─ 3 Property Listing (CRUD) ─────────────┤
├─ 4 Companies & Follow ────────────────────┤
└─ 12 Admin Dashboard (RTL) ───────────────┘
```

---

## 3. Stage‑by‑Stage Breakdown

### Stage 0: Foundation

- Create Spring Boot project `deltahomes-backend` with Maven/Gradle.
- Configure Spring Data JPA with SQL Server driver, connection string, and Flyway migrations.
- Add `LocaleConfig` for `ar‑EG` (default) and `en‑US`.
- Scaffold `.properties` resource bundles for Arabic and English strings.
- **DoD**: `./mvnw clean package` succeeds, migrations run, localization active.

### Stage 1: Auth & Identity

- Implement `AuthController`, `AuthService`, `JwtService`, `OtpService`, BCrypt password hashing.
- Endpoints: `POST /api/v1/auth/register`, `POST /api/v1/auth/login`, `POST /api/v1/auth/otp/request`, `POST /api/v1/auth/otp/verify`.
- Store `preferredCulture` per user.
- **DoD**: Successful registration/login with localized responses; OTP verification returns JWT.

### Stage 2: Lookups (Bilingual Data)

- Create `LookupController` returning lookup tables (Cities, Districts, Service Categories, Features) with `NameAr`/`NameEn`.
- Endpoints: `GET /api/v1/cities`, `GET /api/v1/districts`, etc.
- **DoD**: All lookup endpoints return data in requested culture via `Accept-Language` header.

### Stage 3: Property Listing Module

- Implement CRUD for properties with bilingual fields (`TitleAr`/`TitleEn`, `DescriptionAr`/`DescriptionEn`).
- Image upload handling, storing paths in SQL Server.
- Endpoints: `GET /api/v1/properties`, `POST /api/v1/properties`, `GET /api/v1/properties/{id}`, `PUT /api/v1/properties/{id}`, `DELETE /api/v1/properties/{id}`.
- **DoD**: Properties persisted with bilingual content; image upload works; validation passes.

### Stage 4: Companies & Follow

- `CompanyController` for company profiles (bilingual) and follow functionality.
- Endpoints: `GET /api/v1/companies`, `POST /api/v1/companies`, `GET /api/v1/companies/{id}`, `POST /api/v1/companies/{id}/follow`.
- **DoD**: Companies created with Arabic/English data; users can follow companies; follow status stored.

### Stage 12: Admin Dashboard (RTL)

- Scaffold React or Vue.js frontend project.
- Apply RTL layout (`dir="rtl"`) and Arabic UI strings.
- Integrate with API endpoints for admin tasks (user management, company verification, property moderation).
- Endpoints: `GET /api/v1/admin/users`, `PUT /api/v1/admin/verifications/{id}`, `GET /api/v1/admin/reports`.
- **DoD**: Admin UI loads, displays Arabic interface, performs CRUD actions via API, respects RTL styling.

---

## 4. Verification Plan

- **Automated Tests**: Run `./mvnw test` (or `./gradlew test`) for each module after every stage.
- **Manual Checks**: Verify Arabic/English responses via Postman; browse admin dashboard, confirm RTL layout.
- **Database Validation**: Ensure `nvarchar` columns store Arabic characters correctly.

The plan aligns with the focus on the backend API and Arabic admin dashboard using Spring Boot 3.x.

> **خطة التنفيذ خطوة بخطوة، المراحل، النطاق، معايير القبول.**  
> **17-stage modular implementation guide, Microsoft SQL Server migration, Arabic/English API localization, Arabic Admin (RTL), and DoD checklists.**

---

## 5. Incremental Build Protocol & Guardrails

- **Strict Order Execution**: Build system modules strictly in stage order (Stage 0 to Stage 16).
- **Self-Contained Stages**: Each stage must pass its unit/integration tests and satisfy its Definition of Done (DoD) before progressing to dependent stages.
- **Technology Requirements**:
  - Database: **Microsoft SQL Server 2022+** via `com.microsoft.sqlserver:mssql-jdbc`.
  - Localization: API handles **Arabic (`ar-EG`)** and **English (`en-US`)** for mobile apps via `LocaleResolver`.
  - Admin Dashboard: Built with React or Vue.js rendering in **Arabic** with **Right-to-Left (RTL)** styling (`dir="rtl"`).

---

## 6. Stage Dependency Graph

```
 0 Foundation (SqlServer + Localization) ─────────────────┐
 │                                                         │
 ├─ 1 Auth & Identity ──┐                                  │
 │                      ├─ 5 Saved Items      ──┐          │
 ├─ 2 Lookups (AR/EN) ──┤   ├─ 6 Reviews          ├─ 12 Admin (Arabic RTL) ─┴─ 13 Commerce ──┐
 ├─ 3 Properties ───────┴───┼─ 7 Appointments     │                                          ├─ 14 Social/Analytics
 │  (needs 2)               ├─ 4 Companies/Follow ────┼─ 8 Chat                       ├─ 15 Projects/Timeline
 │                          │                          ├─ 9 Broadcasts                 └─ 16 Background Jobs
 │                          │                          ├─ 10 Search (SQL FTS)
 │                          │                          └─ 11 Notifications
 └──────────────────────────┘
```

---

## 7. Stage-by-Stage Implementation Breakdown

### Stage 0: Foundation (SQL Server & Localization Setup)

- **Scope**: Initialize Spring Boot project `deltahomes-backend`. Configure Spring Data JPA with `mssql-jdbc`, setup `application.yml` SQL Server connection string, wire up `LocaleConfig` for `ar-EG` and `en-US`, configure `.properties` localized resource bundles, setup `GlobalExceptionHandler`, `PageResponse<T>` envelope, and `SortNormalizer`.
- **Key Files**:
  - `src/main/resources/application.yml`
  - `src/main/java/com/deltahomes/config/LocaleConfig.java`
  - `src/main/java/com/deltahomes/config/JpaConfig.java`
  - `src/main/java/com/deltahomes/exception/GlobalExceptionHandler.java`
  - `src/main/java/com/deltahomes/util/PageResponse.java`
  - `src/main/resources/db/migration/V1__Initial_Schema.sql`
- **DoD**: `./mvnw clean package` succeeds; Flyway migrations create SQL Server tables with `uniqueidentifier` PKs and `nvarchar` columns; `LocaleResolver` reads `Accept-Language` headers cleanly.

---

### Stage 1: Auth & Identity Module

- **Scope**: Implement `AuthController`, `AuthService`, `JwtService`, `OtpService`, password hashing (BCrypt via `BCryptPasswordEncoder`), phone/email OTP verification, user culture preference tracking, and localized auth response messages via `MessageSource`.
- **Key Files**:
  - `AuthController.java`, `AuthService.java`
  - `JwtService.java`, `JwtConfig.java`
  - `OtpService.java`
  - `CurrentUserAccessor.java`
  - `User.java`, `OtpCode.java`
  - `messages_ar.properties`, `messages_en.properties`
- **Endpoints**: `POST /api/v1/auth/register`, `POST /api/v1/auth/login`, `POST /api/v1/auth/otp/request`, `POST /api/v1/auth/otp/verify`, `POST /api/v1/auth/refresh`.
- **DoD**: User registration stores `preferredCulture`; OTP verification returns JWT tokens; error responses are localized in Arabic/English according to request culture.

---

### Stage 2: Lookups Module (Bilingual Data)

- **Scope**: Implement `LookupController` and `LookupService` returning bilingual lookup data (Cities, Districts, Service Categories, Features) with `nameAr` and `nameEn` fields.
- **Key Files**:
  - `LookupController.java`, `LookupService.java`
  - `City.java`, `District.java`, `Service.java`, `Feature.java`
  - `CityRepository.java`, `DistrictRepository.java`, etc.
- **Endpoints**: `GET /api/v1/cities`, `GET /api/v1/districts`, `GET /api/v1/services`, `GET /api/v1/features`.
- **DoD**: Endpoints return bilingual lookup data; client can request specific culture localized names.

---

### Stage 3: Property Listing Module

- **Scope**: Implement `PropertyController`, `PropertyService`, bilingual property CRUD (`titleAr`/`titleEn`, `descriptionAr`/`descriptionEn`), image upload, and location binding in SQL Server.
- **Key Files**:
  - `PropertyController.java`, `PropertyService.java`
  - `Property.java`, `PropertyImage.java`
  - `PropertyRepository.java`
  - `PropertySummary.java` (DTO)
- **Endpoints**: `GET /api/v1/properties`, `POST /api/v1/properties`, `GET /api/v1/properties/{id}`, `PUT /api/v1/properties/{id}`, `DELETE /api/v1/properties/{id}`, `POST /api/v1/properties/{id}/images`.
- **DoD**: Users can create listings with Arabic and English descriptions; images are linked; quota validation functions properly.

---

### Stage 4: Companies & Follow Module

- **Scope**: Implement `CompanyController`, `CompanyService`, `FollowService`, team member delegation (`OWNER`, `MANAGER`, `AGENT`), and bilingual company profile data.
- **Key Files**:
  - `CompanyController.java`, `CompanyService.java`
  - `FollowService.java`
  - `Company.java`, `Follower.java`
  - `CompanyRepository.java`
- **Endpoints**: `GET /api/v1/companies`, `POST /api/v1/companies`, `GET /api/v1/companies/{id}`, `POST /api/v1/companies/{id}/follow`, `DELETE /api/v1/companies/{id}/follow`.
- **DoD**: Companies are registered with Arabic/English details; users can follow companies; team roles function properly.

---

### Stage 5: Saved Items Module

- **Scope**: Implement `SavedItemController` and `SavedItemService` for bookmarking listings and saving search criteria in SQL Server.
- **Key Files**:
  - `SavedItemController.java`, `SavedItemService.java`
  - `SavedItem.java`
  - `SavedItemRepository.java`
- **Endpoints**: `GET /api/v1/saved-items`, `POST /api/v1/saved-items`, `DELETE /api/v1/saved-items/{id}`.
- **DoD**: Duplicate bookmark attempts return localized error messages; saved properties are paginated cleanly.

---

### Stage 6: Reviews Module

- **Scope**: Implement `ReviewController` and `ReviewService` for interaction-gated reviews with localized error envelopes.
- **Key Files**:
  - `ReviewController.java`, `ReviewService.java`
  - `Review.java`
  - `ReviewRepository.java`
- **Endpoints**: `GET /api/v1/reviews`, `POST /api/v1/reviews`, `GET /api/v1/reviews/summary/{entityType}/{entityId}`.
- **DoD**: Review submission enforces appointment completion check; ratings aggregate accurately in SQL Server.

---

### Stage 7: Appointments & Viewing Workflows

- **Scope**: Implement `AppointmentController` managing viewing state transitions and localized status notification triggers.
- **Key Files**:
  - `AppointmentController.java`, `AppointmentService.java`
  - `Appointment.java`
  - `AppointmentRepository.java`
- **Endpoints**: `POST /api/v1/appointments`, `GET /api/v1/appointments/my`, `PUT /api/v1/appointments/{id}/status`.
- **DoD**: State machine transitions (`PENDING` ➔ `ACCEPTED` ➔ `COMPLETED`) operate correctly in SQL Server.

---

### Stage 8: Real-Time Chat & Communications

- **Scope**: Implement `ChatController` managing messaging rooms and chat history.
- **Key Files**:
  - `ChatController.java`, `ChatService.java`
  - `Conversation.java`, `Message.java`
  - `ConversationRepository.java`, `MessageRepository.java`
- **Endpoints**: `GET /api/v1/chat/conversations`, `POST /api/v1/chat/conversations`, `GET /api/v1/chat/conversations/{id}/messages`, `POST /api/v1/chat/conversations/{id}/messages`.
- **DoD**: Users can initiate inquiries; messages persist with UTF-8 Arabic/English support in `nvarchar` columns.

---

### Stage 9: Broadcast System

- **Scope**: Implement `BroadcastController` enforcing monthly subscription broadcast quotas.
- **Key Files**:
  - `BroadcastController.java`, `BroadcastService.java`
  - `Broadcast.java`, `BroadcastDelivery.java`
  - `BroadcastRepository.java`
- **Endpoints**: `POST /api/v1/broadcasts`, `GET /api/v1/broadcasts/my`.
- **DoD**: Broadcast creation deducts quota; zero remaining quota returns localized 402 error payload.

---

### Stage 10: Microsoft SQL Server Full-Text Search Module

- **Scope**: Implement `SearchController` and `SearchService` utilizing SQL Server Full-Text Catalogs and T-SQL `CONTAINS` queries over Arabic and English text columns.
- **Key Files**:
  - `SearchController.java`, `SearchService.java`
  - `SearchRepository.java`
  - `V10__Create_FullText_Indexes.sql` (Flyway migration)
- **Endpoints**: `GET /api/v1/search/properties`.
- **DoD**: Search endpoint executes `CONTAINS` queries across `titleAr`, `titleEn`, `descriptionAr`, and `descriptionEn`; returns relevant listings.

---

### Stage 11: Notifications Module

- **Scope**: Implement `NotificationService` generating localized in-app notifications based on recipient user's preferred language.
- **Key Files**:
  - `NotificationController.java`, `NotificationService.java`
  - `Notification.java`
  - `NotificationRepository.java`
- **Endpoints**: `GET /api/v1/notifications`, `PUT /api/v1/notifications/{id}/read`.
- **DoD**: Users receive notifications in their selected language (`ar-EG` or `en-US`).

---

### Stage 12: Arabic Admin Console (RTL Frontend)

- **Scope**: Implement `AdminController` in Web API and construct React or Vue.js frontend localized in **Arabic** with **Right-to-Left (RTL)** layout (`dir="rtl"`).
- **Key Files**:
  - `AdminController.java`, `AdminService.java`
  - `Admin Dashboard Frontend Project` (React/Vue.js)
  - RTL CSS styling
  - Arabic UI components
- **Endpoints**: `GET /api/v1/admin/users`, `PUT /api/v1/admin/verifications/{id}`, `GET /api/v1/admin/reports`, `GET /api/v1/admin/audit-logs`.
- **DoD**: Admin UI loads, displays Arabic interface, performs CRUD actions via API, respects RTL styling.

---

### Stage 13: Commerce & Subscriptions

- **Scope**: Implement plan subscriptions, coupon discount logic, and admin commercial tools in SQL Server.
- **Key Files**:
  - `CommerceController.java`, `CommerceService.java`
  - `SubscriptionPlan.java`, `Subscription.java`, `Payment.java`, `Coupon.java`
  - `SubscriptionRepository.java`, `PaymentRepository.java`
- **Endpoints**: `GET /api/v1/commerce/plans`, `POST /api/v1/commerce/subscribe`, `POST /api/v1/commerce/coupons/apply`.
- **DoD**: Subscriptions update company quotas; coupons apply discounts accurately in EGP.

---

### Stages 14–16: Aspirational Modules (Post-Parity Extensions)

- **Stage 14 (Social & Analytics)**: Google/Apple OAuth integration + PostHog event logging.
- **Stage 15 (Projects & Timelines)**: Property construction timelines and project milestones.
- **Stage 16 (Background Jobs)**: Async background job processing with Spring Batch or external queue.
