# Delta Homes — Architecture Decision Records (ADRs)

> **تسجيل جميع القرارات الهندسية وأسبابها.**  
> **Record of key architectural decisions, Microsoft SQL Server migration, API Localization, and Arabic Admin Dashboard.**

---

## Index of Decision Records

| ADR ID | Title | Status | Date |
|---|---|---|---|
| **ADR-001** | Rebuild Stack: Maintain Java Spring Boot Architecture | Accepted | 2026-08-04 |
| **ADR-002** | Monolithic Architecture Constraint (No Microservices / CQRS) | Accepted | 2026-08-04 |
| **ADR-003** | Database Engine: Microsoft SQL Server & `uniqueidentifier` PKs | Accepted | 2026-08-04 |
| **ADR-004** | Search Architecture: Microsoft SQL Server Full-Text Catalog (`CONTAINS`) | Accepted | 2026-08-04 |
| **ADR-005** | Security Model & Whitelisted Dynamic SQL Sorting | Accepted | 2026-08-04 |
| **ADR-006** | Arabic-First Admin Dashboard with Modern Frontend Framework | Accepted | 2026-08-04 |
| **ADR-007** | Strict Scope Boundary: Core Parity vs Aspirational Features | Accepted | 2026-08-04 |
| **ADR-008** | API Localization Architecture (Arabic & English Mobile Support) | Accepted | 2026-08-04 |

---

## ADR-001: Technology Stack - Java Spring Boot

### Context
The system was originally prototyped in Java 21 using Spring Boot 3.x. The decision is to maintain this technology stack rather than migrating to .NET.

### Decision
Maintain the backend as a **Spring Boot 3.x Web Application** using **Java 17+ LTS** with **Spring Data JPA** and **Microsoft SQL Server**.

### Rationale
- **Team Expertise**: Development team has strong Java/Spring expertise
- **Ecosystem Maturity**: Spring Boot provides a mature, battle-tested ecosystem
- **JPA/Hibernate**: Industry-standard ORM with excellent tooling
- **Spring Security**: Comprehensive security framework for authentication/authorization
- **Community Support**: Large community and extensive documentation
- **Long-term Support**: Java 17+ LTS provides stability

---

## ADR-002: Monolithic Architecture Constraint (No Microservices / CQRS)

### Context
Distributed microservice architectures and CQRS / Event-Driven messaging patterns introduce substantial operational complexity during early product stages.

### Decision
Keep the backend as a single, clean monolithic Spring Boot Web Application (`com.deltahomes`) communicating directly with a single Microsoft SQL Server database instance via Spring Data JPA.

### Rationale
- **Simplicity**: Easier to develop, test, and deploy
- **Cost-effective**: Lower infrastructure costs
- **Development speed**: Faster iteration cycles
- **Operational simplicity**: Single deployment unit
- **Data consistency**: ACID transactions across all entities

### Future Considerations
If the system needs to scale, consider:
- Horizontal scaling with load balancers
- Caching layer (Redis)
- Read replicas for database

---

## ADR-003: Database Engine & Identifier Strategy (Microsoft SQL Server)

### Context
The relational data store requires an enterprise relational database engine with strong tooling, seamless Spring Data JPA integration, and native support for Unicode Arabic text storage.

### Decision
Adopt **Microsoft SQL Server 2022+ / Azure SQL Database** as the single primary database engine using:
- `uniqueidentifier` primary keys (UUIDs generated in `@PrePersist`).
- `nvarchar(n)` and `nvarchar(max)` data types for all text columns (ensuring full UTF-8 Unicode support for Arabic and English).
- `datetimeoffset` for standard UTC datetime tracking.

### Rationale
- **Full-Text Search**: Native SQL Server Full-Text Search with Arabic language support (LANGUAGE 1025)
- **Unicode Support**: Native `nvarchar` support for Arabic characters
- **Enterprise Features**: Point-in-time recovery, AlwaysOn availability groups
- **Cloud-ready**: Seamless migration to Azure SQL Database
- **Tooling**: Excellent tooling with SQL Server Management Studio, Azure Data Studio

### JPA Configuration
```java
@Entity
@Table(name = "users")
public class User extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;
    
    @Column(name = "name_ar", columnDefinition = "nvarchar(200)")
    private String nameAr;
}
```

---

## ADR-004: Microsoft SQL Server Native Full-Text Search Catalog

### Context
Searching property titles, descriptions, and addresses requires fast keyword matching across English and Arabic text without introducing third-party search server clusters.

### Decision
Utilize Microsoft SQL Server native Full-Text Search Catalogs (`CREATE FULLTEXT CATALOG`) and Full-Text Indexes over Arabic (`LANGUAGE 1025`) and English (`LANGUAGE 1033`) text columns, queried using T-SQL `CONTAINS` / `FREETEXT` via Spring Data JPA native queries.

### Rationale
- **Native Integration**: No additional infrastructure required
- **Bilingual Support**: Built-in Arabic and English language support
- **Performance**: Optimized for full-text search workloads
- **Cost**: No additional licensing for external search engines
- **Simplicity**: Single technology stack

### Implementation
```java
@Query(value = "SELECT * FROM properties p WHERE " +
       "CONTAINS((p.title_ar, p.title_en, p.description_ar, p.description_en), :query)",
       nativeQuery = true)
Page<Property> searchByFullText(@Param("query") String query, Pageable pageable);
```

### Migration Script
```sql
CREATE FULLTEXT CATALOG DeltaHomesPropertyCatalog AS DEFAULT;

CREATE FULLTEXT INDEX ON properties(
    title_ar LANGUAGE 1025,
    title_en LANGUAGE 1033,
    description_ar LANGUAGE 1025,
    description_en LANGUAGE 1033
) KEY INDEX PK_properties;
```

