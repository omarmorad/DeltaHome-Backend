# 🧪 Delta Homes — Test Credentials (Development Environment)

> ⚠️ **WARNING:** These credentials are for **development/testing only**. Never use them in production.
>
> All passwords are stored as BCrypt hashes. The plain-text passwords below are the **only** ones that work with the seeded users.
>
> **OTP Dev Mode:** When no SMTP/Twilio is configured, OTP codes are printed to the **server console log**. Look for `[DEV MODE]` entries.

---

## 🔑 Quick Reference — All Credentials

| # | Name (EN) | Name (AR) | Phone | Email | Password | Role |
|---|-----------|-----------|-------|-------|----------|------|
| 1 | Delta Admin | — | `01026962089` | `admin@deltahomes.app` | `admin123` | ADMIN |
| 2 | Ahmed Hassan | أحمد حسن | `01011111111` | `ahmed.hassan@gmail.com` | `secret123` | CUSTOMER |
| 3 | Sara Ali | سارة علي | `01022222222` | `sara.ali@gmail.com` | `secret123` | CUSTOMER |
| 4 | Mahmoud Abdallah | محمود عبد الله | `01033333333` | `mahmoud.abdallah@gmail.com` | `secret123` | OWNER |
| 5 | Hisham Ibrahim | هشام إبراهيم | `01044444444` | `hisham.ibrahim@yahoo.com` | `secret123` | OWNER |
| 6 | Karim Mostafa | كريم مصطفى | `01055555555` | `karim.mostafa@gmail.com` | `secret123` | OFFICE |
| 7 | Mona Elsayed | منى السيد | `01066666666` | `mona.elsayed@outlook.com` | `secret123` | OFFICE |
| 8 | Khaled Ragab | خالد رجب | `01077777777` | `khaled.ragab@gmail.com` | `secret123` | COMPANY |
| 9 | Nourhan Adel | نورهان عادل | `01088888888` | `nourhan.adel@gmail.com` | `secret123` | COMPANY |
| 10 | Amr Said | عمرو سعيد | `01099999999` | `amr.said@yahoo.com` | `secret123` | TECHNICIAN |
| 11 | Ehab Fathy | إيهاب فتحي | `01556789012` | `ehab.fathy@gmail.com` | `secret123` | TECHNICIAN |

---

## 👑 Admin Account

The admin account is **auto-created on startup** by `AdminAccountInitializer` if it doesn't already exist.

| Field | Value |
|-------|-------|
| **Phone** | `01026962089` |
| **Email** | `admin@deltahomes.app` |
| **Password** | `admin123` |
| **Permanent OTP** | `112231` |
| **Role** | `ADMIN` |
| **Status** | `ACTIVE` |

### Admin Login Examples

**Phone + Password:**
```json
POST /api/v1/auth/login
{
  "phone": "01026962089",
  "password": "admin123"
}
```

**Email + Password:**
```json
POST /api/v1/auth/login/email
{
  "email": "admin@deltahomes.app",
  "password": "admin123"
}
```

**Admin OTP (bypass):**
> The permanent OTP `112231` works for **any purpose** and **never expires** for the admin phone/email.

```json
POST /api/v1/auth/otp/verify
{
  "phone": "01026962089",
  "code": "112231",
  "purpose": "LOGIN"
}
```

```json
POST /api/v1/auth/login/otp
{
  "phone": "01026962089",
  "otpCode": "112231"
}
```

---

## 👤 Demo Users (Seeded by DataSeeder)

All demo users are created with:
- **Password:** `secret123`
- **Status:** `ACTIVE`
- **Verification Level:** `1` (email-verified)

### Customers

| Name | Phone | Email | Role |
|------|-------|-------|------|
| أحمد حسن (Ahmed Hassan) | `01011111111` | `ahmed.hassan@gmail.com` | CUSTOMER |
| سارة علي (Sara Ali) | `01022222222` | `sara.ali@gmail.com` | CUSTOMER |

**Login Example (Ahmed Hassan):**
```json
POST /api/v1/auth/login/email
{
  "email": "ahmed.hassan@gmail.com",
  "password": "secret123"
}
```

```json
POST /api/v1/auth/login
{
  "phone": "01011111111",
  "password": "secret123"
}
```

---

### Property Owners

| Name | Phone | Email | Role |
|------|-------|-------|------|
| محمود عبد الله (Mahmoud Abdallah) | `01033333333` | `mahmoud.abdallah@gmail.com` | OWNER |
| هشام إبراهيم (Hisham Ibrahim) | `01044444444` | `hisham.ibrahim@yahoo.com` | OWNER |

**Login Example (Mahmoud):**
```json
POST /api/v1/auth/login/email
{
  "email": "mahmoud.abdallah@gmail.com",
  "password": "secret123"
}
```

---

### Real Estate Offices

| Name | Phone | Email | Role |
|------|-------|-------|------|
| كريم مصطفى (Karim Mostafa) | `01055555555` | `karim.mostafa@gmail.com` | OFFICE |
| منى السيد (Mona Elsayed) | `01066666666` | `mona.elsayed@outlook.com` | OFFICE |

**Login Example (Karim):**
```json
POST /api/v1/auth/login/email
{
  "email": "karim.mostafa@gmail.com",
  "password": "secret123"
}
```

---

### Companies

| Name | Phone | Email | Role |
|------|-------|-------|------|
| خالد رجب (Khaled Ragab) | `01077777777` | `khaled.ragab@gmail.com` | COMPANY |
| نورهان عادل (Nourhan Adel) | `01088888888` | `nourhan.adel@gmail.com` | COMPANY |

**Login Example (Khaled):**
```json
POST /api/v1/auth/login/email
{
  "email": "khaled.ragab@gmail.com",
  "password": "secret123"
}
```

