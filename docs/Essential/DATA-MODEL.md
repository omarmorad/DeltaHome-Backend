# Delta Homes — Database Schema & Entity Model Reference

> **نماذج البيانات، الجداول، العلاقات، التحقق من البيانات.**  
> **Microsoft SQL Server database schema, JPA entity models, `uniqueidentifier` PKs, bilingual `nvarchar` columns, relationships, and full-text indexes.**

---

## 1. Schema Design Principles

- **Database Engine**: Microsoft SQL Server 2022+ / Azure SQL Database.
- **Primary Key Strategy**: `uniqueidentifier` primary keys with `UUID.randomUUID()` generated in `@PrePersist`.
- **Audit Columns**: Every table inherits from `BaseEntity` with `created_at datetimeoffset NOT NULL` and `updated_at datetimeoffset NOT NULL`.
- **Unicode Support**: All string and text columns use `nvarchar(n)` or `nvarchar(max)` to support Arabic (`ar-EG`) and English (`en-US`) text natively.
- **Bilingual Columns**: Lookup tables and properties store explicit `_ar` and `_en` columns (e.g., `name_ar`, `name_en`).
- **Enum Columns**: Stored as `nvarchar(50)` in SQL Server and mapped in JPA using `@Enumerated(EnumType.STRING)`.
- **JSON Fields**: Extended dynamic attributes stored in `nvarchar(max)` columns.

---

## 2. Entity Relational Diagram (ERD Overview)

```
┌──────────────┐         1:N         ┌──────────────┐
│    users     │ ───────────────────►│  properties  │
└──────┬───────┘                     └──────┬───────┘
       │                                    │
       │ 1:N                                │ 1:N
       ▼                                    ▼
┌──────────────┐                     ┌──────────────┐
│ appointments │                     │property_images│
└──────────────┘                     └──────┬───────┘
       ▲                                    │
       │ N:1                                │ N:1
┌──────┴───────┐                            │
│  companies   │◄───────────────────────────┘
└──────────────┘
```

---

## 3. Data Tables & Column Definitions (SQL Server Types)

### 3.1 Auth & User Module

#### `users`
| Column | Type | Constraints | Description |
|---|---|---|---|
| `id` | `uniqueidentifier` | PK | Unique User Identifier (UUID generated in code) |
| `email` | `nvarchar(256)` | UNIQUE, NULLABLE | User Email Address |
| `phone` | `nvarchar(30)` | UNIQUE, NOT NULL | User Egyptian Phone Number |
| `password_hash` | `nvarchar(500)` | NOT NULL | BCrypt Password Hash |
| `full_name` | `nvarchar(200)` | NOT NULL | User Full Name |
| `avatar_url` | `nvarchar(1000)`| NULLABLE | Profile Picture URL |
| `role` | `nvarchar(50)` | NOT NULL | Enum: `CUSTOMER`, `OWNER`, `OFFICE`, `COMPANY`, `TECHNICIAN`, `ADMIN` |
| `preferred_culture` | `nvarchar(10)` | DEFAULT 'ar-EG' | User Preferred Language (`ar-EG` / `en-US`) |
| `status` | `nvarchar(50)` | NOT NULL | Enum: `ACTIVE`, `SUSPENDED`, `PENDING`, `DELETED` |
| `created_at` | `datetimeoffset` | NOT NULL | Creation Timestamp (UTC) |
| `updated_at` | `datetimeoffset` | NOT NULL | Modification Timestamp (UTC) |

#### JPA Entity Example:
```java
@Entity
@Table(name = "users")
@EntityListeners(AuditingEntityListener.class)
public class User extends BaseEntity {
    
    @Column(name = "email", length = 256, unique = true)
    private String email;
    
    @Column(name = "phone", length = 30, nullable = false, unique = true)
    private String phone;
    
    @Column(name = "password_hash", length = 500, nullable = false)
    private String passwordHash;
    
    @Column(name = "full_name", length = 200, nullable = false)
    private String fullName;
    
    @Column(name = "avatar_url", length = 1000)
    private String avatarUrl;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "role", length = 50, nullable = false)
    private UserRole role;
    
    @Column(name = "preferred_culture", length = 10)
    private String preferredCulture = "ar-EG";
    
    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 50, nullable = false)
    private UserStatus status = UserStatus.PENDING;
}
```

