# Delta Homes — 02 · Stage 1 · Auth & Identity (Authentication & Authorization)

> **Stage 1.** The first business module to build. Every other stage depends on it for
> identity, login, registration, OTP verification, JWT issuance, password change/reset,
> and the user-resolution plumbing used everywhere. This file is the **full** module
> spec — endpoints, DTOs, entities, rules, error strings, security, config, and tests.

**Status:** Parity · **Dependencies:** Stage 0 (foundation) · **Effort:** L

---

## 1. Scope

**In**
- Phone + email OTP send/verify, registration (phone & email), login (password phone &
  email, OTP phone & email), token refresh, `/me`, change password, password reset
  (phone & email), logout.
- JWT issuance/validation (access + refresh), principal→user resolution, Spring Security
  config, authorization wiring (admin role gate for `/admin/**`).
- Identity entities: `users`, `otp_codes`, `verifications`.

**Out (explicitly not this stage)**
- Google/Apple social login — **aspirational**, see `15-social-analytics.md`.
- Admin *console* endpoints (`/api/v1/admin/**`) — stage 12; only the role gate is wired here.
- Notifications, properties, etc. — later stages.

---

## 2. Endpoints (all under `/api/v1/auth`, exact)

| Method & Path | Auth | Request body / params | Response |
|---|---|---|---|
| `POST /otp/send` | — | `{ phone, purpose }` | `{ phone, expiresInMinutes, message }` |
| `POST /otp/verify` | — | `{ phone, code, purpose }` | `{ phone, verified: true }` |
| `POST /otp/send-email` | — | `{ email, purpose }` | `{ phone: <email>, expiresInMinutes, message }` |
| `POST /otp/verify-email` | — | `{ email, code, purpose }` | `{ phone: <email>, verified: true }` |
| `POST /register` | — | `{ name, phone, password, role, otpCode }` | **201** `AuthResponse` |
| `POST /register-email` | — | `{ name, email, password, role, otpCode }` | **201** `AuthResponse` |
| `POST /login` | — | `{ phone, password }` | `AuthResponse` |
| `POST /login/email` | — | `{ email, password }` | `AuthResponse` |
| `POST /login/otp` | — | `{ phone, otpCode }` | `AuthResponse` |
| `POST /login/otp/email` | — | `{ email, otpCode }` | `AuthResponse` |
| `POST /refresh` | — | `{ refreshToken }` | `AuthResponse` |
| `GET /me` | ✔ | — | `UserResponse` |
| `PUT /password` | ✔ | `{ currentPassword, newPassword }` | `{ message: "Password changed successfully" }` |
| `POST /password/reset` | — | `{ phone, otpCode, newPassword }` | `{ message: "Password reset successfully" }` |
| `POST /password/reset/email` | — | `{ email, otpCode, newPassword }` | `{ message: "Password reset successfully" }` |
| `POST /logout` | — | — | `{ message: "Logged out" }` |

---

## 3. DTOs

### Request DTOs

