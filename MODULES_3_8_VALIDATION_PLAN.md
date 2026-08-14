# Delta Homes Backend - Modules 3-8 Validation & Completion Plan

This document provides a detailed plan for validating and completing Modules 3 through 8 of the Delta Homes backend project. It follows the same approach used for Modules 0-2: validate against specifications, identify gaps, implement missing pieces following existing patterns, and verify build success.

## Overall Approach

Following the modular, incremental build approach specified in `00-master.md`:

1. **Validate each module against its specification** (`03-lookups.md` through `09-chat.md`)
2. **Identify gaps** in endpoints, DTOs, entities, repositories, services, and configuration
3. **Implement missing pieces** following existing patterns in the codebase
4. **Verify build success** and basic functionality
5. **Proceed sequentially** (Module 3 → 4 → 5 → 6 → 7 → 8) respecting dependencies

Each module specification follows a consistent structure:
- Scope (In/Out)
- Endpoints (with exact paths, methods, params, responses)
- DTOs (Request/Response)
- Entity Models
- Service Implementation
- Repository Interfaces
- Definition of Done

## Module-by-Module Validation Checklist

### �� 📋 **Module 3: Properties** (`04-properties.md`)
**Dependencies:** Stage 0 (Foundation), Stage 1 (Auth)
**Effort:** L (Large)

#### � ✅ Endpoints to Verify (all under `/api/v1/properties`):
| Method & Path | Auth | Params | Response |
|---------------|------|--------|----------|
| `GET /` | — | `q?`, `purpose?`, `propertyType?`, `priceMin?`, `priceMax?`, `rooms?`, `baths?`, `cityId?`, `listingType?`, `sort?`, `page?`, `size?` | `Paginated<PropertySummary>` |
| `GET /{id}` | — | — | `PropertyDetails` |
| `POST /` | � ✔ | `CreatePropertyRequest` | `PropertyDetails` |
| `PUT /{id}` | � ✔ | `UpdatePropertyRequest` | `PropertyDetails` |
| `DELETE /{id}` | � ✔ | — | `MessageResponse` |
| `GET /{id}/similar` | — | `limit?` | `List<PropertySummary>` |
| `GET /{id}/suggested-price` | — | — | `SuggestedPriceResponse` |

#### �� 📋 DTOs to Verify:
**Request DTOs:**
- `CreatePropertyRequest`: purpose, propertyType, listingType, price, rooms, baths, landArea, builtArea, title, description, address, cityId, photos, features, services
- `UpdatePropertyRequest`: Similar to Create but with optional fields
- `SuggestedPriceRequest`: propertyId

**Response DTOs:**
- `PropertySummary`: id, title, price, currency, propertyType, purpose, cityName, mainPhotoUrl, rooms, baths, landArea, builtArea, listingType, status
- `PropertyDetails`: All PropertySummary fields + description, address, photos, features, services, owner (UserSummary), createdAt, updatedAt
- `PropertyPhotoSummary`: id, url, caption, isPrimary
- `PropertyFeatureSummary`: id, name, icon
- `PropertyServiceSummary`: id, name, icon
- `SuggestedPriceResponse`: suggestedPrice, confidence, comparableCount
- `MessageResponse`: message

#### �� 🏗��️ Entities to Verify:
- **Property**: Basic info, location, pricing, features, relationships, auditing
- **PropertyPhoto**: Property relationship, URL, caption, ordering
- **PropertyFeature**: Property relationship, feature details
- **PropertyService**: Property relationship, service details
- *Check for proper use of OffsetDateTime, enums, relationships, indexes*

#### �� 🔧 Service & Repository Patterns to Follow:
- Use existing `PageUtils.normalizeSort()` for sort parameter handling
- Implement FTS search using `websearch_to_tsquery('simple', :q) @@ search_vector`
- Use constructor expressions in repository queries for DTO projection
- Follow existing `@Transactional` patterns for write operations
- Use existing exception handling (BusinessException, ResourceNotFoundException)