---

### Technicians

| Name | Phone | Email | Role |
|------|-------|-------|------|
| عمرو سعيد (Amr Said) | `01099999999` | `amr.said@yahoo.com` | TECHNICIAN |
| إيهاب فتحي (Ehab Fathy) | `01556789012` | `ehab.fathy@gmail.com` | TECHNICIAN |

**Login Example (Amr):**
```json
POST /api/v1/auth/login/email
{
  "email": "amr.said@yahoo.com",
  "password": "secret123"
}
```

---

## 🏢 Company Ownership Map

Each company is owned by a specific user. This affects who can manage the company profile and broadcasts.

| Company | Company Type | Owner User | Owner Email |
|---------|-------------|------------|-------------|
| Delta Finishing Co. | FINISHING_COMPANY | Admin | `admin@deltahomes.app` |
| El-Delta Finishing Group | FINISHING_COMPANY | Khaled Ragab | `khaled.ragab@gmail.com` |
| Damietta Star Finishing | FINISHING_COMPANY | Nourhan Adel | `nourhan.adel@gmail.com` |
| Nile Maintenance & Services | MAINTENANCE_PROVIDER | Nourhan Adel | `nourhan.adel@gmail.com` |
| Delta Prime Real Estate | REAL_ESTATE_OFFICE | Karim Mostafa | `karim.mostafa@gmail.com` |

---

## 📊 Data Context — What Each User Has

### Ahmed Hassan (CUSTOMER)
- **Saved Items:** 2 properties
- **Conversations:** 3 (with Mahmoud, Khaled, Amr)
- **Appointments:** 3 (accepted, completed)
- **Reviews:** 4 reviews
- **Following:** Delta Finishing Co., El-Delta, Nile Maintenance
- **Home City:** New Damietta

### Sara Ali (CUSTOMER)
- **Saved Items:** 2 properties
- **Conversations:** 3 (with Hisham, Nourhan, Karim)
- **Appointments:** 4 (accepted, rejected, pending)
- **Reviews:** 4 reviews
- **Following:** Delta Finishing Co., El-Delta, Nile Maintenance
- **Home City:** Mansoura

### Mahmoud Abdallah (OWNER)
- **Properties:** 4 (Sea View Apartment, Family Villa, Ground Floor with Garden, Furnished Apartment)
- **Conversations:** 1 (with Ehab)
- **Appointments:** 3 (as owner)
- **Reviews:** 1 review
- **Following:** El-Delta
- **Home City:** Damietta

### Hisham Ibrahim (OWNER)
- **Properties:** 3 (Modern Duplex, Studio, Beach Villa)
- **Conversations:** 1 (with Sara)
- **Appointments:** 2 (as owner)
- **Home City:** Ras El Bar

---

## 🧪 Login Flow Examples

### Example 1: Email + Password Login

```bash
# Login as Ahmed Hassan
curl -X POST http://localhost:8080/api/v1/auth/login/email \
  -H "Content-Type: application/json" \
  -d '{
    "email": "ahmed.hassan@gmail.com",
    "password": "secret123"
  }'
```

### Example 2: Phone + Password Login

```bash
# Login as Ahmed Hassan
curl -X POST http://localhost:8080/api/v1/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "phone": "01011111111",
    "password": "secret123"
  }'
```

### Example 3: Passwordless Login with OTP (Dev Mode)

```bash
# Step 1: Send OTP (check server console for code)
curl -X POST http://localhost:8080/api/v1/auth/otp/send-email \
  -H "Content-Type: application/json" \
  -d '{
    "email": "ahmed.hassan@gmail.com",
    "purpose": "LOGIN"
  }'

# Step 2: Login with OTP (replace 123456 with actual code from console)
curl -X POST http://localhost:8080/api/v1/auth/login/otp/email \
  -H "Content-Type: application/json" \
  -d '{
    "email": "ahmed.hassan@gmail.com",
    "otpCode": "123456"
  }'
```

### Example 4: Admin OTP Bypass

```bash
# Login as admin using permanent OTP
curl -X POST http://localhost:8080/api/v1/auth/login/otp \
  -H "Content-Type: application/json" \
  -d '{
    "phone": "01026962089",
    "otpCode": "112231"
  }'
```

---

## 📝 Environment Variables (Defaults)

These are the default values used when no environment variables are set. Found in `application.yml`:

| Variable | Default Value | Description |
|----------|---------------|-------------|
| `ADMIN_PHONE` | `01026962089` | Admin account phone |
| `ADMIN_EMAIL` | `admin@deltahomes.app` | Admin account email |
| `ADMIN_PASSWORD` | `admin123` | Admin account password |
| `ADMIN_PERMANENT_OTP` | `112231` | Admin bypass OTP code |
| `DB_PASSWORD` | `00` | PostgreSQL password |
| `MAIL_PASSWORD` | `pkcr efsb jifw vtig` | SMTP (Gmail app password) |
| `JWT_SECRET` | `dHlwZS5hY2Nlc3MtdG9rZW4u...` | JWT signing key |

---

## ⚠️ Security Notes

1. **All demo users have the same password (`secret123`)** — this is intentional for easy testing.
2. **The admin permanent OTP (`112231`)** works for any OTP purpose and never expires — for dev convenience only.
3. **In production**, ensure:
   - `ADMIN_PHONE`, `ADMIN_EMAIL`, `ADMIN_PASSWORD`, and `ADMIN_PERMANENT_OTP` are set via environment variables.
   - `SEED_ENABLED=false` to prevent demo data from being created.
   - Strong, unique passwords for all admin accounts.
4. **OTP codes** are stored as SHA-256 hashes, never in plain text.
5. **Passwords** are stored as BCrypt hashes, never in plain text.