```java
// OTP Requests
public record OtpSendRequest(
    @NotBlank(message = "{validation.phone.required}")
    @Pattern(regexp = "^01[0-9]{9}$", message = "{validation.phone.egyptian}")
    String phone,
    
    @NotNull(message = "{validation.purpose.required}")
    OtpPurpose purpose
) {}

public record OtpVerifyRequest(
    String phone,
    @Pattern(regexp = "^[0-9]{6}$", message = "{validation.otp.digits}")
    String code,
    OtpPurpose purpose
) {}

public record OtpSendEmailRequest(
    @Email(message = "{validation.email.invalid}")
    String email,
    OtpPurpose purpose
) {}

public record OtpVerifyEmailRequest(
    @Email(message = "{validation.email.invalid}")
    String email,
    @Pattern(regexp = "^[0-9]{6}$", message = "{validation.otp.digits}")
    String code,
    OtpPurpose purpose
) {}

// Registration Requests
public record RegisterRequest(
    @NotBlank(message = "{validation.name.required}")
    @Size(max = 120, message = "{validation.name.length}")
    String name,
    
    @Pattern(regexp = "^01[0-9]{9}$", message = "{validation.phone.egyptian}")
    String phone,
    
    @Size(min = 6, max = 72, message = "{validation.password.length}")
    String password,
    
    @NotNull(message = "{validation.role.required}")
    UserRole role,
    
    @Pattern(regexp = "^[0-9]{6}$", message = "{validation.otp.digits}")
    String otpCode
) {}

public record RegisterEmailRequest(
    @NotBlank(message = "{validation.name.required}")
    @Size(max = 120, message = "{validation.name.length}")
    String name,
    
    @Email(message = "{validation.email.invalid}")
    @Size(max = 150, message = "{validation.email.length}")
    String email,
    
    @Size(min = 6, max = 72, message = "{validation.password.length}")
    String password,
    
    UserRole role,
    
    @Pattern(regexp = "^[0-9]{6}$", message = "{validation.otp.digits}")
    String otpCode
) {}

// Login Requests
public record LoginRequest(String phone, String password) {}
public record LoginEmailRequest(@Email String email, String password) {}
public record LoginOtpRequest(String phone, String otpCode) {}
public record LoginEmailOtpRequest(@Email String email, String otpCode) {}

// Other Requests
public record RefreshRequest(String refreshToken) {}
public record ChangePasswordRequest(String currentPassword, String newPassword) {}
public record ResetPasswordRequest(String phone, String otpCode, String newPassword) {}
public record ResetPasswordEmailRequest(String email, String otpCode, String newPassword) {}
```

### Response DTOs

```java
public record AuthResponse(
    String accessToken,
    String refreshToken,
    String tokenType,
    int expiresInSeconds,
    UserResponse user
) {}

public record UserResponse(
    UUID id,
    String name,
    String phone,
    String email,
    String photoUrl,
    UserRole role,
    UserStatus status,
    byte verificationLevel,
    OffsetDateTime createdAt
) {}

public record OtpResponse(String phone, int expiresInMinutes, String message) {}
public record OtpVerifyResponse(String phone, boolean verified) {}
public record MessageResponse(String message) {}
```

---

## 4. Entity Models

### User Entity

```java
@Entity
@Table(name = "users")
@EntityListeners(AuditingEntityListener.class)
public class User extends BaseEntity {
    
    @Column(name = "name", length = 120, nullable = false)
    private String name;
    
    @Column(name = "phone", length = 20, unique = true)
    private String phone;
    
    @Column(name = "email", length = 150, unique = true)
    private String email;
    
    @Column(name = "password_hash", length = 255, nullable = false)
    private String passwordHash;
    
    @Column(name = "photo_url", length = 255)
    private String photoUrl;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "role", length = 50, nullable = false)
    private UserRole role;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 50, nullable = false)
    private UserStatus status = UserStatus.ACTIVE;
    
    @Column(name = "verification_level", nullable = false)
    private byte verificationLevel = 0;
    
    @Column(name = "preferred_culture", length = 10)
    private String preferredCulture = "ar-EG";
    
    @Column(name = "last_login_at")
    private OffsetDateTime lastLoginAt;
    
    // Getters and setters
}
```

### OtpCode Entity

```java
@Entity
@Table(name = "otp_codes")
public class OtpCode extends BaseEntity {
    
    @Column(name = "identifier", length = 256, nullable = false)
    private String identifier; // phone or email
    
    @Column(name = "code_hash", length = 64, nullable = false)
    private String codeHash;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "purpose", length = 50, nullable = false)
    private OtpPurpose purpose;
    
    @Column(name = "expires_at", nullable = false)
    private OffsetDateTime expiresAt;
    
    @Column(name = "attempts", nullable = false)
    private int attempts = 0;
    
    @Column(name = "is_used", nullable = false)
    private boolean isUsed = false;
    
    // Getters and setters
}
```

---

## 5. OTP Service Implementation