#### �� 📝 Definition of Done:
- [ ] All 7 endpoints live with exact paths/methods/auth/status codes
- [ ] Property, PropertyPhoto, PropertyFeature, PropertyService entities with correct fields
- [ ] Full-text search with optional equality filters working
- [ ] DTO summaries never leak sensitive data (no passwords, hashes, etc.)
- [ ] Seed data loaded idempotently
- [ ] Unit + integration tests passing

---

### �� 📋 **Module 4: Companies** (`05-companies.md`)
**Dependencies:** Stage 0 (Foundation), Stage 1 (Auth), Stage 3 (Properties)
**Effort:** L (Large)

#### � ✅ Endpoints to Verify (all under `/api/v1/companies`):
| Method & Path | Auth | Params | Response |
|---------------|------|--------|----------|
| `GET /` | — | `q?`, `followed?`, `sort?`, `page?`, `size?` | `Paginated<CompanySummary>` |
| `GET /{id}` | — | — | `CompanyDetails` |
| `POST /` | � ✔ | `CreateCompanyRequest` | `CompanyDetails` |
| `PUT /{id}` | � ✔ | `UpdateCompanyRequest` | `CompanyDetails` |
| `DELETE /{id}` | � ✔ | — | `MessageResponse` |
| `POST /{id}/follow` | � ✔ | — | `MessageResponse` |
| `DELETE /{id}/follow` | � ✔ | — | `MessageResponse` |
| `GET /{id}/followers` | — | `page?`, `size?` | `Paginated<UserSummary>` |

#### �� 📋 DTOs to Verify:
**Request DTOs:**
- `CreateCompanyRequest`: name, description, type, logoUrl, coverUrl, phone, whatsapp, email, website, verified, reputationScore, coverageArea
- `UpdateCompanyRequest`: Similar to Create but with optional fields

**Response DTOs:**
- `CompanySummary`: id, name, type, logoUrl, coverUrl, phone, whatsapp, email, website, verified, followersCount, reputationScore
- `CompanyDetails`: All CompanySummary fields + description, verification, coverageArea, portfolio (PropertySummary), services (CompanyServiceSummary), staff (CompanyStaffSummary), createdAt, updatedAt
- `CompanyServiceSummary`: id, name (service name), category, iconUrl
- `CompanyStaffSummary`: id, name, role, user (UserSummary)

#### �� 🏗��️ Entities to Verify:
- **Company**: Basic info, contact details, metrics, relationships
- **CompanyService**: Company relationship, service details
- **CompanyStaff**: Company relationship, user relationship, role
- **CompanyPortfolio**: Company relationship, property relationship
- *Check for proper enum usage (CompanyType), relationships, validation*

#### �� 🔧 Service & Repository Patterns to Follow:
- Follow existing patterns for follow/unfollow functionality (similar to other social features)
- Use existing pagination and sorting utilities
- Implement proper relationship loading to avoid N+1 queries
- Use existing validation patterns for company data

#### �� 📝 Definition of Done:
- [ ] All 8 endpoints live with exact paths/methods/auth/status codes
- [ ] Company, CompanyService, CompanyStaff, CompanyPortfolio entities with correct fields
- [ ] Follow/unfollow functionality working correctly
- [ ] DTO summaries never leak sensitive data
- [ ] Seed data loaded idempotently
- [ ] Unit + integration tests passing

---

### �� 📋 **Module 5: Saved Items** (`06-saved-items.md`)
**Dependencies:** Stage 0 (Foundation), Stage 1 (Auth)
**Effort:** S (Small)

#### � ✅ Endpoints to Verify (all under `/api/v1/saved-items`):
| Method & Path | Auth | Params | Response |
|---------------|------|--------|----------|
| `GET /` | � ✔ | `entityType?`, `page?`, `size?` | `Paginated<SavedItemSummary>` |
| `POST /` | � ✔ | `entityType`, `entityId` | `SavedItemDetails` |
| `DELETE /{id}` | � ✔ | — | `MessageResponse` |

#### �� 📋 DTOs to Verify:
**Request DTOs:**
- `SavedItemRequest`: entityType, entityId

