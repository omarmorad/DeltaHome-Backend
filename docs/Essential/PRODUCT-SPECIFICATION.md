# Delta Homes — Product Specification & Business Logic Manual

> **كيف يجب أن يعمل المنتج؟ القواعد، السيناريوهات، حالات الاستثناء، معايير القبول.**  
> **Functional specifications, domain business rules, localization handling, state machine transitions, edge cases, and acceptance criteria.**

---

## 1. Authentication & Identity Management

### 1.1 Business Rules & Scenarios
- **Registration & Login**: Users register or log in using either a valid phone number (Egyptian format `^01[0-9]{9}$`) or a valid email address.
- **OTP Cycle**: OTP codes are 6-digit numeric strings. OTPs expire in exactly 5 minutes. Max 5 OTP requests per phone/email per window. Resend cooldown of 60 seconds between requests.
- **Password Security**: Passwords are hashed using BCrypt via Spring Security's `BCryptPasswordEncoder` (strength 10).
- **JWT Refresh Tokens**: Access tokens expire in 24 hours; refresh tokens expire in 7 days.

### 1.2 Localization & Language Resolution
- Mobile client app specifies culture via `Accept-Language: ar-EG` or `Accept-Language: en-US` header.
- Backend returns localized validation messages via Spring `MessageSource` and lookup labels based on the resolved request culture.

### 1.3 Exception Cases
- Supplying an expired or incorrect OTP returns `400 Bad Request` with localized error payload:
  - **Arabic (`ar-EG`)**: `"رمز التحقق غير صحيح أو منتهي الصلاحية."`
  - **English (`en-US`)**: `"Invalid or expired OTP code."`

### 1.4 Implementation Example

```java
@Service
@RequiredArgsConstructor
public class OtpService {
    
    private final OtpCodeRepository otpCodeRepository;
    private final MessageSource messageSource;
    
    public void verifyOtp(String identifier, String code, OtpPurpose purpose, Locale locale) {
        OtpCode otpCode = otpCodeRepository
            .findValidOtp(identifier, purpose, OffsetDateTime.now())
            .orElseThrow(() -> new BusinessException(
                messageSource.getMessage("otp.invalid", null, locale)
            ));
        
        String hashedCode = hashOtp(code);
        if (!MessageDigest.isEqual(hashedCode.getBytes(), otpCode.getCodeHash().getBytes())) {
            throw new BusinessException(
                messageSource.getMessage("otp.invalid", null, locale)
            );
        }
        
        otpCode.setIsUsed(true);
        otpCodeRepository.save(otpCode);
    }
}
```

---

## 2. Property Listing Domain & Search Logic

### 2.1 Listing Lifecycle State Machine

```
      [Create Listing]
             │
             ▼
        ┌──────────┐   Admin Approve    ┌───────────┐
        │  DRAFT   │ ─────────────────► │ PUBLISHED │
        └────┬─────┘                    └─────┬─────┘
             │                                │
             │ Admin Reject                   │ Mark Sold/Rented
             ▼                                ▼
       ┌───────────┐                    ┌───────────┐
       │ REJECTED  │                    │   SOLD /  │
       └───────────┘                    │  RENTED   │
                                        └───────────┘
```

- **DRAFT**: Listing is being edited by owner/broker. Not searchable publicly.
- **PUBLISHED**: Listing is live and indexed in Microsoft SQL Server Full-Text Catalog.
- **SOLD / RENTED**: Transaction finalized; listing retained for historical records.

### 2.2 Business Rules & Bilingual Content
- Listings accept titles and descriptions in Arabic (`titleAr`, `descriptionAr`) and/or English (`titleEn`, `descriptionEn`).
- If an English-only user views a listing without English text, the system falls back gracefully to Arabic text.
- **Listing Quotas**: Free individual users can publish up to 2 active listings simultaneously. Companies require active subscriptions for higher quotas.

### 2.3 Full-Text Search Implementation

```java
@Service
@RequiredArgsConstructor
public class SearchService {
    
    private final PropertyRepository propertyRepository;
    
    public PageResponse<PropertySummary> searchProperties(String query, Pageable pageable) {
        String searchPattern = "\"" + query + "*\"";
        Page<Property> results = propertyRepository.searchByFullText(searchPattern, pageable);
        return PageResponse.from(results.map(this::toSummary));
    }
}
```

---

## 3. Company Directory & Member Roles

### 3.1 Business Rules
- **Company Types**: `REAL_ESTATE_OFFICE`, `FINISHING_COMPANY`, `MAINTENANCE_PROVIDER`.
- **Company Member Roles**: `OWNER`, `MANAGER`, `AGENT`.
- **Follow Mechanics**: Users can follow/unfollow any verified company. Duplicate follow attempts are silently ignored.