```java
@Service
@RequiredArgsConstructor
public class OtpService {
    
    private final OtpCodeRepository otpCodeRepository;
    private final SmsService smsService;
    private final EmailService emailService;
    private final MessageSource messageSource;
    
    private static final int OTP_LENGTH = 6;
    private static final int MAX_ATTEMPTS = 5;
    private static final int MAX_SENDS_PER_WINDOW = 5;
    private static final int RESEND_COOLDOWN_SECONDS = 60;
    
    @Value("${app.otp.expiry-minutes:5}")
    private int expiryMinutes;
    
    @Value("${app.otp.max-sends-per-window:5}")
    private int maxSendsPerWindow;
    
    @Value("${app.admin.phone:}")
    private String adminPhone;
    
    @Value("${app.admin.permanent-otp:}")
    private String permanentOtp;
    
    public OtpResponse sendOtp(String recipient, OtpPurpose purpose, boolean isEmail) {
        // Check rate limiting
        OffsetDateTime windowStart = OffsetDateTime.now().minusMinutes(15);
        long recentSends = otpCodeRepository.countByIdentifierAndCreatedAtAfter(recipient, windowStart);
        
        if (recentSends >= maxSendsPerWindow) {
            throw new BusinessException(messageSource.getMessage("otp.too.many.requests", null, Locale.getDefault()));
        }
        
        // Check cooldown
        otpCodeRepository.findTopByIdentifierAndPurposeOrderByCreatedAtDesc(recipient, purpose)
            .ifPresent(existing -> {
                if (existing.getCreatedAt().plusSeconds(RESEND_COOLDOWN_SECONDS).isAfter(OffsetDateTime.now())) {
                    throw new BusinessException(messageSource.getMessage("otp.wait.cooldown", null, Locale.getDefault()));
                }
            });
        
        // Delete existing OTPs for this recipient/purpose
        otpCodeRepository.deleteByIdentifierAndPurpose(recipient, purpose);
        
        // Admin bypass
        if (recipient.equals(adminPhone) && permanentOtp != null && !permanentOtp.isBlank()) {
            log.info("[DEV MODE] OTP for admin {}: {}", maskRecipient(recipient), permanentOtp);
            return new OtpResponse(maskRecipient(recipient), expiryMinutes, "OTP sent");
        }
        
        // Generate and store OTP
        String code = generateOtp();
        OtpCode otpCode = new OtpCode();
        otpCode.setIdentifier(recipient);
        otpCode.setCodeHash(hashOtp(code));
        otpCode.setPurpose(purpose);
        otpCode.setExpiresAt(OffsetDateTime.now().plusMinutes(expiryMinutes));
        otpCodeRepository.save(otpCode);
        
        // Send via SMS or Email
        if (isEmail) {
            emailService.sendOtp(recipient, code);
        } else {
            smsService.sendOtp(recipient, code);
        }
        
        return new OtpResponse(maskRecipient(recipient), expiryMinutes, "OTP sent to " + maskRecipient(recipient));
    }
    
    public void verifyOtp(String recipient, String code, OtpPurpose purpose, Locale locale) {
        // Admin bypass
        if (recipient.equals(adminPhone) && permanentOtp != null && !permanentOtp.isBlank()) {
            if (code.equals(permanentOtp)) return;
        }
        
        OtpCode otpCode = otpCodeRepository
            .findValidOtp(recipient, purpose, OffsetDateTime.now())
            .orElseThrow(() -> new BusinessException(messageSource.getMessage("otp.invalid", null, locale)));
        
        if (!MessageDigest.isEqual(code.getBytes(), otpCode.getCodeHash().getBytes())) {
            otpCode.setAttempts(otpCode.getAttempts() + 1);
            if (otpCode.getAttempts() >= MAX_ATTEMPTS) {
                otpCodeRepository.delete(otpCode);
                throw new BusinessException(messageSource.getMessage("otp.max.attempts", null, locale));
            }
            otpCodeRepository.save(otpCode);
            throw new BusinessException(messageSource.getMessage("otp.invalid", null, locale));
        }
        
        otpCode.setUsed(true);
        otpCodeRepository.save(otpCode);
    }
    
    public void consumeOtp(String recipient, OtpPurpose purpose) {
        otpCodeRepository.deleteByIdentifierAndPurpose(recipient, purpose);
    }
    
    private String generateOtp() {
        return String.format("%06d", new Random().nextInt(999999));
    }
    
    private String hashOtp(String otp) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(otp.getBytes(StandardCharsets.UTF_8));
            return bytesToHex(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 not available", e);
        }
    }
    
    private String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }
    
    private String maskRecipient(String recipient) {
        if (recipient.contains("@")) {
            int atIndex = recipient.indexOf("@");
            if (recipient.length() > 6) {
                return recipient.substring(0, 2) + "***" + recipient.substring(atIndex);
            }
            return recipient;
        } else {
            return "****" + recipient.substring(Math.max(0, recipient.length() - 4));
        }
    }
}
```