**Response DTOs:**
- `SavedItemSummary`: id, entityType, entityId, entity (polymorphic summary - PropertySummary/CompanySummary/etc.), savedAt
- `SavedItemDetails`: All SavedItemSummary fields + entity (detailed polymorphic object)
- `MessageResponse`: message

#### �� 🏗��️ Entities to Verify:
- **SavedItem**: User relationship, entityType (enum), entityId (UUID for polymorphic association), savedAt
- *Check for proper use of OffsetDateTime, enum for entityType, polymorphic association handling*

#### �� 🔧 Service & Repository Patterns to Follow:
- Follow existing patterns for polymorphic associations (similar to how other entities handle relationships)
- Use existing authentication patterns (`@AuthenticationPrincipal`)
- Implement proper error handling for invalid entityType/entityId combinations
- Follow existing pagination patterns

#### �� 📝 Definition of Done:
- [ ] All 3 endpoints live with exact paths/methods/auth/status codes
- [ ] SavedItem entity with correct fields (userId, entityType, entityId, savedAt)
- [ ] Polymorphic association handling working for Property, Company, etc.
- [ ] DTO summaries never leak sensitive data
- [ ] Seed data loaded idempotently
- [ ] Unit + integration tests passing

---

### �� 📋 **Module 6: Reviews** (`07-reviews.md`)
**Dependencies:** Stage 0 (Foundation), Stage 1 (Auth)
**Effort:** S (Small)

#### � ✅ Endpoints to Verify (all under `/api/v1/reviews`):
| Method & Path | Auth | Params | Response |
|---------------|------|--------|----------|
| `GET /` | — | `entityType?`, `entityId?`, `page?`, `size?` | `Paginated<ReviewSummary>` |
| `GET /{id}` | — | — | `ReviewDetails` |
| `POST /` | � ✔ | `entityType`, `entityId`, `rating`, `comment` | `ReviewDetails` |
| `PUT /{id}` | � ✔ | `rating`, `comment` | `ReviewDetails` |
| `DELETE /{id}` | � ✔ | — | `MessageResponse` |

#### �� 📋 DTOs to Verify:
**Request DTOs:**
- `CreateReviewRequest`: entityType, entityId, rating (1-5), comment
- `UpdateReviewRequest`: rating (1-5), comment

**Response DTOs:**
- `ReviewSummary`: id, entityType, entityId, entity (summary), user (UserSummary), rating, comment, createdAt
- `ReviewDetails`: All ReviewSummary fields + entity (detailed object), updatedAt
- `MessageResponse`: message

#### �� 🏗��️ Entities to Verify:
- **Review**: User relationship, entityType (enum), entityId (UUID), rating, comment, createdAt, updatedAt
- *Check for proper validation (rating 1-5), OffsetDateTime usage, enum for entityType*

#### �� 🔧 Service & Repository Patterns to Follow:
- Follow existing patterns for review/rating systems
- Use existing authentication and validation patterns
- Implement proper ownership checks (users can only edit/delete their own reviews)
- Use existing pagination patterns

#### �� 📝 Definition of Done:
- [ ] All 5 endpoints live with exact paths/methods/auth/status codes
- [ ] Review entity with correct fields (userId, entityType, entityId, rating, comment, createdAt, updatedAt)
- [ ] Ownership enforcement (users can only modify/delete their own reviews)
- [ ] DTO summaries never leak sensitive data
- [ ] Seed data loaded idempotently
- [ ] Unit + integration tests passing

---

### �� 📋 **Module 7: Appointments** (`08-appointments.md`)
**Dependencies:** Stage 0 (Foundation), Stage 1 (Auth), Stage 3 (Properties)
**Effort:** L (Large)

#### � ✅ Endpoints to Verify (all under `/api/v1/appointments`):
| Method & Path | Auth | Params | Response |
|---------------|------|--------|----------|
| `GET /` | � ✔ | `role?`, `status?`, `sort?`, `page?`, `size?` | `Paginated<AppointmentSummary>` |
| `GET /{id}` | � ✔ | — | `AppointmentDetails` |
| `POST /` | — | `propertyId`, `customerId`, `requestedSlot`, `note` | `AppointmentDetails` |
| `PUT /{id}/status` | � ✔ | `status` | `AppointmentDetails` |
| `DELETE /{id}` | � ✔ | — | `MessageResponse` |
| `GET /{id}/timeline` | � ✔ | — | `List<AppointmentTimelineEvent>` |

