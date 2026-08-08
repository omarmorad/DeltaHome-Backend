# Delta Homes — Technical Specification & NFR Manual

> **كيف سنبنيه؟ التقنيات، المعمارية، المتطلبات التقنية، القيود.**  
> **Technical stack specifications, Microsoft SQL Server specs, API Localization, Admin Arabic RTL, NFRs, and wire formats.**

---

## 1. Technical Environment & Framework Specifications

| Tier | Component | Technology / Library | Version / Standard |
|---|---|---|---|
| Framework | Web API Core | Spring Boot | 3.x (Java 17 LTS) |
| Front-end | Admin Console | React or Vue.js | Arabic `ar-EG`, RTL `dir="rtl"` |
| Database | System of Record | Microsoft SQL Server | 2022+ / Azure SQL Database |
| ORM | Data Access | Spring Data JPA + Hibernate | Jakarta Persistence 3.1 |
| Serialization | JSON Engine | Jackson | Default Spring Boot configuration |
| Localization | API & Resources | Spring MessageSource | `ar-EG` (Default), `en-US` |
| Auth & Crypto | Security | Spring Security + JWT (jjwt) | HMAC-SHA256 JWT, BCrypt |
| SMS / Email | Notifications | Twilio Java SDK / Spring Mail | Dev-mode fallback logging |
| Build Tool | Build & Dependencies | Maven or Gradle | Wrapper included |
| Testing | Unit & Integration | JUnit 5, Mockito, Testcontainers | Spring Boot Test |

---

## 2. Non-Functional Requirements (NFRs)

### 2.1 Performance & Latency Targets
- **Read APIs**: 95th percentile (p95) latency < 200 ms.
- **Search Queries**: Microsoft SQL Server Full-Text Search (`CONTAINS`) latency < 400 ms over 100,000+ property records.
- **Write APIs**: Latency < 300 ms.

### 2.2 Security & Data Protection
- **JWT Authentication**: Bearer tokens with claims (`sub`, `email`, `phone`, `role`, `preferred_culture`, `exp`).
- **Password Hashing**: BCrypt hashes with strength 10 via Spring Security's `BCryptPasswordEncoder`.
- **OTP Protection**: SHA-256 hex hashing before SQL Server storage using `MessageDigest`.
- **Sort Sanitization**: Dynamic sorting sanitized via `SortNormalizer` utility into whitelisted T-SQL column names.
- **Unicode Support**: All text columns created as `nvarchar` to natively store Arabic and English Unicode characters without corruption.

---

## 3. Localization Architecture

### 3.1 Configuration (application.yml)

```yaml
spring:
  messages:
    basename: messages
    encoding: UTF-8
    fallback-to-system-locale: false
  mvc:
    locale: ar-EG
    locale-resolver: accept-header
```

### 3.2 Locale Resolver Configuration

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
    
    @Bean
    public LocaleChangeInterceptor localeChangeInterceptor() {
        LocaleChangeInterceptor interceptor = new LocaleChangeInterceptor();
        interceptor.setParamName("lang");
        return interceptor;
    }
    
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(localeChangeInterceptor());
    }
}
```

### 3.3 Error Envelope with Localized Output

```json
{
  "timestamp": "2026-08-04T12:53:00Z",
  "status": 400,
  "error": "VALIDATION_ERROR",
  "message": "حدث خطأ أثناء التحقق من صحة البيانات المدخلة.",
  "validationErrors": {
    "phone": ["رقم الهاتف المحمول يجب أن يكون برقم مصري صحيح."]
  }
}
```

### 3.4 Global Exception Handler

```java
@RestControllerAdvice
public class GlobalExceptionHandler {
    
    @Autowired
    private MessageSource messageSource;
    
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationErrors(
            MethodArgumentNotValidException ex,
            Locale locale) {
        
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(error -> {
            String message = messageSource.getMessage(
                error.getDefaultMessage(), 
                null, 
                locale
            );
            errors.put(error.getField(), message);
        });
        
        ErrorResponse response = new ErrorResponse(
            Instant.now(),
            HttpStatus.BAD_REQUEST.value(),
            "VALIDATION_ERROR",
            messageSource.getMessage("error.validation", null, locale),
            errors
        );
        
        return ResponseEntity.badRequest().body(response);
    }
}
```

---

## 4. Configuration Schema (application.yml)

```yaml
server:
  port: 8080