---

## 6. JWT Service Implementation

```java
@Service
@RequiredArgsConstructor
public class JwtService {
    
    @Value("${jwt.secret}")
    private String secret;
    
    @Value("${jwt.expiration-ms:86400000}")
    private long expirationMs;
    
    @Value("${jwt.refresh-expiration-ms:604800000}")
    private long refreshExpirationMs;
    
    private Key getSigningKey() {
        byte[] keyBytes = Base64.getDecoder().decode(secret);
        if (keyBytes.length < 32) {
            keyBytes = secret.getBytes(StandardCharsets.UTF_8);
        }
        return Keys.hmacShaKeyFor(keyBytes);
    }
    
    public String generateToken(User user, boolean isRefresh) {
        long now = System.currentTimeMillis();
        long expiry = isRefresh ? refreshExpirationMs : expirationMs;
        
        return Jwts.builder()
            .subject(user.getPhone() != null ? user.getPhone() : user.getEmail())
            .claim("userId", user.getId().toString())
            .claim("role", user.getRole().name())
            .claim("type", isRefresh ? "refresh" : "access")
            .issuedAt(new Date(now))
            .expiration(new Date(now + expiry))
            .signWith(getSigningKey(), Jwts.SIG.HS256)
            .compact();
    }
    
    public boolean isTokenValid(String token, String expectedSubject) {
        try {
            Claims claims = Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
            
            String type = claims.get("type", String.class);
            if (type == null) return false;
            
            String subject = claims.getSubject();
            return subject != null && subject.equals(expectedSubject);
        } catch (Exception e) {
            return false;
        }
    }
    
    public boolean isRefreshToken(String token) {
        try {
            Claims claims = Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
            return "refresh".equals(claims.get("type", String.class));
        } catch (Exception e) {
            return false;
        }
    }
    
    public String getSubjectFromToken(String token) {
        return Jwts.parser()
            .verifyWith(getSigningKey())
            .build()
            .parseSignedClaims(token)
            .getPayload()
            .getSubject();
    }
    
    public String getUserIdFromToken(String token) {
        return Jwts.parser()
            .verifyWith(getSigningKey())
            .build()
            .parseSignedClaims(token)
            .getPayload()
            .get("userId", String.class);
    }
    
    public int getExpiresInSeconds() {
        return (int) (expirationMs / 1000);
    }
}
```

---

## 7. Auth Service Implementation