---

## ADR-005: Security Model & Whitelisted Dynamic SQL Sorting

### Context
Dynamic sorting inputs (`sort=price,asc`) passed in query parameters can expose systems to SQL injection if blindly interpolated into SQL queries.

### Decision
Implement a strict `SortNormalizer` utility class that maps and validates allowed incoming sort fields against a strict domain column whitelist before execution. Use Spring Data's `Pageable` interface for safe sorting.

### Rationale
- **Security**: Prevents SQL injection attacks
- **Performance**: Avoids unnecessary columns in ORDER BY
- **Maintainability**: Centralized sort column management
- **Type Safety**: Validates sort direction and column names

### Implementation
```java
@Component
public class SortNormalizer {
    
    private static final Set<String> ALLOWED_SORT_FIELDS = Set.of(
        "createdAt", "updatedAt", "price", "areaSqm", "titleAr", "titleEn"
    );
    
    public Sort normalize(Sort sort) {
        return sort.stream()
            .filter(order -> ALLOWED_SORT_FIELDS.contains(order.getProperty()))
            .map(order -> order.withProperty(toSnakeCase(order.getProperty())))
            .reduce(Sort.unsorted(), Sort::and);
    }
    
    private String toSnakeCase(String camelCase) {
        return camelCase.replaceAll("([a-z])([A-Z]+)", "$1_$2").toLowerCase();
    }
}
```

---

## ADR-006: Arabic-First Admin Dashboard with Modern Frontend Framework

### Context
Platform operations, verifications, audit monitoring, and content moderation are managed by an Egyptian administration team.

### Decision
Build the admin dashboard using a **modern frontend framework** (React or Vue.js) configured natively in **Arabic (`ar-EG`)** with full **Right-to-Left (RTL)** layout formatting (`dir="rtl"`).

### Rationale
- **Team Familiarity**: Frontend developers can use their preferred framework
- **Rich Ecosystem**: Extensive component libraries for admin dashboards
- **Performance**: Optimized rendering for complex admin interfaces
- **RTL Support**: Mature RTL support in modern frameworks
- **Mobile-Responsive**: Built-in responsive design capabilities

### Technology Options
| Framework | RTL Support | Component Libraries |
|---|---|---|
| React | Excellent | Material-UI, Ant Design, Chakra UI |
| Vue.js | Excellent | Vuetify, Element Plus, Quasar |

---

## ADR-007: Scope Boundary: Core Parity vs Aspirational Features

### Context
Product document requirements include aspirational features (Google/Apple social login, PostHog analytics pipelines, construction project timelines, background job queues) not present in the current parity baseline.

### Decision
Enforce a two-tier implementation boundary: Stages 0–13 focus strictly on **1:1 Parity**, while Stages 14–16 encompass **Aspirational Features**.

### Rationale
- **Risk Management**: Avoid scope creep
- **Deliverable Clarity**: Clear milestones for stakeholders
- **Resource Allocation**: Prioritize core functionality
- **Testing Focus**: Ensure parity features are thoroughly tested

### Parity Features (Stages 0-13)
- Authentication & Authorization
- Property Management
- Company Management
- Reviews & Ratings
- Appointments
- Chat
- Broadcasts
- Search
- Notifications
- Admin Dashboard
- Commerce

### Aspirational Features (Stages 14-16)
- Social Login (Google/Apple)
- Analytics Integration (PostHog)
- Construction Projects & Timelines
- Background Job Processing

---

## ADR-008: API Localization Architecture (Arabic & English Mobile Support)

### Context
The mobile client app serves a diverse Egyptian user base requiring full support for both Arabic (`ar-EG`) and English (`en-US`) languages.

### Decision
Configure Spring's `LocaleResolver` and `LocaleChangeInterceptor` to inspect `Accept-Language` headers. Backend endpoints return localized error responses via `MessageSource`, localized validation constraints via `.properties` resource bundles, and localized entity payload representations.

### Rationale
- **User Experience**: Native language support for all users
- **Spring Integration**: Built-in localization support
- **Resource Bundles**: Standard Java i18n mechanism
- **Performance**: No runtime overhead for localization
- **Maintainability**: Clear separation of code and translations

### Implementation
```java
@Configuration
public class LocaleConfig implements WebMvcConfigurer {
    
    @Bean
    public LocaleResolver localeResolver() {
        AcceptHeaderLocaleResolver resolver = new AcceptHeaderLocaleResolver();
        resolver.setDefaultLocale(Locale.forLanguageTag("ar-EG"));
        resolver.setSupportedLocales(List.of(
            Locale.forLanguageTag("ar-EG"),
            Locale.forLanguageTag("en-US")
        ));
        return resolver;
    }
}
```

### Resource Bundles
```properties
# messages_ar.properties
validation.phone.required=رقم الهاتف مطلوب
validation.email.invalid=البريد الإلكتروني غير صالح
error.unauthorized=يجب تسجيل الدخول للوصول إلى هذا المورد

# messages_en.properties
validation.phone.required=Phone number is required
validation.email.invalid=Invalid email address
error.unauthorized=Authentication is required to access this resource
```

---

## 9. References

1. Spring Boot Documentation: https://docs.spring.io/spring-boot/
2. Spring Data JPA: https://spring.io/projects/spring-data-jpa
3. Spring Security: https://spring.io/projects/spring-security
4. Microsoft SQL Server Full-Text Search: https://docs.microsoft.com/en-us/sql/relational-databases/search/full-text-search
5. Flyway Migrations: https://flywaydb.org/documentation/
6. Java Internationalization: https://docs.oracle.com/javase/tutorial/i18n/
