# Delta Homes (Dhomes) — Master Documentation & Specification Suite

> **تعريف المشروع وطريقة تشغيله والمتطلبات وأوامر التشغيل.**  
> **Project Overview, Setup Guide, System Requirements, and Specification Index.**

---

## 1. Project Overview (تعريف المشروع)

**Delta Homes** is a multi-sided Egyptian home-lifecycle marketplace. It provides an end-to-end digital platform where users can discover properties for sale or rent, hire specialized companies (real estate brokerages, interior design & finishing companies, maintenance providers), schedule property viewings, communicate via real-time chat, leave verified reviews, follow favorite service providers, and receive targeted broadcast notifications.

The system features:
- **Bilingual REST API**: Supports **Arabic (`ar-EG`)** and **English (`en-US`)** for mobile and web clients via Spring LocaleResolver and MessageSource.
- **Arabic Admin Dashboard**: Fully localized in **Arabic** with a **Right-to-Left (RTL)** layout for system administrators and moderators.
- **Database Engine**: Built on **Microsoft SQL Server 2022+** as the primary relational system of record with native SQL Server Full-Text Search.

---

## 2. Technology Stack & Requirements (التقنيات والمتطلبات)

### 2.1 System Stack
- **Backend API**: Spring Boot 3.x Web Application (Java 17+ LTS)
- **Admin Dashboard**: React or Vue.js — Arabic-first (`ar-EG`), Right-to-Left (RTL) interface
- **Database**: Microsoft SQL Server 2022+ / Azure SQL Database (with native `FULLTEXT INDEX` search catalogs)
- **Data Access**: Spring Data JPA + Hibernate
- **Localization**: Spring MessageSource (`ar-EG` default, `en-US` mobile client support) + `.properties` resource bundles
- **Authentication**: Spring Security with JWT Bearer Tokens & BCrypt hashing
- **Migrations**: Flyway or Liquibase
- **Communications / Integrations**: Spring Mail (SMTP), Twilio SMS SDK (Dev-mode logging fallbacks)
- **Testing**: JUnit 5, Mockito, Testcontainers (SQL Server)