#### �� 📋 DTOs to Verify:
**Request DTOs:**
- `CreateAppointmentRequest`: propertyId, customerId, requestedSlot, note
- `UpdateAppointmentStatusRequest`: status (enum: PENDING, CONFIRMED, CANCELLED, COMPLETED, NO_SHOW)

**Response DTOs:**
- `AppointmentSummary`: id, property (PropertySummary), customer (UserSummary), owner (UserSummary), requestedSlot, status, createdAt
- `AppointmentDetails`: All AppointmentSummary fields + note, timeline (AppointmentTimelineEvent), updatedAt
- `AppointmentTimelineEvent`: timestamp, status, note
- `MessageResponse`: message

#### �� 🏗��️ Entities to Verify:
- **Appointment**: Property relationship, Customer relationship, Owner relationship, requestedSlot (OffsetDateTime), status (enum), note, createdAt, updatedAt
- *Check for proper enum usage (AppointmentStatus), OffsetDateTime for timestamps, relationships*

#### �� 🔧 Service & Repository Patterns to Follow:
- Follow existing patterns for state machine/status transitions
- Use existing validation for date/time slots (future-only, business hours, etc.)
- Implement proper authorization checks (customers, owners, admins)
- Use existing relationship loading patterns to avoid N+1 queries
- Follow existing exception handling for business rules (conflicting appointments, etc.)

#### �� 📝 Definition of Done:
- [ ] All 6 endpoints live with exact paths/methods/auth/status codes
- [ ] Appointment entity with correct fields (propertyId, customerId, ownerId, requestedSlot, status, note, createdAt, updatedAt)
- [ ] State machine enforcement (valid status transitions)
- [ ] Proper authorization (customers create, owners confirm, both can view)
- [ ] DTO summaries never leak sensitive data
- [ ] Seed data loaded idempotently
- [ ] Unit + integration tests passing

---

### �� 📋 **Module 8: Chat** (`09-chat.md`)
**Dependencies:** Stage 0 (Foundation), Stage 1 (Auth)
**Effort:** L (Large)

#### � ✅ Endpoints to Verify (all under `/api/v1/chat`):
| Method & Path | Auth | Params | Response |
|---------------|------|--------|----------|
| `GET /conversations` | � ✔ | `page?`, `size?` | `Paginated<ConversationSummary>` |
| `GET /conversations/{id}` | � ✔ | — | `ConversationDetails` |
| `POST /conversations` | � ✔ | `participantIds` | `ConversationDetails` |
| `GET /conversations/{id}/messages` | � ✔ | `page?`, `size?` | `Paginated<MessageSummary>` |
| `POST /conversations/{id}/messages` | � ✔ | `content` | `MessageDetails` |
| `DELETE /conversations/{id}` | � ✔ | — | `MessageResponse` |
| `POST /conversations/{id}/read` | � ✔ | — | `MessageResponse` |

#### �� 📋 DTOs to Verify:
**Request DTOs:**
- `CreateConversationRequest`: participantIds (List<UUID>)
- `CreateMessageRequest`: content

**Response DTOs:**
- `ConversationSummary`: id, participants (UserSummary), lastMessage (MessageSummary), unreadCount, createdAt, updatedAt
- `ConversationDetails`: All ConversationSummary fields + participants (detailed UserSummary), messages (Page<MessageSummary>)
- `MessageSummary`: id, conversationId, sender (UserSummary), content, sentAt, readBy (List<UserSummary>)
- `MessageDetails`: All MessageSummary fields + deliveredAt, readAt
- `MessageResponse`: message

#### �� 🏗��️ Entities to Verify:
- **Conversation**: Participants (many-to-many with User), createdAt, updatedAt
- **Message**: Conversation relationship, Sender relationship, content, sentAt, readBy (many-to-many with User), deliveredAt, readAt
- *Check for proper OffsetDateTime usage, many-to-many relationships, indexes for performance*