spring:
  application:
    name: delta-homes-backend
  
  datasource:
    url: jdbc:sqlserver://localhost:1433;databaseName=DeltaHomesDb;encrypt=true;trustServerCertificate=true
    username: sa
    password: ${DB_PASSWORD}
    driver-class-name: com.microsoft.sqlserver.jdbc.SQLServerDriver
    hikari:
      maximum-pool-size: 10
      minimum-idle: 5
  
  jpa:
    database-platform: org.hibernate.dialect.SQLServerDialect
    show-sql: false
    hibernate:
      ddl-auto: validate
    properties:
      hibernate:
        format_sql: true
        default_schema: dbo
  
  flyway:
    enabled: true
    locations: classpath:db/migration
    baseline-on-migrate: true
  
  jackson:
    property-naming-strategy: LOWER_CAMEL_CASE
    serialization:
      write-dates-as-timestamps: false
      fail-on-empty-beans: false
    deserialization:
      fail-on-unknown-properties: false
    default-property-inclusion: non_null
    date-format: yyyy-MM-dd'T'HH:mm:ss.SSS'Z'
    time-zone: UTC
  
  messages:
    basename: messages
    encoding: UTF-8

jwt:
  secret: ${JWT_SECRET:your-super-secret-key-min-32-characters-long}
  issuer: DeltaHomesApi
  audience: DeltaHomesClient
  expiration-minutes: 1440
  refresh-expiration-days: 7

twilio:
  account-sid: ${TWILIO_ACCOUNT_SID:}
  auth-token: ${TWILIO_AUTH_TOKEN:}
  phone-number: ${TWILIO_PHONE_NUMBER:}
  dev-mode-logging: true

mail:
  host: ${MAIL_HOST:smtp.example.com}
  port: 587
  username: ${MAIL_USERNAME:}
  password: ${MAIL_PASSWORD:}
  from: noreply@deltahomes.app