#### `otp_codes`
| Column | Type | Constraints | Description |
|---|---|---|---|
| `id` | `uniqueidentifier` | PK | Record ID |
| `identifier` | `nvarchar(256)` | NOT NULL | Phone Number or Email |
| `code_hash` | `nvarchar(128)` | NOT NULL | SHA-256 Hash of 6-digit OTP |
| `purpose` | `nvarchar(50)` | NOT NULL | Enum: `REGISTRATION`, `LOGIN`, `PASSWORD_RESET` |
| `expires_at` | `datetimeoffset` | NOT NULL | Expiration Timestamp (5 mins) |
| `is_used` | `bit` | DEFAULT 0 | Consumption Flag |
| `created_at` | `datetimeoffset` | NOT NULL | Creation Timestamp |

---

### 3.2 Lookups & Location Module (Bilingual Schema)

#### `cities`
| Column | Type | Constraints | Description |
|---|---|---|---|
| `id` | `uniqueidentifier` | PK | City ID |
| `name_ar` | `nvarchar(150)` | NOT NULL | Arabic City Name (e.g. `القاهرة`) |
| `name_en` | `nvarchar(150)` | NOT NULL | English City Name (e.g. `Cairo`) |
| `code` | `nvarchar(50)` | UNIQUE, NOT NULL | City Code |

#### JPA Entity Example:
```java
@Entity
@Table(name = "cities")
public class City extends BaseEntity {
    
    @Column(name = "name_ar", length = 150, nullable = false)
    private String nameAr;
    
    @Column(name = "name_en", length = 150, nullable = false)
    private String nameEn;
    
    @Column(name = "code", length = 50, nullable = false, unique = true)
    private String code;
    
    @OneToMany(mappedBy = "city", fetch = FetchType.LAZY)
    private List<District> districts = new ArrayList<>();
}
```

#### `districts`
| Column | Type | Constraints | Description |
|---|---|---|---|
| `id` | `uniqueidentifier` | PK | District ID |
| `city_id` | `uniqueidentifier` | FK ➔ `cities.id` | Parent City |
| `name_ar` | `nvarchar(150)` | NOT NULL | Arabic District Name |
| `name_en` | `nvarchar(150)` | NOT NULL | English District Name |

---

### 3.3 Property Listing Module (Bilingual & SQL Server Types)

#### `properties`
| Column | Type | Constraints | Description |
|---|---|---|---|
| `id` | `uniqueidentifier` | PK | Property Listing ID |
| `title_ar` | `nvarchar(250)` | NOT NULL | Arabic Listing Title |
| `title_en` | `nvarchar(250)` | NULLABLE | English Listing Title |
| `description_ar` | `nvarchar(max)` | NOT NULL | Arabic Listing Description |
| `description_en` | `nvarchar(max)` | NULLABLE | English Listing Description |
| `price` | `decimal(18,2)` | NOT NULL | Price in EGP |
| `area_sqm` | `decimal(10,2)` | NOT NULL | Area in Square Meters |
| `purpose` | `nvarchar(50)` | NOT NULL | Enum: `SALE`, `RENT` |
| `category` | `nvarchar(50)` | NOT NULL | Property category |
| `status` | `nvarchar(50)` | NOT NULL | Enum: `DRAFT`, `PENDING_REVIEW`, `PUBLISHED`, `HIDDEN`, `SOLD`, `RENTED`, `ARCHIVED` |
| `bedrooms` | `int` | NULLABLE | Number of Bedrooms |
| `bathrooms` | `int` | NULLABLE | Number of Bathrooms |
| `floor_number` | `int` | NULLABLE | Floor Level |
| `readiness` | `nvarchar(50)` | NOT NULL | Enum: `READY`, `UNDER_CONSTRUCTION` |
| `finishing_level` | `nvarchar(50)` | NOT NULL | Enum: `UNFINISHED`, `SEMI_FINISHED`, `FINISHED`, `LUXURY` |
| `city_id` | `uniqueidentifier` | FK ➔ `cities.id` | City Reference |
| `district_id` | `uniqueidentifier` | FK ➔ `districts.id` | District Reference |
| `owner_user_id` | `uniqueidentifier` | FK ➔ `users.id` | Listing Owner |
| `company_id` | `uniqueidentifier` | FK ➔ `companies.id` | Managing Company (Optional) |
| `features` | `nvarchar(max)` | NULLABLE | JSON array of feature IDs |
| `images` | `nvarchar(max)` | NULLABLE | JSON array of image URLs |
| `is_featured` | `bit` | DEFAULT 0 | Featured listing flag |
| `created_at` | `datetimeoffset` | NOT NULL | Creation Timestamp |
| `updated_at` | `datetimeoffset` | NOT NULL | Update Timestamp |

