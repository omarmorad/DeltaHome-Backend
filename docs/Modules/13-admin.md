# Delta Homes — 13 · Stage 12 · Admin API + Dashboard

> **Stage 12.** Admin endpoints and Arabic RTL dashboard.

**Status:** Parity · **Dependencies:** Stage 0, 1, 2..11 · **Effort:** L

---

## 1. Endpoints (all require ADMIN role)

| Method & Path | Response |
|---|---|
| `GET /api/v1/admin/users` | `Paginated<UserSummary>` |
| `PUT /api/v1/admin/users/{id}/status` | `200` |
| `GET /api/v1/admin/verifications` | `Paginated<VerificationSummary>` |
| `PUT /api/v1/admin/verifications/{id}` | `200` |
| `GET /api/v1/admin/reports` | `Paginated<ReportSummary>` |
| `PUT /api/v1/admin/reports/{id}` | `200` |
| `GET /api/v1/admin/audit-logs` | `Paginated<AuditLogSummary>` |

---

## 2. Security

```java
@RestController
@RequestMapping("/api/v1/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {
    // Admin endpoints
}
```

---

## 3. Admin Dashboard (Frontend)

- React or Vue.js with Arabic RTL UI
- Components: Users, Verifications, Reports, Audit Logs, Settings

---

## 4. Definition of Done

- [ ] All admin endpoints secured with ADMIN role
- [ ] Dashboard with RTL Arabic interface
- [ ] Audit logging for admin actions