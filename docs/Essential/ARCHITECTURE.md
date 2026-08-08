# Delta Homes — System Architecture & Design Specification

> **معمارية النظام، المكونات، تدفق البيانات، الـ APIs، القرارات التصميمية.**  
> **System architecture, component diagrams, Microsoft SQL Server FTS design, localization pipeline, and Arabic Admin layout.**

---

## 1. Solution Architecture Overview

Delta Homes is built around a **Deliberate Simplicity Monolith** pattern powered by Spring Boot and Microsoft SQL Server.

```
┌───────────────────────────────────────┐       ┌───────────────────────────────────────┐
│     Mobile App (Arabic & English)     │       │       Admin Dashboard (Arabic)        │
│            (iOS / Android)            │       │          (React/Vue RTL UI)            │
└───────────────────┬───────────────────┘       └───────────────────┬───────────────────┘
                    │                                               │
                    │ HTTP / REST (Accept-Language Header)          │ HTTP / REST
                    └───────────────────────┬───────────────────────┘
                                            │
                                            ▼
┌────────────────────────────────────────────────────────────────────────────────┐
│ DeltaHomes Backend (Spring Boot 3.x Web Application)                          │
│                                                                                │
│   ┌────────────────────────────────────────────────────────────────────────┐   │
│   │ Filter Chain (ExceptionHandlingFilter, LocaleChangeInterceptor, JWT)   │   │
│   └───────────────────────────────────┬────────────────────────────────────┘   │
│                                       │                                        │
│   ┌───────────────────────────────────▼────────────────────────────────────┐   │
│   │ Controllers (AuthController, PropertyController, AdminController, etc.)│   │
│   └───────────────────────────────────┬────────────────────────────────────┘   │
│                                       │                                        │
│   ┌───────────────────────────────────▼────────────────────────────────────┐   │
│   │ Service Layer (AuthService, PropertyService, AppointmentService, etc.) │   │
│   └───────────────────────────────────┬────────────────────────────────────┘   │
│                                       │                                        │
│   ┌───────────────────────────────────▼────────────────────────────────────┐   │
│   │ Repository Layer (Spring Data JPA + Hibernate)                         │   │
│   └───────────────────────────────────┬────────────────────────────────────┘   │
└───────────────────────────────────────┼────────────────────────────────────────┘
                                        │ Microsoft SQL Server JDBC Driver
                                        ▼
┌────────────────────────────────────────────────────────────────────────────────┐
│ Microsoft SQL Server 2022+ Database                                            │
│ (Tables, FK Constraints, FULLTEXT CATALOG, FULLTEXT INDEX on Property Search) │
└────────────────────────────────────────────────────────────────────────────────┘
```

---

## 2. Project Component Breakdown

```
deltahomes-backend/
├── src/main/java/com/deltahomes/
│   ├── controller/              # REST API Controllers (@RestController)
│   ├── service/                 # Business Logic & Localization Services (@Service)
│   ├── repository/              # Spring Data JPA Repositories
│   ├── entity/                  # JPA Entities (BaseEntity + domain entities)
│   ├── dto/                     # Data Transfer Objects (records/POJOs)
│   ├── enums/                   # Enumerations
│   ├── security/                # JWT Token Generator, Security Config, User Context
│   ├── exception/               # Custom Exceptions & Global Exception Handler
│   ├── config/                  # Configuration Classes (Security, Locale, etc.)
│   └── util/                    # Utility Classes (Paging, Sort Normalizers)
├── src/main/resources/
│   ├── messages_ar.properties    # Arabic Resource Bundles
│   ├── messages_en.properties    # English Resource Bundles
│   ├── application.yml           # Main Configuration
│   └── db/migration/             # Flyway/Liquibase Migrations
└── src/test/java/com/deltahomes/ # Unit & Integration Tests (JUnit 5)
```

---

## 3. Data Flow & Request Localization Lifecycle

```
[HTTP Request with Accept-Language: ar-EG / en-US]
    │
    ▼
[LocaleChangeInterceptor]      ──► Sets LocaleContextHolder for current request
    │
    ▼
[Security Filter Chain]        ──► Validates JWT Token Claims
    │
    ▼
[Controller]                   ──► Binds DTO & Invokes @Valid Validation
    │
    ▼
[Service Layer]                ──► Executes Business Logic with localized messages
    │
    ▼
[Spring Data Repository]       ──► Queries SQL Server via Hibernate/JPA
    │
    ▼
[Microsoft SQL Server]         ──► Returns nvarchar Unicode Data Records
```