app:
  base-url: ${APP_BASE_URL:http://localhost:8080}
  cors:
    allowed-origins: ${CORS_ORIGINS:http://localhost:3000}
  otp:
    expiry-minutes: 5
    max-attempts: 5
    max-sends-per-window: 5
    resend-cooldown-seconds: 60
  admin:
    phone: ${ADMIN_PHONE:}
    name: Delta Admin
    password: ${ADMIN_PASSWORD:admin123}
  seed:
    enabled: ${SEED_ENABLED:true}

logging:
  level:
    root: INFO
    com.deltahomes: DEBUG
    org.springframework.security: DEBUG
  pattern:
    console: "%d{yyyy-MM-dd HH:mm:ss} [%thread] %-5level %logger{36} - %msg%n"
```

---

## 5. Security Configuration

### 5.1 JWT Filter Configuration

```java
@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {
    
    private final JwtAuthenticationFilter jwtAuthFilter;
    private final AuthenticationProvider authenticationProvider;
    
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(AbstractHttpConfigurer::disable)
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .sessionManagement(session -> 
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authenticationProvider(authenticationProvider)
            .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/v1/auth/**").permitAll()
                .requestMatchers("/api/v1/properties", "/api/v1/properties/**").permitAll()
                .requestMatchers("/api/v1/companies", "/api/v1/companies/**").permitAll()
                .requestMatchers("/api/v1/cities", "/api/v1/districts", 
                               "/api/v1/services", "/api/v1/features", "/api/v1/plans").permitAll()
                .requestMatchers("/api/v1/reviews", "/api/v1/reviews/**").permitAll()
                .requestMatchers("/api/v1/search").permitAll()
                .requestMatchers("/api/v1/admin/**").hasRole("ADMIN")
                .requestMatchers("/health").permitAll()
                .anyRequest().authenticated()
            );
        
        return http.build();
    }
    
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(
            Arrays.asList(appConfig.getCors().getAllowedOrigins().split(","))
        );
        configuration.setAllowedMethods(
            Arrays.asList("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS")
        );
        configuration.setAllowedHeaders(Collections.singletonList("*"));
        configuration.setAllowCredentials(true);
        
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/api/**", configuration);
        return source;
    }
}
```

### 5.2 Password Encoding

```java
@Bean
public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder(10);
}
```

---

## 6. Validation Configuration

### 6.1 Jakarta Bean Validation Examples

```java
public record RegisterRequest(
    @NotBlank(message = "{validation.phone.required}")
    @Pattern(regexp = "^01[0-9]{9}$", message = "{validation.phone.egyptian}")
    String phone,
    
    @Email(message = "{validation.email.invalid}")
    String email,
    
    @NotBlank(message = "{validation.password.required}")
    @Size(min = 8, max = 100, message = "{validation.password.length}")
    String password,
    
    @NotBlank(message = "{validation.name.required}")
    @Size(max = 100, message = "{validation.name.length}")
    String name
) {}
```

### 6.2 Messages Properties

```properties
# messages_ar.properties
validation.phone.required=رقم الهاتف مطلوب
validation.phone.egyptian=رقم الهاتف المحمول يجب أن يكون برقم مصري صحيح
validation.email.invalid=البريد الإلكتروني غير صالح
validation.password.required=كلمة المرور مطلوبة
validation.password.length=كلمة المرور يجب أن تكون بين 8 و 100 حرف
validation.name.required=الاسم مطلوب
validation.name.length=الاسم يجب ألا يتجاوز 100 حرف
error.validation=حدث خطأ أثناء التحقق من صحة البيانات المدخلة

# messages_en.properties
validation.phone.required=Phone number is required
validation.phone.egyptian=Phone number must be a valid Egyptian mobile number
validation.email.invalid=Invalid email address
validation.password.required=Password is required
validation.password.length=Password must be between 8 and 100 characters
validation.name.required=Name is required
validation.name.length=Name must not exceed 100 characters
error.validation=Validation error occurred
```

---

## 7. Database Configuration

### 7.1 Entity Auditing

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
    }
}
```

### 7.2 Enable JPA Auditing

```java
@Configuration
@EnableJpaAuditing
public class JpaConfig {
}
```

---

## 8. Testing Configuration

### 8.1 Test Application Properties

```yaml
# src/test/resources/application-test.yml
spring:
  datasource:
    url: jdbc:tc:sqlserver:2022:///testdb
    driver-class-name: org.testcontainers.jdbc.ContainerDatabaseDriver
  jpa:
    hibernate:
      ddl-auto: create-drop
  flyway:
    enabled: false
```

### 8.2 Integration Test Example

```java
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
@ActiveProfiles("test")
class AuthControllerIntegrationTest {
    
    @Container
    static MSSQLServerContainer<?> sqlServer = new MSSQLServerContainer<>("mcr.microsoft.com/mssql/server:2022-latest");
    
    @Autowired
    private TestRestTemplate restTemplate;
    
    @Test
    void register_WithValidData_ReturnsCreated() {
        RegisterRequest request = new RegisterRequest(
            "01234567890",
            "test@example.com",
            "password123",
            "Test User"
        );
        
        ResponseEntity<AuthResponse> response = restTemplate.postForEntity(
            "/api/v1/auth/register",
            request,
            AuthResponse.class
        );
        
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody().token()).isNotNull();
    }
}
```

---

## 9. Health Check Configuration

```yaml
management:
  endpoints:
    web:
      exposure:
        include: health,info
  endpoint:
    health:
      show-details: when-authorized
```

Access health endpoint at: `GET /actuator/health`

---

## 10. Build & Deployment

### 10.1 Maven Configuration (pom.xml)

```xml
<parent>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-parent</artifactId>
    <version>3.2.0</version>
</parent>

<properties>
    <java.version>17</java.version>
</properties>

<dependencies>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-web</artifactId>
    </dependency>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-data-jpa</artifactId>
    </dependency>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-security</artifactId>
    </dependency>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-validation</artifactId>
    </dependency>
    <dependency>
        <groupId>com.microsoft.sqlserver</groupId>
        <artifactId>mssql-jdbc</artifactId>
    </dependency>
    <dependency>
        <groupId>io.jsonwebtoken</groupId>
        <artifactId>jjwt-api</artifactId>
        <version>0.12.3</version>
    </dependency>
</dependencies>
```

### 10.2 Build Commands

```bash
# Build with Maven
./mvnw clean package

# Run tests
./mvnw test

# Run application
./mvnw spring-boot:run

# Build Docker image
docker build -t deltahomes-backend:latest .
```