#### �� 🔧 Service & Repository Patterns to Follow:
- Follow existing patterns for messaging/chat systems
- Use existing authentication and pagination patterns
- Implement proper read receipts and delivery tracking
- Use efficient querying for recent messages (avoid loading entire conversation history)
- Use existing relationship handling patterns
- Consider performance implications of many-to-many relationships

#### �� 📝 Definition of Done:
- [ ] All 7 endpoints live with exact paths/methods/auth/status codes
- [ ] Conversation and Message entities with correct fields and relationships
- [ ] Real-time messaging simulation (polling-based acceptable for MVP)
- [ ] Read receipts and delivery tracking working
- [ ] DTO summaries never leak sensitive data
- [ ] Seed data loaded idempotently
- [ ] Unit + integration tests passing

---

## Common Implementation Patterns to Reuse

Based on examination of the existing codebase, consistently use these patterns:

### 1. **DTO Projection in Repositories**
```java
@Query("""
    SELECT NEW com.deltahomes.backend.dto.summary.PropertySummary(
        p.id, p.title, p.price, p.currency, p.propertyType, p.purpose,
        c.name AS cityName, p.mainPhotoUrl, p.rooms, p.baths,
        p.landArea, p.builtArea, p.listingType, p.status
    )
    FROM Property p
    JOIN p.city c
    WHERE (:q = '' OR websearch_to_tsquery('simple', :q) @@ p.search_vector)
    """)
Page<PropertySummary> searchByFullText(
    @Param("q") String query,
    @Param("cityId") UUID cityId,
    Pageable pageable);
```

### 2. **Sort Parameter Normalization**
```java
Pageable normalized = PageUtils.normalizeSort(pageable);
return repository.searchIndex(query, normalized);
```

### 3. **Exception Handling**
```java
if (condition) {
    throw new BusinessException("Error message key"); // Uses MessageSource for i18n
}
```

### 4. **Transactional Service Methods**
```java
@Transactional
public ResponseEntity<?> createSomething(@Valid @RequestBody CreateRequest request) {
    // Validation
    // Entity creation
    // Repository save
    // Return DTO response
}
```

### 5. **Controller Structure**
```java
@RestController
@RequestMapping("/api/v1/properties")
@RequiredArgsConstructor
public class PropertyController {

    private final PropertyService propertyService;

    // GET endpoints
    @GetMapping("")
    public ResponseEntity<PaginatedResponse<PropertySummary>> searchProperties(...) {
        return ResponseEntity.ok(propertyService.searchProperties(...));
    }
    
    // POST endpoints
    @PostMapping("")
    public ResponseEntity<ResponseEntity<PropertyDetails>> createProperty(...) {
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(propertyService.createProperty(...));
    }
    
    // etc.
}
```

### 6. **Entity Auditing**
Ensure all entities extend `BaseEntity` which provides:
- `createdAt` (OffsetDateTime)
- `updatedAt` (OffsetDateTime)
- AuditingEntityListener for automatic population

### 7. **Validation Annotations**
Use Jakarta Bean Validation consistently:
```java
@NotBlank(message = "{validation.field.required}")
@Size(max = 100, message = "{validation.field.length}")
@Pattern(regexp = "^pattern$", message = "{validation.field.pattern}")
```

### 8. **Response Wrapping**
Always wrap responses in appropriate DTOs:
- Single entity: Return the DTO directly
- Collections: Use `PaginatedResponse<T>` for paginated results
- Status messages: Use `MessageResponse`
- Never return raw entities or internal data structures

## Definition of Done Checklist (Per Module)

Before marking a module as complete, verify:

### �� 🔹 **Functional Completeness**
- [ ] All specified endpoints implemented with exact:
  - HTTP methods (GET, POST, PUT, DELETE)
  - Path patterns (including path variables and query parameters)
  - Authentication requirements (��✔ for protected, — for public)
  - Request/response DTO structures
  - HTTP status codes (200, 201, 204, 400, 401, 403, 404, etc.)

