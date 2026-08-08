# Delta Homes — 01 · Stage 0 · Foundation (Skeleton & Cross-Cutting)

> **Stage 0.** Create the project skeleton and every cross-cutting
> concern from `00-master.md` so all later stages can be built on top. Nothing here is
> module-specific; every later stage depends on this stage.

**Status:** Parity · **Dependencies:** none · **Effort:** M

---

## 1. Deliverables (what "done" means for this stage)

1. Spring Boot project with main application, test structure, and admin dashboard placeholder.
2. API boots, serves `GET /actuator/health`, and applies the global pipeline (JSON, CORS, JWT
   wiring, exception handling, validation, Swagger/OpenAPI).
3. `BaseEntity` + JPA auditing configuration exist (entities come in their own stages).
4. Flyway migrations for FTS indexes, admin bootstrap, and seeder hooks are wired and
   idempotent — with no tables yet, it must run without error.
5. `Common/` (PageResponse, PagingParams, SortNormalizer, exceptions), `Security/` (JwtService, JwtConfig, CurrentUserAccessor), and DTO conventions are in place and unit-tested.
6. Admin dashboard placeholder renders a shell with a login page placeholder.
7. Config binding per `00-master.md §9`; env override test passes.

---

## 2. Project layout

```
deltahomes-backend/
├── src/main/java/com/deltahomes/
│   ├── controller/      (empty except HealthController)
│   ├── service/         (empty)
│   ├── repository/      (empty)
│   ├── entity/          BaseEntity.java
│   ├── enums/           (empty)
│   ├── dto/             (empty)
│   ├── config/
│   │   ├── SecurityConfig.java
│   │   ├── LocaleConfig.java
│   │   ├── JpaConfig.java
│   │   └── OpenApiConfig.java
│   ├── security/
│   │   ├── JwtService.java
│   │   ├── JwtConfig.java
│   │   ├── JwtAuthenticationFilter.java
│   │   └── CurrentUserAccessor.java
│   ├── exception/
│   │   ├── BusinessException.java
│   │   ├── ResourceNotFoundException.java
│   │   └── GlobalExceptionHandler.java
│   ├── util/
│   │   ├── PageResponse.java
│   │   ├── PagingParams.java
│   │   └── SortNormalizer.java
│   └── DeltaHomesApplication.java
├── src/main/resources/
│   ├── db/migration/
│   │   └── V1__Initial_Setup.sql
│   ├── messages_ar.properties
│   ├── messages_en.properties
│   └── application.yml
└── src/test/java/com/deltahomes/
    ├── unit/
    └── integration/
```

---

## 3. Application Configuration (application.yml)

```yaml
server:
  port: 8080

spring:
  application:
    name: delta-homes-backend
  
  datasource:
    url: jdbc:sqlserver://localhost:1433;databaseName=DeltaHomesDb;encrypt=true;trustServerCertificate=true
    username: sa
    password: ${DB_PASSWORD:}
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

management:
  endpoints:
    web:
      exposure:
        include: health,info
  endpoint:
    health:
      show-details: when-authorized

jwt:
  secret: ${JWT_SECRET:your-super-secret-key-min-32-characters-long}
  expiration-ms: 86400000
  refresh-expiration-ms: 604800000

app:
  base-url: ${APP_BASE_URL:http://localhost:8080}
  cors:
    allowed-origins: ${CORS_ORIGINS:http://localhost:3000}
```

---

## 4. BaseEntity Implementation

```java
package com.deltahomes.entity;

import jakarta.persistence.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.OffsetDateTime;
import java.util.UUID;

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
            createdAt = OffsetDateTime.now(java.time.ZoneOffset.UTC);
        }
    }
    
    // Getters and setters
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public OffsetDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(OffsetDateTime createdAt) { this.createdAt = createdAt; }
    public OffsetDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(OffsetDateTime updatedAt) { this.updatedAt = updatedAt; }
}
```

---

## 5. JPA Configuration

```java
package com.deltahomes.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@Configuration
@EnableJpaAuditing
public class JpaConfig {
}
```

---

## 6. Global Exception Handler

```java
package com.deltahomes.exception;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.Instant;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {
    
    @Autowired
    private MessageSource messageSource;
    
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ErrorResponse> handleBusinessException(BusinessException ex, Locale locale) {
        ErrorResponse error = new ErrorResponse(
            Instant.now(),
            HttpStatus.BAD_REQUEST.value(),
            ex.getMessage()
        );
        return ResponseEntity.badRequest().body(error);
    }
    
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleResourceNotFound(ResourceNotFoundException ex, Locale locale) {
        String message = messageSource.getMessage(
            "resource.not.found",
            new Object[]{ex.getResourceType(), ex.getResourceId()},
            locale
        );
        ErrorResponse error = new ErrorResponse(
            Instant.now(),
            HttpStatus.NOT_FOUND.value(),
            message
        );
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }
    
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationErrors(MethodArgumentNotValidException ex, Locale locale) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(error -> {
            String message = messageSource.getMessage(error, locale);
            errors.put(error.getField(), message);
        });
        
        ErrorResponse error = new ErrorResponse(
            Instant.now(),
            HttpStatus.BAD_REQUEST.value(),
            "VALIDATION_ERROR",
            messageSource.getMessage("error.validation", null, locale),
            errors
        );
        return ResponseEntity.badRequest().body(error);
    }
    
    public record ErrorResponse(
        Instant timestamp,
        int status,
        String error,
        String message,
        Map<String, String> validationErrors
    ) {
        public ErrorResponse(Instant timestamp, int status, String error) {
            this(timestamp, status, error, null, null);
        }
        
        public ErrorResponse(Instant timestamp, int status, String error, String message, Map<String, String> validationErrors) {
            this.timestamp = timestamp;
            this.status = status;
            this.error = error;
            this.message = message;
            this.validationErrors = validationErrors;
        }
    }
}
```