```java
@Service
@RequiredArgsConstructor
public class AuthService {
    
    private final UserRepository userRepository;
    private final OtpService otpService;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;
    private final CurrentUserAccessor currentUserAccessor;
    private final MessageSource messageSource;
    
    public AuthResponse register(RegisterRequest request, Locale locale) {
        // Check existing user
        if (userRepository.existsByPhone(request.phone())) {
            throw new BusinessException(messageSource.getMessage("auth.phone.registered", null, locale));
        }
        
        if (request.role() == UserRole.ADMIN) {
            throw new BusinessException(messageSource.getMessage("auth.admin.denied", null, locale));
        }
        
        // Verify OTP
        otpService.verifyOtp(request.phone(), request.otpCode(), OtpPurpose.REGISTRATION, locale);
        
        // Create user
        User user = new User();
        user.setName(request.name().trim());
        user.setPhone(request.phone());
        user.setPasswordHash(passwordEncoder.encode(request.password()));
        user.setRole(request.role());
        user.setStatus(UserStatus.ACTIVE);
        user.setVerificationLevel((byte) 0);
        
        userRepository.save(user);
        
        // Consume OTP and return response
        otpService.consumeOtp(request.phone(), OtpPurpose.REGISTRATION);
        
        return generateAuthResponse(user);
    }
    
    public AuthResponse loginWithPassword(String phone, String password, Locale locale) {
        User user = userRepository.findByPhone(phone)
            .orElseThrow(() -> new BusinessException(messageSource.getMessage("auth.invalid.credentials", null, locale)));
        
        ensureActive(user, locale);
        
        if (!passwordEncoder.matches(password, user.getPasswordHash())) {
            throw new BusinessException(messageSource.getMessage("auth.invalid.credentials", null, locale));
        }
        
        user.setLastLoginAt(OffsetDateTime.now());
        userRepository.save(user);
        
        return generateAuthResponse(user);
    }
    
    public AuthResponse loginWithOtp(String phone, String otpCode, Locale locale) {
        User user = userRepository.findByPhone(phone)
            .orElseThrow(() -> new BusinessException(messageSource.getMessage("auth.user.not.found", null, locale)));
        
        ensureActive(user, locale);
        
        otpService.verifyOtp(phone, otpCode, OtpPurpose.LOGIN, locale);
        otpService.consumeOtp(phone, OtpPurpose.LOGIN);
        
        user.setLastLoginAt(OffsetDateTime.now());
        userRepository.save(user);
        
        return generateAuthResponse(user);
    }
    
    public AuthResponse refresh(String refreshToken, Locale locale) {
        if (!jwtService.isRefreshToken(refreshToken)) {
            throw new BusinessException(messageSource.getMessage("auth.invalid.refresh", null, locale));
        }
        
        String subject = jwtService.getSubjectFromToken(refreshToken);
        User user = userRepository.findByPhoneOrEmail(subject)
            .orElseThrow(() -> new BusinessException(messageSource.getMessage("auth.user.not.found", null, locale)));
        
        ensureActive(user, locale);
        
        return generateAuthResponse(user);
    }
    
    public UserResponse getCurrentUser() {
        User user = currentUserAccessor.requireUser();
        return toUserResponse(user);
    }
    
    public void changePassword(String currentPassword, String newPassword, Locale locale) {
        User user = currentUserAccessor.requireUser();
        
        if (!passwordEncoder.matches(currentPassword, user.getPasswordHash())) {
            throw new BusinessException(messageSource.getMessage("auth.password.wrong", null, locale));
        }
        
        user.setPasswordHash(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }
    
    public void resetPassword(String phone, String otpCode, String newPassword, Locale locale) {
        User user = userRepository.findByPhone(phone)
            .orElseThrow(() -> new BusinessException(messageSource.getMessage("auth.user.not.found", null, locale)));
        
        otpService.verifyOtp(phone, otpCode, OtpPurpose.PASSWORD_RESET, locale);
        
        user.setPasswordHash(passwordEncoder.encode(newPassword));
        userRepository.save(user);
        
        otpService.consumeOtp(phone, OtpPurpose.PASSWORD_RESET);
    }
    
    private void ensureActive(User user, Locale locale) {
        if (user.getStatus() != UserStatus.ACTIVE) {
            throw new BusinessException(messageSource.getMessage("auth.account." + user.getStatus().name().toLowerCase(), null, locale));
        }
    }
    
    private AuthResponse generateAuthResponse(User user) {
        String accessToken = jwtService.generateToken(user, false);
        String refreshToken = jwtService.generateToken(user, true);
        
        return new AuthResponse(
            accessToken,
            refreshToken,
            "Bearer",
            jwtService.getExpiresInSeconds(),
            toUserResponse(user)
        );
    }
    
    private UserResponse toUserResponse(User user) {
        return new UserResponse(
            user.getId(),
            user.getName(),
            user.getPhone(),
            user.getEmail(),
            user.getPhotoUrl(),
            user.getRole(),
            user.getStatus(),
            user.getVerificationLevel(),
            user.getCreatedAt()
        );
    }
}
```

---

## 8. Security Configuration Updates

```java
@Configuration
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    
    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;
    
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, 
                                    FilterChain filterChain) throws ServletException, IOException {
        
        String authHeader = request.getHeader("Authorization");
        
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            
            try {
                String subject = jwtService.getSubjectFromToken(token);
                String userId = jwtService.getUserIdFromToken(token);
                
                if (subject != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                    UserDetails userDetails = userDetailsService.loadUserByUsername(subject);
                    
                    if (jwtService.isTokenValid(token, subject)) {
                        UsernamePasswordAuthenticationToken authToken = 
                            new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                        authToken.setDetails(new UserAuthenticationTokenDetails(userId));
                        SecurityContextHolder.getContext().setAuthentication(authToken);
                    }
                }
            } catch (Exception e) {
                // Token invalid - continue without authentication
            }
        }
        
        filterChain.doFilter(request, response);
    }
}
```