### �� 🔹 **Data Integrity**
- [ ] JPA entities match specification exactly:
  - Field names, types, lengths, nullability
  - Enum usage (EnumType.STRING where specified)
  - Relationships (cardinality, fetch types, cascade rules)
  - Indexes for query performance
  - Proper use of OffsetDateTime for all timestamps
- [ ] DTOs never expose sensitive data:
  - Password hashes, tokens, internal IDs, etc.
  - Use summary DTOs for lists, detailed DTOs for individual items
  - Never return raw entities in API responses

### �� 🔹 **Business Logic**
- [ ] Service methods implement specified business rules:
  - Validation logic (input validation, business rule validation)
  - Authorization checks (who can do what)
  - Data consistency (transactions where needed)
  - Edge case handling (empty results, invalid inputs, etc.)
- [ ] Repository methods implement specified query logic:
  - Full-text search with `websearch_to_tsquery`
  - Filtering and pagination
  - Sorting normalization
  - Proper join strategies to avoid N+1 problems

### �� 🔹 **Configuration & Infrastructure**
- [ ] Spring beans properly configured (@Service, @Repository, etc.)
- [ ] Validation annotations present and correct
- [ ] Exception handling consistent with rest of application
- [ ] Logging where appropriate (service layer for business logic)
- [ ] Seed data considerations (if module requires lookup data)

### �� 🔹 **Quality & Testing**
- [ ] Code compiles without warnings
- [ ] Follows existing code style and patterns
- [ ] No TODOs or FIXMEs left in implementation
- [ ] Unit tests for service layer (if creating new logic)
- [ ] Integration tests for controller layer (if modifying existing)
- [ ] Manual verification of endpoint behavior (using curl, Postman, or similar)

### �� 🔹 **Documentation**
- [ ] Implementation matches specification document
- [ ] Any deviations documented and justified
- [ ] Clear separation of concerns (controller → service → repository)
- [ ] Meaningful method and variable names

## Technical Considerations & Tips

### 1. **Date-Time Handling**
- All timestamps must use `OffsetDateTime` (not `LocalDateTime` or `Date`)
- Format for JSON serialization: ISO-8601 with offset (e.g., `2023-06-15T14:30:00+02:00`)
- Already handled by Jackson configuration in `application.yml`

### 2. **Pagination**
- Use zero-based page indexing (page=0 is first page)
- Default size: 20 (configurable via `size` parameter)
- Always validate and sanitize page/size parameters
- Use `PageUtils.normalizeSort()` for sort parameter handling

### 3. **Full-Text Search**
- Uses PostgreSQL `@@` operator with `websearch_to_tsquery('simple', :q)`
- Empty query (`:q = ''`) should return all results (no filtering)
- Search vector should be pre-computed and indexed on entities
- Follow existing pattern: `(CAST(:q AS text) = '' OR websearch_to_tsquery('simple', :q) @@ [entity].search_vector)`

### 4. **Enum Handling**
- All specification enums use `EnumType.STRING` for database storage
- Frontend expects string values, not ordinals
- Use `@Enumerated(EnumType.STRING)` on all enum fields
- Validate enum values in DTOs where applicable

### 5. **Error Handling**
- Use `BusinessException` for validation/business rule violations
- Use `ResourceNotFoundException` for missing entities (extends BusinessException)
- Message codes should follow `{module.field.error}` pattern
- Actual messages resolved via MessageSource (already configured for i18n)

### 6. **Security**
- All protected endpoints should use `@AuthenticationPrincipal`
- Service layer should re-verify permissions (defense in depth)
- Never trust client-provided IDs for authorization checks
- Validate ownership before allowing modifications/deletions