#### JPA Entity Example:
```java
@Entity
@Table(name = "properties")
@EntityListeners(AuditingEntityListener.class)
public class Property extends BaseEntity {
    
    @Column(name = "title_ar", length = 250, nullable = false)
    private String titleAr;
    
    @Column(name = "title_en", length = 250)
    private String titleEn;
    
    @Column(name = "description_ar", columnDefinition = "nvarchar(max)", nullable = false)
    private String descriptionAr;
    
    @Column(name = "description_en", columnDefinition = "nvarchar(max)")
    private String descriptionEn;
    
    @Column(name = "price", nullable = false, precision = 18, scale = 2)
    private BigDecimal price;
    
    @Column(name = "area_sqm", nullable = false, precision = 10, scale = 2)
    private BigDecimal areaSqm;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "purpose", length = 50, nullable = false)
    private PropertyPurpose purpose;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 50, nullable = false)
    private PropertyStatus status = PropertyStatus.DRAFT;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "city_id")
    private City city;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "district_id")
    private District district;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_user_id", nullable = false)
    private User owner;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id")
    private Company company;
    
    @Column(name = "features", columnDefinition = "nvarchar(max)")
    private String features; // JSON string
    
    @Column(name = "is_featured", nullable = false)
    private Boolean isFeatured = false;
}
```

#### `property_images`
| Column | Type | Constraints | Description |
|---|---|---|---|
| `id` | `uniqueidentifier` | PK | Image ID |
| `property_id` | `uniqueidentifier` | FK ➔ `properties.id`| Parent Property |
| `image_url` | `nvarchar(1000)`| NOT NULL | Storage Image URL |
| `is_primary` | `bit` | DEFAULT 0 | Primary Cover Image Flag |
| `display_order` | `int` | DEFAULT 0 | Sorting Position |

---

### 3.4 Companies & Subscriptions

#### `companies`
| Column | Type | Constraints | Description |
|---|---|---|---|
| `id` | `uniqueidentifier` | PK | Company ID |
| `name_ar` | `nvarchar(200)` | NOT NULL | Arabic Company Name |
| `name_en` | `nvarchar(200)` | NULLABLE | English Company Name |
| `type` | `nvarchar(50)` | NOT NULL | Enum: `REAL_ESTATE_OFFICE`, `FINISHING_COMPANY`, `MAINTENANCE_PROVIDER` |
| `logo_url` | `nvarchar(1000)`| NULLABLE | Company Logo URL |
| `phone` | `nvarchar(30)` | NOT NULL | Business Phone |
| `whatsapp` | `nvarchar(30)` | NULLABLE | WhatsApp Number |
| `email` | `nvarchar(256)` | NULLABLE | Business Email |
| `website` | `nvarchar(500)` | NULLABLE | Website URL |
| `verified` | `bit` | DEFAULT 0 | Business Verification Badge |
| `followers_count` | `int` | DEFAULT 0 | Total Followers |
| `reputation_score` | `decimal(3,2)` | DEFAULT 0.00 | Average Rating |
| `created_at` | `datetimeoffset` | NOT NULL | Creation Timestamp |
| `updated_at` | `datetimeoffset` | NOT NULL | Update Timestamp |

#### JPA Entity Example:
```java
@Entity
@Table(name = "companies")
@EntityListeners(AuditingEntityListener.class)
public class Company extends BaseEntity {
    
    @Column(name = "name_ar", length = 200, nullable = false)
    private String nameAr;
    
    @Column(name = "name_en", length = 200)
    private String nameEn;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "type", length = 50, nullable = false)
    private CompanyType type;
    
    @Column(name = "logo_url", length = 1000)
    private String logoUrl;
    
    @Column(name = "phone", length = 30, nullable = false)
    private String phone;
    
    @Column(name = "email", length = 256)
    private String email;
    
    @Column(name = "verified", nullable = false)
    private Boolean verified = false;
    
    @Column(name = "followers_count", nullable = false)
    private Integer followersCount = 0;
    
    @Column(name = "reputation_score", precision = 3, scale = 2)
    private BigDecimal reputationScore = BigDecimal.ZERO;
}
```

---

## 4. SQL Server Indexes & Full-Text Search Catalog