### 2.2 Prerequisites
- [Java 17+ LTS (OpenJDK)](https://adoptium.net/)
- [Microsoft SQL Server 2022+ (Developer/Express Edition)](https://www.microsoft.com/sql-server/)
- [Maven 3.9+](https://maven.apache.org/) or [Gradle 8+](https://gradle.org/)
- IDE: IntelliJ IDEA / VS Code / Eclipse

---

## 3. Getting Started & Run Commands (طريقة تشغيل المشروع وأوامر التشغيل)

### 3.1 Environment Setup
1. Clone the repository and navigate to the project directory:
   ```bash
   git clone <repository-url>
   cd DeltaHome-Backend
   ```

2. Configure environment settings in `src/main/resources/application.yml`:
   ```yaml
   spring:
     datasource:
       url: jdbc:sqlserver://localhost:1433;databaseName=DeltaHomesDb;encrypt=true;trustServerCertificate=true
       username: sa
       password: ${DB_PASSWORD}
     jpa:
       hibernate:
         ddl-auto: validate
   
   jwt:
     secret: ${JWT_SECRET:your-super-secret-key-min-32-characters-long}
     expiration-minutes: 1440
   
   app:
     base-url: http://localhost:8080
     cors:
       allowed-origins: http://localhost:3000
   ```

### 3.2 Database Migration & Seeding
Apply Flyway migrations to SQL Server and initialize Full-Text Search Catalogs:
```bash
./mvnw flyway:migrate
# or with Gradle
./gradlew flywayMigrate
```

### 3.3 Execution Commands

- **Run Web API**:
  ```bash
  ./mvnw spring-boot:run
  # or with Gradle
  ./gradlew bootRun
  ```
  *Default endpoint*: `http://localhost:8080`  
  *Swagger UI*: `http://localhost:8080/swagger-ui.html`  
  *OpenAPI JSON*: `http://localhost:8080/v3/api-docs`

- **Run Admin Dashboard (React/Vue - Arabic RTL)**:
  ```bash
  cd admin-dashboard
  npm install
  npm run dev
  ```
  *Default endpoint*: `http://localhost:3000`

- **Execute Test Suite**:
  ```bash
  ./mvnw test
  # or with Gradle
  ./gradlew test
  ```

- **Build Production JAR**:
  ```bash
  ./mvnw clean package
  # or with Gradle
  ./gradlew build
  ```

---

## 4. Specification Structure Index (فهرس ومستندات المواصفات)

This directory (`docs/`) contains the authoritative specification suite organized into standardized document modules:

### 4.1 Essential Documents

| # | File Name | Arabic Description | English Purpose |
|---|---|---|---|
| 1 | `README.md` | تعريف المشروع وطريقة تشغيله والمتطلبات وأوامر التشغيل | Overview, prerequisites, installation, run commands & index. |
| 2 | `PRODUCT-REQUIREMENTS.md` | ماذا نبني؟ ولماذا؟ الأهداف، الجمهور، المميزات، مؤشرات النجاح | PRD: Target audience, bilingual mobile apps, Arabic admin, feature scope & KPIs. |
| 3 | `PRODUCT-SPECIFICATION.md` | كيف يجب أن يعمل المنتج؟ القواعد، السيناريوهات، حالات الاستثناء، معايير القبول | Functional specs, business rules, localization rules, edge cases & scenarios. |
| 4 | `TECHNICAL-SPECIFICATION.md` | كيف سنبنيه؟ التقنيات، المعمارية، المتطلبات التقنية، القيود | NFRs, SQL Server specs, localization middleware, error formats & config schema. |
| 5 | `ARCHITECTURE.md` | معمارية النظام، المكونات، تدفق البيانات، الـ APIs، القرارات التصميمية | Layered architecture, SQL Server FTS catalog design, API localization pipeline. |
| 6 | `DATA-MODEL.md` | نماذج البيانات، الجداول، العلاقات، التحقق من البيانات | SQL Server database schema, `uniqueidentifier` PKs, bilingual `nvarchar` columns & indexes. |
| 7 | `IMPLEMENTATION-PLAN.md` | خطة التنفيذ خطوة بخطوة، المراحل، النطاق، معايير القبول | 17-stage modular implementation guide with SQL Server and localization DoD checklists. |
| 8 | `DECISIONS.md` | تسجيل جميع القرارات الهندسية وأسبابها | Architecture Decision Records (ADRs 001–008). |
| 9 | `ROADMAP.md` | خريطة الطريق، الإصدارات، ما الذي سيأتي لاحقاً | Release phases (Phase 1 MVP Parity -> Phase 2 Monetization -> Phase 3 Expansion). |

### 4.2 Module Specifications

Located in `docs/Modules/`, these documents provide stage-by-stage implementation details:

| Stage | File | Module |
|---|---|---|
| — | `00-master.md` | Global Contract, NFRs, Build Order |
| 0 | `01-foundation.md` | Foundation & Cross-cutting Infrastructure |
| 1 | `02-auth.md` | Auth & Identity |
| 2 | `03-lookups.md` | Lookups (Cities, Districts, Services, Features) |
| 3 | `04-properties.md` | Properties |
| 4 | `05-companies.md` | Companies & Follow |
| 5 | `06-saved-items.md` | Saved Items |
| 6 | `07-reviews.md` | Reviews |
| 7 | `08-appointments.md` | Appointments |
| 8 | `09-chat.md` | Chat |
| 9 | `10-broadcasts.md` | Broadcasts |
| 10 | `11-search.md` | Search (SQL FTS) |
| 11 | `12-notifications.md` | Notifications |
| 12 | `13-admin.md` | Admin API + Dashboard |
| 13 | `14-commerce.md` | Commerce (Plans, Subscriptions, Payments) |
| 14 | `15-social-analytics.md` | Social Login + Analytics (Aspirational) |
| 15 | `16-projects-timeline.md` | Projects & Timeline (Aspirational) |
| 16 | `17-background-jobs.md` | Background Jobs (Aspirational) |

---

## 5. Specification Hierarchy Rules

1. **Parity Priority**: Core API contracts follow parity specifications; database engine is Microsoft SQL Server.
2. **Bilingual Localization**: Mobile apps support both Arabic (`ar-EG`) and English (`en-US`). Admin Dashboard is native Arabic (`ar-EG`) with RTL formatting.
3. **Wire Contract Fixed**: camelCase JSON keys, ISO-8601 UTC datetimes, nulls omitted, 0-based pagination, localized error messages.
4. **Simplicity Guardrail**: Single Spring Boot Application + single Admin Dashboard + single Microsoft SQL Server database.

---

## 6. Project Structure

```
deltahomes-backend/
├── src/
│   ├── main/
│   │   ├── java/com/deltahomes/
│   │   │   ├── controller/          # REST Controllers
│   │   │   ├── service/             # Business Logic Services
│   │   │   ├── repository/          # Spring Data JPA Repositories
│   │   │   ├── entity/              # JPA Entities
│   │   │   ├── dto/                 # Data Transfer Objects
│   │   │   ├── enums/               # Enumerations
│   │   │   ├── security/            # Security Config & JWT
│   │   │   ├── exception/           # Exception Handling
│   │   │   ├── config/              # Application Configuration
│   │   │   └── util/                # Utilities
│   │   └── resources/
│   │       ├── db/migration/        # Flyway Migrations
│   │       ├── messages_ar.properties  # Arabic Messages
│   │       ├── messages_en.properties  # English Messages
│   │       └── application.yml      # Configuration
│   └── test/java/com/deltahomes/    # Test Classes
├── docs/                            # Documentation
│   ├── Essential/                   # Essential Documents
│   └── Modules/                     # Module Specifications
├── pom.xml                          # Maven Configuration
└── README.md                        # This file
```

---

## 7. Quick Reference

### API Endpoints (Base: `/api/v1`)

| Module | Endpoints |
|---|---|
| Auth | `POST /auth/register`, `POST /auth/login`, `POST /auth/otp/*` |
| Properties | `GET/POST /properties`, `GET/PUT/DELETE /properties/{id}` |
| Companies | `GET/POST /companies`, `POST /companies/{id}/follow` |
| Lookups | `GET /cities`, `GET /districts`, `GET /services`, `GET /features` |
| Appointments | `GET/POST /appointments`, `PUT /appointments/{id}/status` |
| Reviews | `GET/POST /reviews` |
| Chat | `GET/POST /chat/conversations`, `GET/POST /chat/conversations/{id}/messages` |
| Search | `GET /search/properties` |
| Admin | `GET /admin/users`, `PUT /admin/verifications/{id}`, `GET /admin/reports` |

### Common Headers

```
Authorization: Bearer <jwt_token>
Accept-Language: ar-EG | en-US
Content-Type: application/json
```

### Error Response Format

```json
{
  "timestamp": "2026-08-08T12:00:00Z",
  "status": 400,
  "error": "VALIDATION_ERROR",
  "message": "حدث خطأ أثناء التحقق من صحة البيانات المدخلة.",
  "validationErrors": {
    "phone": ["رقم الهاتف المحمول يجب أن يكون برقم مصري صحيح."]
  }
}
```