---

## 9. Message Properties

```properties
# messages_ar.properties
auth.phone.registered=رقم الهاتف مسجل بالفعل
auth.email.registered=البريد الإلكتروني مسجل بالفعل
auth.admin.denified=حساب المسؤول يتم إنشاؤه بواسطة المنصة
auth.invalid.credentials=بيانات الاعتماد غير صحيحة
auth.user.not.found=لا يوجد حساب بهذا الهاتف
auth.account.suspended=الحساب معلق
auth.password.wrong=كلمة المرور الحالية غير صحيحة
auth.invalid.refresh=رمز التحديث غير صالح أو منتهي الصلاحية
otp.too.many.requests=محاولات كثيرة جداً. يرجى المحاولة لاحقاً
otp.wait.cooldown=يرجى الانتظار قبل طلب رمز جديد
otp.invalid=رمز التحقق غير صحيح أو منتهي الصلاحية
otp.max.attempts=محاولات كثيرة جداً. يرجى طلب رمز جديد

# messages_en.properties
auth.phone.registered=Phone number already registered
auth.email.registered=Email already registered
auth.admin.denied=Admin accounts are provisioned by the platform
auth.invalid.credentials=Invalid credentials
auth.user.not.found=No account found for this phone
auth.account.suspended=Account is suspended
auth.password.wrong=Current password is incorrect
auth.invalid.refresh=Invalid or expired refresh token
otp.too.many.requests=Too many OTP requests. Please try again later
otp.wait.cooldown=Please wait a moment before requesting a new code
otp.invalid=Invalid or expired OTP code
otp.max.attempts=Too many invalid attempts. Request a new code
```

---

## 10. Repository Interfaces

```java
public interface UserRepository extends JpaRepository<User, UUID> {
    Optional<User> findByPhone(String phone);
    Optional<User> findByEmail(String email);
    Optional<User> findByPhoneOrEmail(String phoneOrEmail);
    boolean existsByPhone(String phone);
    boolean existsByEmail(String email);
}

public interface OtpCodeRepository extends JpaRepository<OtpCode, UUID> {
    Optional<OtpCode> findValidOtp(String identifier, OtpPurpose purpose, OffsetDateTime now);
    Optional<OtpCode> findTopByIdentifierAndPurposeOrderByCreatedAtDesc(String identifier, OtpPurpose purpose);
    long countByIdentifierAndCreatedAtAfter(String identifier, OffsetDateTime after);
    void deleteByIdentifierAndPurpose(String identifier, OtpPurpose purpose);
}
```

---

## 11. Tests (Stage 1)

### Unit Tests

```java
@ExtendWith(MockitoExtension.class)
class OtpServiceTest {
    
    @Test
    void sendOtp_shouldEnforceRateLimiting() {
        when(otpCodeRepository.countByIdentifierAndCreatedAtAfter(any(), any())).thenReturn(5);
        
        assertThatThrownBy(() -> otpService.sendOtp("01234567890", OtpPurpose.LOGIN, false))
            .isInstanceOf(BusinessException.class)
            .hasMessageContaining("too many");
    }
}

@ExtendWith(MockitoExtension.class)
class JwtServiceTest {
    
    @Test
    void generateToken_shouldIncludeCorrectClaims() {
        User user = new User();
        user.setId(UUID.randomUUID());
        user.setPhone("01234567890");
        user.setRole(UserRole.CUSTOMER);
        
        String token = jwtService.generateToken(user, false);
        
        assertThat(jwtService.isTokenValid(token, "01234567890")).isTrue();
    }
}
```

---

## 12. Definition of Done (Stage 1)

- [ ] All 17 endpoints live, with exact paths/methods/auth/status codes.
- [ ] User and OTP entities with Flyway migrations.
- [ ] OTP flow fully rate-limited, hashed, single-use.
- [ ] JWT access/refresh non-swappable.
- [ ] All error strings localized in Arabic and English.
- [ ] Unit + integration tests green.
- [ ] Login page placeholder wired to `/auth/login`.