### 4.1 Full-Text Catalog Setup (Flyway Migration)

```sql
-- V1__Create_FullText_Catalog.sql
IF NOT EXISTS (SELECT * FROM sys.fulltext_catalogs WHERE name = 'DeltaHomesPropertyCatalog')
BEGIN
    CREATE FULLTEXT CATALOG DeltaHomesPropertyCatalog AS DEFAULT;
END

-- V2__Create_Property_FullText_Index.sql
IF NOT EXISTS (SELECT * FROM sys.fulltext_indexes WHERE object_id = OBJECT_ID('properties'))
BEGIN
    CREATE FULLTEXT INDEX ON properties(
        title_ar LANGUAGE 1025,
        title_en LANGUAGE 1033,
        description_ar LANGUAGE 1025,
        description_en LANGUAGE 1033
    )
    KEY INDEX PK_properties ON DeltaHomesPropertyCatalog;
END
```

### 4.2 Repository Search Example

```java
@Repository
public interface PropertyRepository extends JpaRepository<Property, UUID> {
    
    @Query(value = "SELECT * FROM properties p WHERE " +
           "(:query = '' OR CONTAINS((p.title_ar, p.title_en, p.description_ar, p.description_en), :query))",
           nativeQuery = true)
    Page<Property> searchByFullText(@Param("query") String query, Pageable pageable);
}
```

### 4.3 Unique & Non-Clustered Indexes

```sql
CREATE UNIQUE INDEX IX_users_phone ON users(phone);
CREATE UNIQUE INDEX IX_users_email ON users(email) WHERE email IS NOT NULL;
CREATE INDEX IX_properties_city_id ON properties(city_id);
CREATE INDEX IX_properties_district_id ON properties(district_id);
CREATE INDEX IX_properties_status ON properties(status);
CREATE INDEX IX_properties_owner_user_id ON properties(owner_user_id);
CREATE INDEX IX_properties_company_id ON properties(company_id);
```

---

## 5. BaseEntity Implementation

```java
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
public abstract class BaseEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;
    
    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private OffsetDateTime createdAt;
    
    @LastModifiedDate
    @Column(name = "updated_at")
    private OffsetDateTime updatedAt;
    
    @PrePersist
    protected void onCreate() {
        if (id == null) {
            id = UUID.randomUUID();
        }
        if (createdAt == null) {
            createdAt = OffsetDateTime.now(ZoneOffset.UTC);
        }
    }
    
    // Getters and setters
}
```

---

## 6. Spring Data JPA Repository Examples

```java
@Repository
public interface UserRepository extends JpaRepository<User, UUID> {
    
    Optional<User> findByPhone(String phone);
    
    Optional<User> findByEmail(String email);
    
    @Query("SELECT u FROM User u WHERE u.phone = :identifier OR u.email = :identifier")
    Optional<User> findByPhoneOrEmail(@Param("identifier") String identifier);
    
    boolean existsByPhone(String phone);
    
    boolean existsByEmail(String email);
}

@Repository
public interface OtpCodeRepository extends JpaRepository<OtpCode, UUID> {
    
    @Query("SELECT o FROM OtpCode o WHERE o.identifier = :identifier " +
           "AND o.purpose = :purpose AND o.isUsed = false AND o.expiresAt > :now")
    Optional<OtpCode> findValidOtp(@Param("identifier") String identifier,
                                    @Param("purpose") OtpPurpose purpose,
                                    @Param("now") OffsetDateTime now);
    
    @Modifying
    @Query("DELETE FROM OtpCode o WHERE o.expiresAt < :now")
    void deleteExpiredOtps(@Param("now") OffsetDateTime now);
}
```

---

## 7. Enum Definitions

```java
public enum UserRole {
    CUSTOMER, OWNER, OFFICE, COMPANY, TECHNICIAN, ADMIN
}

public enum UserStatus {
    ACTIVE, SUSPENDED, PENDING, DELETED
}

public enum PropertyPurpose {
    SALE, RENT
}

public enum PropertyStatus {
    DRAFT, PENDING_REVIEW, PUBLISHED, HIDDEN, SOLD, RENTED, ARCHIVED
}

public enum CompanyType {
    REAL_ESTATE_OFFICE, FINISHING_COMPANY, MAINTENANCE_PROVIDER
}

public enum OtpPurpose {
    REGISTRATION, LOGIN, PASSWORD_RESET
}
```