### 3.2 Implementation Example

```java
@Entity
@Table(name = "companies")
public class Company extends BaseEntity {
    
    @Enumerated(EnumType.STRING)
    @Column(name = "type", length = 50, nullable = false)
    private CompanyType type;
    
    @Column(name = "verified", nullable = false)
    private Boolean verified = false;
    
    @Column(name = "followers_count", nullable = false)
    private Integer followersCount = 0;
}

@Service
public class FollowService {
    
    @Transactional
    public void followCompany(UUID userId, UUID companyId) {
        if (followerRepository.existsByUserIdAndCompanyId(userId, companyId)) {
            return; // Silently ignore duplicate follows
        }
        
        Follower follower = new Follower();
        follower.setUserId(userId);
        follower.setCompanyId(companyId);
        followerRepository.save(follower);
        
        companyRepository.incrementFollowersCount(companyId);
    }
}
```

---

## 4. Appointments & Viewing Workflows

### 4.1 Viewing State Machine

```
 [User Request] ──► PENDING ──┬──► ACCEPTED ───┬──► COMPLETED (Unlocks Review)
                             │                │
                             ├──► REJECTED    └──► CANCELLED
                             │
                             └──► CANCELLED
```

### 4.2 Rules & Localization
- Notifications generated during state changes (e.g. appointment confirmed) are localized in the target user's preferred language via Spring `MessageSource`.

### 4.3 Implementation Example

```java
@Service
@RequiredArgsConstructor
public class AppointmentService {
    
    private final AppointmentRepository appointmentRepository;
    private final NotificationService notificationService;
    
    @Transactional
    public void updateStatus(UUID appointmentId, AppointmentStatus newStatus, Locale locale) {
        Appointment appointment = appointmentRepository.findById(appointmentId)
            .orElseThrow(() -> new ResourceNotFoundException("Appointment", appointmentId));
        
        validateTransition(appointment.getStatus(), newStatus);
        
        appointment.setStatus(newStatus);
        appointmentRepository.save(appointment);
        
        // Send localized notification
        notificationService.notifyAppointmentUpdate(appointment, newStatus, locale);
    }
    
    private void validateTransition(AppointmentStatus current, AppointmentStatus newStatus) {
        // State machine validation logic
    }
}
```

---

## 5. Interaction-Gated Reviews & Ratings

### 5.1 Business Rules
- **Rating Scale**: 1 to 5 stars (integer), optional text review comment.
- **Interaction Gate**: Users can only review a property or company after a confirmed/completed viewing appointment.
- **Single Review Constraint**: One review per interaction entity per user.

### 5.2 Implementation Example

```java
@Service
@RequiredArgsConstructor
public class ReviewService {
    
    private final ReviewRepository reviewRepository;
    private final AppointmentRepository appointmentRepository;
    
    public void createReview(UUID userId, ReviewRequest request, Locale locale) {
        // Verify interaction gate
        boolean hasCompletedAppointment = appointmentRepository
            .existsByUserIdAndEntityIdAndStatus(
                userId, 
                request.getEntityId(), 
                AppointmentStatus.COMPLETED
            );
        
        if (!hasCompletedAppointment) {
            throw new BusinessException(
                messageSource.getMessage("review.interaction.required", null, locale)
            );
        }
        
        // Check for existing review
        if (reviewRepository.existsByUserIdAndEntityTypeAndEntityId(
            userId, request.getEntityType(), request.getEntityId())) {
            throw new BusinessException(
                messageSource.getMessage("review.already.exists", null, locale)
            );
        }
        
        Review review = new Review();
        review.setUserId(userId);
        review.setEntityType(request.getEntityType());
        review.setEntityId(request.getEntityId());
        review.setRating(request.getRating());
        review.setComment(request.getComment());
        
        reviewRepository.save(review);
    }
}
```

---

## 6. Real-Time Chat & Communications

### 6.1 Business Rules
- A chat room is created automatically when an inquiry is submitted.
- **Participant Access**: Only room participants can read or post messages.
- **Message Read Indicators**: Messages store `isRead` and `readAt` timestamps.

### 6.2 Entity Model

```java
@Entity
@Table(name = "messages")
public class Message extends BaseEntity {
    
    @Column(name = "conversation_id", nullable = false)
    private UUID conversationId;
    
    @Column(name = "sender_id", nullable = false)
    private UUID senderId;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "type", length = 50, nullable = false)
    private MessageType type;
    
    @Column(name = "text_body", columnDefinition = "nvarchar(max)")
    private String textBody;
    
    @Column(name = "media_url", length = 1000)
    private String mediaUrl;
    
    @Column(name = "is_read", nullable = false)
    private Boolean isRead = false;
    
    @Column(name = "read_at")
    private OffsetDateTime readAt;
}
```