---

## 4. Microsoft SQL Server Full-Text Search (FTS) Architecture

To support fast Arabic and English search across listing titles, descriptions, and address details, Delta Homes uses SQL Server Full-Text Search Catalogs.

### 4.1 SQL Server Full-Text Catalog & Index Setup
During system startup via Flyway/Liquibase migrations, the application creates the Full-Text Catalog and Full-Text Index over `TitleAr`, `TitleEn`, `DescriptionAr`, and `DescriptionEn`:

```sql
IF NOT EXISTS (SELECT * FROM sys.fulltext_catalogs WHERE name = 'DeltaHomesPropertyCatalog')
BEGIN
    CREATE FULLTEXT CATALOG DeltaHomesPropertyCatalog AS DEFAULT;
END

IF NOT EXISTS (SELECT * FROM sys.fulltext_indexes WHERE object_id = OBJECT_ID('Properties'))
BEGIN
    CREATE FULLTEXT INDEX ON Properties(
        TitleAr LANGUAGE 1025,       -- 1025 = Arabic
        TitleEn LANGUAGE 1033,       -- 1033 = English
        DescriptionAr LANGUAGE 1025,
        DescriptionEn LANGUAGE 1033
    )
    KEY INDEX PK_Properties ON DeltaHomesPropertyCatalog;
END
```

### 4.2 Query Execution Pattern
Full-text search queries execute using native SQL via Spring Data JPA `@Query` with `CONTAINS`:

```java
@Query(value = "SELECT * FROM Properties p WHERE " +
       "(:query = '' OR CONTAINS((p.TitleAr, p.TitleEn, p.DescriptionAr, p.DescriptionEn), :query))",
       nativeQuery = true)
List<Property> searchByFullText(@Param("query") String query, Pageable pageable);
```

Or using `JdbcTemplate` for more complex queries:
```java
String searchPattern = "\"" + query + "*\"";
List<Property> properties = jdbcTemplate.query(
    "SELECT * FROM Properties p WHERE CONTAINS((TitleAr, TitleEn, DescriptionAr, DescriptionEn), ?) " +
    "ORDER BY ? OFFSET ? ROWS FETCH NEXT ? ROWS ONLY",
    new Object[]{searchPattern, sortClause, page * size, size},
    propertyRowMapper
);
```

---

## 5. Architectural Principles & Guardrails

1. **SQL Server Single Database Monolith**: Single SQL Server instance managing relational tables and full-text search catalogs.
2. **Bilingual Mobile API Support**: API endpoints transparently resolve `Accept-Language` headers (`ar-EG`, `en-US`) via `LocaleChangeInterceptor` and deliver localized payloads using `MessageSource`.
3. **Arabic-First Admin Console**: Admin dashboard renders strictly in Arabic with RTL styling (`dir="rtl"`) using React or Vue.js frontend.
4. **Whitelisted T-SQL Sort Sanitization**: Dynamic sort params are sanitized via `SortNormalizer` utility into safe T-SQL column names before use in queries.

---

## 6. Technology Stack

| Layer | Technology | Details |
|---|---|---|
| **Runtime** | Java 17+ (LTS) | OpenJDK / Azul Zulu |
| **Framework** | Spring Boot 3.x | Spring Web MVC, Spring Data JPA |
| **ORM** | Hibernate (via Spring Data JPA) | JPA 3.1 compliant |
| **Database** | Microsoft SQL Server 2022+ | With Full-Text Search |
| **Migrations** | Flyway or Liquibase | Version-controlled schema evolution |
| **Authentication** | Spring Security + JWT | HS256 signed tokens |
| **Validation** | Jakarta Bean Validation | @Valid, @NotNull, etc. |
| **Serialization** | Jackson | JSON processing |
| **Localization** | Spring MessageSource | ResourceBundle based |
| **Build Tool** | Maven or Gradle | Dependency management & build |
| **Testing** | JUnit 5 + Mockito + Testcontainers | Unit & integration tests |