---

## 7. PageResponse & PagingParams

```java
package com.deltahomes.util;

import org.springframework.data.domain.Page;

import java.util.List;

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

```java
package com.deltahomes.util;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

public class PagingParams {
    private int page = 0;
    private int size = 20;
    private String sort = "createdAt,desc";
    
    public Pageable toPageable() {
        String[] sortParts = sort.split(",");
        String property = sortParts[0];
        Sort.Direction direction = sortParts.length > 1 
            ? Sort.Direction.fromString(sortParts[1]) 
            : Sort.Direction.DESC;
        return PageRequest.of(page, size, Sort.by(direction, property));
    }
    
    // Getters and setters
    public int getPage() { return page; }
    public void setPage(int page) { this.page = Math.max(0, page); }
    public int getSize() { return size; }
    public void setSize(int size) { this.size = Math.min(100, Math.max(1, size)); }
    public String getSort() { return sort; }
    public void setSort(String sort) { this.sort = sort; }
}
```

---

## 8. SortNormalizer

```java
package com.deltahomes.util;

import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component
public class SortNormalizer {
    
    private static final Set<String> DEFAULT_ALLOWED_FIELDS = Set.of(
        "createdAt", "updatedAt", "id"
    );
    
    public Sort normalize(Sort sort, Set<String> allowedFields) {
        return sort.stream()
            .filter(order -> allowedFields.contains(order.getProperty()))
            .map(order -> order.withProperty(toSnakeCase(order.getProperty())))
            .reduce(Sort.unsorted(), Sort::and);
    }
    
    public Sort normalize(Sort sort) {
        return normalize(sort, DEFAULT_ALLOWED_FIELDS);
    }
    
    private String toSnakeCase(String camelCase) {
        return camelCase.replaceAll("([a-z])([A-Z]+)", "$1_$2").toLowerCase();
    }
}
```

---

## 9. Security Configuration

```java
package com.deltahomes.config;

import com.deltahomes.security.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
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
                .requestMatchers("/actuator/health").permitAll()
                .requestMatchers("/swagger-ui/**", "/v3/api-docs/**").permitAll()
                .requestMatchers("/api/v1/properties", "/api/v1/properties/**").permitAll()
                .requestMatchers("/api/v1/companies", "/api/v1/companies/**").permitAll()
                .requestMatchers("/api/v1/cities", "/api/v1/districts", 
                               "/api/v1/services", "/api/v1/features").permitAll()
                .requestMatchers("/api/v1/reviews", "/api/v1/reviews/**").permitAll()
                .requestMatchers("/api/v1/search").permitAll()
                .requestMatchers("/api/v1/admin/**").hasRole("ADMIN")
                .anyRequest().authenticated()
            );
        
        return http.build();
    }
    
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(List.of("http://localhost:3000"));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(Collections.singletonList("*"));
        configuration.setAllowCredentials(true);
        
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/api/**", configuration);
        return source;
    }
}
```

---

## 10. Locale Configuration

```java
package com.deltahomes.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.i18n.AcceptHeaderLocaleResolver;
import org.springframework.web.servlet.i18n.LocaleChangeInterceptor;

import java.util.List;
import java.util.Locale;

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

---

## 11. Initial Flyway Migration

```sql
-- V1__Initial_Setup.sql

-- Create Full-Text Catalog
IF NOT EXISTS (SELECT * FROM sys.fulltext_catalogs WHERE name = 'DeltaHomesFtsCatalog')
BEGIN
    CREATE FULLTEXT CATALOG DeltaHomesFtsCatalog AS DEFAULT;
END
```

---

## 12. Tests (stage 0)

### Unit Tests

```java
@ExtendWith(MockitoExtension.class)
class SortNormalizerTest {
    
    @Test
    void normalize_shouldFilterUnallowedFields() {
        SortNormalizer normalizer = new SortNormalizer();
        Sort input = Sort.by("createdAt", "invalidField");
        Set<String> allowed = Set.of("created_at");
        
        Sort result = normalizer.normalize(input, allowed);
        
        assertThat(result.getOrderFor("created_at")).isNotNull();
        assertThat(result.getOrderFor("invalidField")).isNull();
    }
}
```

### Integration Test

```java
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
@ActiveProfiles("test")
class HealthEndpointIntegrationTest {
    
    @Container
    static MSSQLServerContainer<?> sqlServer = new MSSQLServerContainer<>("mcr.microsoft.com/mssql/server:2022-latest");
    
    @Autowired
    private TestRestTemplate restTemplate;
    
    @Test
    void healthEndpoint_shouldReturn200() {
        ResponseEntity<String> response = restTemplate.getForEntity("/actuator/health", String.class);
        
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }
}
```

---

## 13. Definition of Done (stage 0)

- [ ] Spring Boot project builds with zero warnings (`./mvnw clean package`).
- [ ] Health endpoint 200; Swagger loads (`/swagger-ui.html`).
- [ ] Pipeline (filters, JSON, CORS, JWT config) verified by tests.
- [ ] Flyway migrations idempotent across two runs.
- [ ] `JwtService` + `CurrentUserAccessor` unit tests green.
- [ ] Admin dashboard placeholder runs.
- [ ] Config env-override test green.