---

## 7. Broadcast System & Quotas

### 7.1 Business Rules
- Broadcasts permit subscribed companies to push announcements to followers.
- **Quota Deduction**: Creating a broadcast deducts 1 from remaining subscription quota.
- **Bilingual Broadcasts**: Companies can compose broadcast messages in Arabic, English, or both.

### 7.2 Implementation Example

```java
@Service
@RequiredArgsConstructor
public class BroadcastService {
    
    private final BroadcastRepository broadcastRepository;
    private final SubscriptionService subscriptionService;
    
    @Transactional
    public Broadcast createBroadcast(UUID companyId, BroadcastRequest request, Locale locale) {
        // Check quota
        int remainingQuota = subscriptionService.getRemainingBroadcastQuota(companyId);
        if (remainingQuota <= 0) {
            throw new BusinessException(
                messageSource.getMessage("broadcast.quota.exceeded", null, locale)
            );
        }
        
        Broadcast broadcast = new Broadcast();
        broadcast.setCompanyId(companyId);
        broadcast.setTitleAr(request.getTitleAr());
        broadcast.setTitleEn(request.getTitleEn());
        broadcast.setBodyAr(request.getBodyAr());
        broadcast.setBodyEn(request.getBodyEn());
        broadcast.setType(request.getType());
        
        broadcastRepository.save(broadcast);
        
        // Deduct quota
        subscriptionService.deductBroadcastQuota(companyId);
        
        return broadcast;
    }
}
```

---

## 8. Commerce, Subscriptions & Coupons

### 8.1 Business Rules
- **Subscription Cycles**: Monthly (30 days) or Annual (365 days).
- **Coupon Rules**: Discount percentage or fixed amount in EGP, expiration date, max redemptions.

### 8.2 Entity Model

```java
@Entity
@Table(name = "coupons")
public class Coupon extends BaseEntity {
    
    @Column(name = "code", nullable = false, unique = true, length = 50)
    private String code;
    
    @Column(name = "discount_type", length = 20, nullable = false)
    private DiscountType discountType; // PERCENTAGE, FIXED
    
    @Column(name = "discount_value", nullable = false, precision = 10, scale = 2)
    private BigDecimal discountValue;
    
    @Column(name = "max_redemptions")
    private Integer maxRedemptions;
    
    @Column(name = "current_redemptions", nullable = false)
    private Integer currentRedemptions = 0;
    
    @Column(name = "expires_at")
    private OffsetDateTime expiresAt;
}
```

---

## 9. Arabic Admin Console & Acceptance Criteria

### 9.1 Dashboard Requirements
- The admin dashboard is rendered exclusively in **Arabic (`ar-EG`)** with full **Right-to-Left (RTL)** layout.
- Admins inspect identity verifications, review fraud reports, manage lookup taxonomies in Arabic, and review immutable `AuditLog` records.

### 9.2 Admin Controller Example

```java
@RestController
@RequestMapping("/api/v1/admin")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {
    
    private final AdminService adminService;
    
    @GetMapping("/users")
    public ResponseEntity<PageResponse<UserSummary>> getUsers(
            Pageable pageable,
            @RequestParam(required = false) String search) {
        return ResponseEntity.ok(adminService.getUsers(pageable, search));
    }
    
    @PutMapping("/verifications/{id}")
    public ResponseEntity<VerificationSummary> updateVerification(
            @PathVariable UUID id,
            @RequestBody @Valid VerificationDecisionRequest request,
            Locale locale) {
        return ResponseEntity.ok(adminService.updateVerification(id, request, locale));
    }
    
    @GetMapping("/audit-logs")
    public ResponseEntity<PageResponse<AuditLogSummary>> getAuditLogs(Pageable pageable) {
        return ResponseEntity.ok(adminService.getAuditLogs(pageable));
    }
}
```

---

## 10. Global Exception Handling

```java
@RestControllerAdvice
@RequiredArgsConstructor
public class GlobalExceptionHandler {
    
    private final MessageSource messageSource;
    
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ErrorResponse> handleBusinessException(
            BusinessException ex, Locale locale) {
        ErrorResponse error = new ErrorResponse(
            Instant.now(),
            HttpStatus.BAD_REQUEST.value(),
            ex.getMessage()
        );
        return ResponseEntity.badRequest().body(error);
    }
    
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleResourceNotFound(
            ResourceNotFoundException ex, Locale locale) {
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
    public ResponseEntity<ErrorResponse> handleValidationErrors(
            MethodArgumentNotValidException ex, Locale locale) {
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
}
```