### 7. **Performance**
- Avoid N+1 query problems with proper JOIN FETCH or entity graphs
- Use pagination for all list endpoints (even if spec doesn't explicitly require it)
- Consider caching for relatively static lookup data (modules 2-3)
- Index queried columns appropriately (already done in specifications)

## Troubleshooting Guide

### �� 🔧 **Compilation Errors**
- **Cannot find symbol**: Check imports and spelling
- **Method does not override**: Verify method signatures match interface/parent
- **Invalid release version**: Ensure JDK 21 is being used (per pom.xml)
- **Dependency issues**: Run `mvn dependency:tree` to check for conflicts

### �� 🔧 **Runtime Errors**
- **BeanDefinitionOverrideException**: Check for duplicate bean definitions (common with auditing)
- **NoSuchMethodError**: Check dependency versions, clean rebuild (`mvn clean compile`)
- **LazyInitializationException**: Use proper fetch plans or DTO projection to avoid lazy loading
- **QuerySyntaxError**: Check JPQL/HQL syntax, native queries for dialect compatibility

### �� 🔧 **Test Failures**
- **AssertionError**: Check actual vs expected values
- **ConstraintViolationException**: Validate input data against JPA constraints
- **DataIntegrityViolationException**: Check for missing required fields or duplicate unique constraints
- **TransactionRolledbackException**: Check for exceptions thrown within @Transactional methods

### �� 🔧 **API Issues**
- **400 Bad Request**: Check request body format, validation annotations
- **401 Unauthorized**: Check authentication headers, token validity
- **403 Forbidden**: Check authorization logic, role/permissions
- **404 Not Found**: Check path spelling, URL encoding, controller mapping
- **500 Internal Server Error**: Check server logs for stack trace

## Validation Workflow (Per Module)

Follow this sequence for each module:

1. **���📄 Review Specification**
   - Read the module specification document thoroughly
   - Note all endpoints, DTOs, entities, and special requirements

2. **���🔍 Existing Code Review**
   - Check what already exists for this module
   - Identify which components are missing vs. present but incorrect
   - Note any deviations from specification

3. **���📝 Gap Analysis**
   - Create checklist of missing/incorrect items
   - Prioritize by endpoints → DTOs → entities → repositories → services

4. **���🛠��️ Implementation**
   - Fix repository methods first (data access layer)
   - Update entities to match specification
   - Create/update DTOs as needed
   - Implement service layer business logic
   - Implement controller layer endpoint handling
   - Apply consistent patterns from existing code

5. **��✅ Verification**
   - Compile: `mvn compile`
   - Run tests: `mvn test` (or skip tests with `-DskipTests` if needed for temporary checking)
   - Manual endpoint verification (using curl, Postman, or browser)
   - Verify against Definition of Done checklist

6. **���🔄 Iterate**
   - Fix any issues found during verification
   - Re-run verification steps
   - Move to next module only when current module is complete

## Estimated Effort by Module

Based on the specification documents and patterns observed:

- **Module 3: Properties** - Large (L): Complex entity relationships, multiple DTOs, FTS search with many filters
- **Module 4: Companies** - Large (L): Similar to Properties but with social features (follow/unfollow)
- **Module 5: Saved Items** - Small (S): Relatively simple polymorphic association
- **Module 6: Reviews** - Small (S): Standard rating/comment system
- **Module 7: Appointments** - Large (L): Date/time slots, state machine, multiple relationships
- **Module 8: Chat** - Large (L): Real-time messaging simulation, many-to-many relationships, pagination

## Final Notes

1. **Preserve Existing Functionality**: When making changes, ensure you don't break existing working modules (0-2)
2. **Follow Established Patterns**: The codebase has clear conventions - emulate them rather than inventing new approaches
3. **Incremental Progress**: Validate and complete one module at a time
4. **Leverage Existing Infrastructure**: Use the existing exception handling, validation, pagination, and security systems
5. **Think About Performance**: Consider indexing, query efficiency, and payload sizes especially for list endpoints
6. **Maintain Consistency**: Ensure your implementation feels like a natural extension of the existing codebase

This plan provides a comprehensive roadmap for completing Modules 3-8. By following the specification-driven validation approach and reusing established patterns, you can achieve a consistent, high-quality implementation that integrates seamlessly with the existing foundation and auth modules.

---
*Document generated for use in subsequent development sessions. Based on analysis of specifications and existing codebase as of 2026-08-08.* 