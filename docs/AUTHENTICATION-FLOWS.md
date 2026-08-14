# 🔐 Delta Homes API — Authentication Flows

> **Base URL:** `{{baseUrl}}/api/v1/auth`
>
> All requests accept and return `application/json`.
>
> **OTP Channel Preference:** Email (SMTP) is the **primary** channel for new users. SMS is only available for **existing** (already-registered) phone numbers.

---

## Table of Contents

1. [Overview & Token Model](#1-overview--token-model)
2. [OTP Endpoints (Shared)](#2-otp-endpoints-shared)
3. [Scenario 1 — Registration with Email](#3-scenario-1--registration-with-email)
4. [Scenario 2 — Registration with Phone](#4-scenario-2--registration-with-phone)
5. [Scenario 3 — Login with Email + Password](#5-scenario-3--login-with-email--password)
6. [Scenario 4 — Login with Phone + Password](#6-scenario-4--login-with-phone--password)
7. [Scenario 5 — Passwordless Login with Email OTP](#7-scenario-5--passwordless-login-with-email-otp)
8. [Scenario 6 — Passwordless Login with Phone OTP](#8-scenario-6--passwordless-login-with-phone-otp)
9. [Scenario 7 — Refresh Access Token](#9-scenario-7--refresh-access-token)
10. [Scenario 8 — Get Current User Profile](#10-scenario-8--get-current-user-profile)
11. [Scenario 9 — Change Password (Logged In)](#11-scenario-9--change-password-logged-in)
12. [Scenario 10 — Reset Password with Email OTP](#12-scenario-10--reset-password-with-email-otp)
13. [Scenario 11 — Reset Password with Phone OTP](#13-scenario-11--reset-password-with-phone-otp)
14. [Scenario 12 — Logout](#14-scenario-12--logout)
15. [Error Handling](#15-error-handling)
16. [Validation Rules](#16-validation-rules)
17. [Quick Reference Table](#17-quick-reference-table)

---

## 1. Overview & Token Model

### Authentication Scheme

| Header | Value |
|--------|-------|
| `Authorization` | `Bearer <accessToken>` |

### AuthResponse (returned by all login/register endpoints)

```json
{
  "accessToken": "eyJhbGciOi...",
  "refreshToken": "eyJhbGciOi...",
  "tokenType": "Bearer",
  "expiresInSeconds": 3600,
  "user": {
    "id": "550e8400-e29b-41d4-a716-446655440000",
    "name": "Ahmed Hassan",
    "phone": "01012345678",
    "email": "ahmed@example.com",
    "photoUrl": null,
    "role": "CUSTOMER",
    "status": "ACTIVE",
    "verificationLevel": 1,
    "createdAt": "2026-01-15T10:30:00Z"
  }
}
```

### User Roles

| Role | Description |
|------|-------------|
| `CUSTOMER` | End user looking for properties |
| `OWNER` | Property owner listing properties |
| `OFFICE` | Real estate office |
| `COMPANY` | Finishing / maintenance company |
| `TECHNICIAN` | Service technician |
| `ADMIN` | Platform admin (provisioned by system, not self-registerable) |

### OTP Purposes

| Purpose | Used For |
|---------|----------|
| `REGISTRATION` | New account creation |
| `LOGIN` | Passwordless login |
| `PASSWORD_RESET` | Forgotten password recovery |

---

## 2. OTP Endpoints (Shared)

These are utility endpoints used **within** the registration, login, and password-reset flows. They can be called independently if your app wants a two-step UX (send code → user enters code → then call the final endpoint).

### 2.1 Send Email OTP

> **POST** `/api/v1/auth/otp/send-email`

**Request Body:**

```json
{
  "email": "user@example.com",
  "purpose": "REGISTRATION"
}
```

**Response (200):**

```json
{
  "phone": "user@example.com",
  "expiresInMinutes": 5,
  "message": "OTP sent to us***@example.com"
}
```

> 💡 In dev mode (no SMTP configured), the code is printed to the server console log.

---

### 2.2 Verify Email OTP

> **POST** `/api/v1/auth/otp/verify-email`

**Request Body:**

```json
{
  "email": "user@example.com",
  "code": "123456",
  "purpose": "REGISTRATION"
}
```

**Response (200):**

```json
{
  "phone": "user@example.com",
  "verified": true
}
```

---

### 2.3 Send Phone OTP (Existing Users Only)

> **POST** `/api/v1/auth/otp/send`

> ⚠️ **Only works for already-registered phone numbers.** Unknown phones are rejected with a message to use the email flow.

**Request Body:**

```json
{
  "phone": "01012345678",
  "purpose": "LOGIN"
}
```

**Response (200):**

```json
{
  "phone": "01012345678",
  "expiresInMinutes": 5,
  "message": "OTP sent to 010***678"
}
```

---

### 2.4 Verify Phone OTP

> **POST** `/api/v1/auth/otp/verify`

**Request Body:**

```json
{
  "phone": "01012345678",
  "code": "123456",
  "purpose": "LOGIN"
}
```

**Response (200):**

```json
{
  "phone": "01012345678",
  "verified": true
}
```

---

## 3. Scenario 1 — Registration with Email

> **Recommended flow for new users.**

```
┌──────────────┐     ┌──────────────────┐     ┌─────────────────┐
│  Mobile App  │────▶│  Send Email OTP  │────▶│  User enters    │
│              │     │  POST /otp/      │     │  code from email│
│              │     │  send-email      │     │                 │
└──────────────┘     └──────────────────┘     └────────┬────────┘
                                                       │
                                                       ▼
                                               ┌─────────────────┐
                                               │  Register with  │
                                               │  Email          │
                                               │  POST /register-│
                                               │  email          │
                                               └────────┬────────┘
                                                        │
                                                        ▼
                                               ┌─────────────────┐
                                               │  AuthResponse   │
                                               │  (tokens + user)│
                                               └─────────────────┘
```

### Step 1 — Send Email OTP

> **POST** `/api/v1/auth/otp/send-email`

```json
{
  "email": "newuser@example.com",
  "purpose": "REGISTRATION"
}
```

**Response (200):**

```json
{
  "phone": "newuser@example.com",
  "expiresInMinutes": 5,
  "message": "OTP sent to ne***@example.com"
}
```

### Step 2 — Register with Email

> **POST** `/api/v1/auth/register-email`

```json
{
  "name": "Ahmed Hassan",
  "email": "newuser@example.com",
  "password": "secret123",
  "role": "CUSTOMER",
  "otpCode": "123456"
}
```

**Response (201):**

```json
{
  "accessToken": "eyJhbGciOi...",
  "refreshToken": "eyJhbGciOi...",
  "tokenType": "Bearer",
  "expiresInSeconds": 3600,
  "user": {
    "id": "550e8400-e29b-41d4-a716-446655440000",
    "name": "Ahmed Hassan",
    "phone": null,
    "email": "newuser@example.com",
    "photoUrl": null,
    "role": "CUSTOMER",
    "status": "ACTIVE",
    "verificationLevel": 1,
    "createdAt": "2026-08-08T12:00:00Z"
  }
}
```

> ✅ `verificationLevel: 1` — email-verified users get level 1 automatically.

---

## 4. Scenario 2 — Registration with Phone

> ⚠️ Phone OTP is only sent to **existing** users. For new users, use the Email registration flow above.

### Step 1 — Send Phone OTP

> **POST** `/api/v1/auth/otp/send`

```json
{
  "phone": "01012345678",
  "purpose": "REGISTRATION"
}
```

> If the phone is **not** registered, the server returns:
> ```json
> { "message": "Phone OTP is only available for registered users. Please register using your email (SMTP verification)." }
> ```

### Step 2 — Register with Phone

> **POST** `/api/v1/auth/register`

```json
{
  "name": "Ahmed Hassan",
  "phone": "01012345678",
  "password": "secret123",
  "role": "CUSTOMER",
  "otpCode": "123456"
}
```

**Response (201):** Same `AuthResponse` structure as Scenario 1.

> ✅ `verificationLevel: 0` — phone-registered users start at level 0.

---

## 5. Scenario 3 — Login with Email + Password

> **POST** `/api/v1/auth/login/email`

```json
{
  "email": "user@example.com",
  "password": "secret123"
}
```

**Response (200):** `AuthResponse` with tokens + user object.

> 💡 Store `accessToken` and `refreshToken` securely on the device.

---

## 6. Scenario 4 — Login with Phone + Password

> **POST** `/api/v1/auth/login`

```json
{
  "phone": "01012345678",
  "password": "secret123"
}
```

**Response (200):** `AuthResponse` with tokens + user object.

---

## 7. Scenario 5 — Passwordless Login with Email OTP

```
┌──────────────┐     ┌──────────────────┐     ┌─────────────────┐
│  Mobile App  │────▶│  Send Email OTP  │────▶│  User enters    │
│              │     │  POST /otp/      │     │  code from email│
│              │     │  send-email      │     │                 │
└──────────────┘     └──────────────────┘     └────────┬────────┘
                                                       │
                                                       ▼
                                               ┌─────────────────┐
                                               │  Login with     │
                                               │  Email OTP      │
                                               │  POST /login/   │
                                               │  otp/email      │
                                               └────────┬────────┘
                                                        │
                                                        ▼
                                               ┌─────────────────┐
                                               │  AuthResponse   │
                                               │  (tokens + user)│
                                               └─────────────────┘
```

### Step 1 — Send Email OTP

> **POST** `/api/v1/auth/otp/send-email`

```json
{
  "email": "user@example.com",
  "purpose": "LOGIN"
}
```

### Step 2 — Login with Email OTP

> **POST** `/api/v1/auth/login/otp/email`

```json
{
  "email": "user@example.com",
  "otpCode": "123456"
}
```

**Response (200):** `AuthResponse` with tokens + user object.

---

## 8. Scenario 6 — Passwordless Login with Phone OTP

> ⚠️ SMS OTP is only sent to **existing** (registered) phone numbers.

### Step 1 — Send Phone OTP

> **POST** `/api/v1/auth/otp/send`

```json
{
  "phone": "01012345678",
  "purpose": "LOGIN"
}
```

### Step 2 — Login with Phone OTP

> **POST** `/api/v1/auth/login/otp`

```json
{
  "phone": "01012345678",
  "otpCode": "123456"
}
```

**Response (200):** `AuthResponse` with tokens + user object.

---

## 9. Scenario 7 — Refresh Access Token

> When the `accessToken` expires, use the `refreshToken` to get a new pair **without** requiring the user to log in again.

> **POST** `/api/v1/auth/refresh`

```json
{
  "refreshToken": "eyJhbGciOi..."
}
```

**Response (200):**

```json
{
  "accessToken": "eyJhbGciOi...(new)",
  "refreshToken": "eyJhbGciOi...(new)",
  "tokenType": "Bearer",
  "expiresInSeconds": 3600,
  "user": { ... }
}
```

> 💡 **Best practice:** Implement a token refresh interceptor in your HTTP client. When you receive a `401`, attempt a refresh before prompting re-login.

---

## 10. Scenario 8 — Get Current User Profile

> **GET** `/api/v1/auth/me`
>
> 🔒 **Requires** `Authorization: Bearer <accessToken>` header.

**Response (200):**

```json
{
  "id": "550e8400-e29b-41d4-a716-446655440000",
  "name": "Ahmed Hassan",
  "phone": "01012345678",
  "email": "ahmed@example.com",
  "photoUrl": "https://example.com/photos/ahmed.jpg",
  "role": "CUSTOMER",
  "status": "ACTIVE",
  "verificationLevel": 1,
  "createdAt": "2026-01-15T10:30:00Z"
}
```

---

## 11. Scenario 9 — Change Password (Logged In)

> User **knows** their current password and wants to change it.

> **PUT** `/api/v1/auth/password`
>
> 🔒 **Requires** `Authorization: Bearer <accessToken>` header.

```json
{
  "currentPassword": "oldSecret123",
  "newPassword": "newSecret456"
}
```

**Response (200):**

```json
{
  "message": "Password changed successfully"
}
```

---

## 12. Scenario 10 — Reset Password with Email OTP

> User **forgot** their password and needs to reset it via email.

```
┌──────────────┐     ┌──────────────────┐     ┌─────────────────┐
│  Mobile App  │────▶│  Send Email OTP  │────▶│  User enters    │
│              │     │  POST /otp/      │     │  code from email│
│              │     │  send-email      │     │                 │
└──────────────┘     └──────────────────┘     └────────┬────────┘
                                                       │
                                                       ▼
                                               ┌─────────────────┐
                                               │  Reset Password │
                                               │  POST /password/│
                                               │  reset/email    │
                                               └────────┬────────┘
                                                        │
                                                        ▼
                                               ┌─────────────────┐
                                               │  Success message│
                                               └─────────────────┘
```

### Step 1 — Send Email OTP

> **POST** `/api/v1/auth/otp/send-email`

```json
{
  "email": "user@example.com",
  "purpose": "PASSWORD_RESET"
}
```

### Step 2 — Reset Password

> **POST** `/api/v1/auth/password/reset/email`

```json
{
  "email": "user@example.com",
  "otpCode": "123456",
  "newPassword": "brandnew123"
}
```

**Response (200):**

```json
{
  "message": "Password reset successfully"
}
```

---

## 13. Scenario 11 — Reset Password with Phone OTP

> ⚠️ SMS OTP is only sent to **existing** (registered) phone numbers.

### Step 1 — Send Phone OTP

> **POST** `/api/v1/auth/otp/send`

```json
{
  "phone": "01012345678",
  "purpose": "PASSWORD_RESET"
}
```

### Step 2 — Reset Password

> **POST** `/api/v1/auth/password/reset`

```json
{
  "phone": "01012345678",
  "otpCode": "123456",
  "newPassword": "brandnew123"
}
```

**Response (200):**

```json
{
  "message": "Password reset successfully"
}
```

---

## 14. Scenario 12 — Logout

> **POST** `/api/v1/auth/logout`
>
> 🔒 **Requires** `Authorization: Bearer <accessToken>` header.

**Response (200):**

```json
{
  "message": "Logged out"
}
```

> ⚠️ **Important:** JWT is stateless. The server does **not** invalidate tokens. The mobile app must:
> 1. Delete `accessToken` and `refreshToken` from secure storage.
> 2. Clear any cached user data.
> 3. Navigate to the login/landing screen.

---

## 15. Error Handling

All errors follow this structure:

```json
{
  "timestamp": "2026-08-08T12:00:00Z",
  "status": 400,
  "error": "Bad Request",
  "message": "Invalid OTP code.",
  "path": "/api/v1/auth/otp/verify-email"
}
```

### Common Error Codes

| HTTP Status | Message | When |
|-------------|---------|------|
| `400` | `Invalid credentials` | Wrong email/phone or password |
| `400` | `No account found for this email` | Email not registered (login/OTP) |
| `400` | `No account found for this phone` | Phone not registered |
| `400` | `Email already registered` | Duplicate email on registration |
| `400` | `Phone number already registered` | Duplicate phone on registration |
| `400` | `Phone OTP is only available for registered users...` | OTP send for unknown phone |
| `400` | `No active OTP code found. Request a new one.` | Verify before sending |
| `400` | `OTP code has expired. Request a new one.` | Code expired (5 min window) |
| `400` | `Too many invalid attempts. Request a new code.` | >5 wrong codes entered |
| `400` | `Too many OTP requests. Please try again later.` | Rate limit hit (5 per 15 min) |
| `400` | `Please wait a moment before requesting a new code.` | Resend within 60s cooldown |
| `400` | `Invalid or expired refresh token` | Bad/expired refresh token |
| `400` | `Current password is incorrect` | Wrong current password |
| `400` | `Admin accounts are provisioned by the platform` | Attempting admin registration |
| `401` | `Unauthorized` | Missing or invalid Bearer token |
| `403` | `Forbidden` | Authenticated but insufficient role |

---

## 16. Validation Rules

| Field | Rules |
|-------|-------|
| `phone` | Required. Egyptian format: `^01[0-9]{9}$` (e.g., `01012345678`) |
| `email` | Required. Valid email, max 150 chars, normalized to lowercase |
| `password` | Required. Min 6, max 72 characters |
| `otpCode` | Required. Exactly 6 digits (`^[0-9]{6}$`) |
| `name` | Required. Max 120 characters |
| `role` | Required. One of: `CUSTOMER`, `OWNER`, `OFFICE`, `COMPANY`, `TECHNICIAN` |
| `purpose` | Required. One of: `REGISTRATION`, `LOGIN`, `PASSWORD_RESET` |

---

## 17. Quick Reference Table

| Scenario | Step 1 | Step 2 | Auth Required |
|----------|--------|--------|---------------|
| **Register (Email)** | `POST /otp/send-email` | `POST /register-email` | ❌ |
| **Register (Phone)** | `POST /otp/send` | `POST /register` | ❌ |
| **Login (Email+Password)** | — | `POST /login/email` | ❌ |
| **Login (Phone+Password)** | — | `POST /login` | ❌ |
| **Login (Email OTP)** | `POST /otp/send-email` | `POST /login/otp/email` | ❌ |
| **Login (Phone OTP)** | `POST /otp/send` | `POST /login/otp` | ❌ |
| **Refresh Token** | — | `POST /refresh` | ❌ |
| **Get Profile** | — | `GET /me` | ✅ |
| **Change Password** | — | `PUT /password` | ✅ |
| **Reset Password (Email)** | `POST /otp/send-email` | `POST /password/reset/email` | ❌ |
| **Reset Password (Phone)** | `POST /otp/send` | `POST /password/reset` | ❌ |
| **Logout** | — | `POST /logout` | ✅ |

---

## Appendix: Mobile App Implementation Tips

### Recommended HTTP Client Setup

```kotlin
// Example: OkHttp interceptor (Kotlin/Android)
class AuthInterceptor(private val tokenProvider: () -> String?) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request().newBuilder().apply {
            tokenProvider()?.let { addHeader("Authorization", "Bearer $it") }
        }.build()
        return chain.proceed(request)
    }
}
```

### Token Storage

- Store `accessToken` and `refreshToken` in **encrypted** storage (Keychain on iOS, EncryptedSharedPreferences on Android).
- Never store tokens in plain text or SharedPreferences.

### Refresh Strategy

1. Make API call with `accessToken`.
2. If `401` is received, call `POST /refresh` with `refreshToken`.
3. If refresh succeeds, retry the original request with the new `accessToken`.
4. If refresh fails (invalid/expired `refreshToken`), navigate to login screen.

### OTP Auto-Fill (Android)

Use `SMS Retriever API` for auto-filling SMS OTPs, or implement email OTP auto-fill via `SMS_USER_CONSENT_API`.

### Rate Limiting

- **Max 5 OTP sends** per recipient per 15-minute window.
- **60-second cooldown** between resend requests.
- **Max 5 invalid attempts** per OTP code before it's invalidated.
- **5-minute expiry** on all OTP codes.
