# Delta Homes — Master Build Specification (Modular, Incremental)

> **This is the single source of truth for building the Delta Homes platform.**
> An AI agent (or human team) builds the system **one module per stage**, in the order
> defined below. Every stage is a self-contained spec file with its own API contract,
> data model, business rules, tests, and Definition of Done.

---

## 1. What is being built

Delta Homes is a multi-sided Egyptian **home-lifecycle marketplace**: users discover
properties, hire companies (real-estate offices, finishing companies, maintenance
providers), book viewings, chat, follow companies, save listings, write reviews, and
receive broadcasts/notifications. Companies pay for plans that grant listing and
broadcast quotas. An **admin console** handles users, reports, verifications, fraud,
coupons, payments, subscriptions, and audit logs.

The backend is a **Spring Boot** application targeting parity with the existing Java API surface and data model, plus aspirational features (documented as such).

### Architecture (kept deliberately simple)

- **One Spring Boot Web Application** (`com.deltahomes`) — thin controllers that delegate
  to service classes, which use Spring Data JPA repositories directly. **No** separate Domain / Application /
  Infrastructure modules, **no** CQRS, **no** messaging bus, **no** background job system
  in v1.
- **One admin dashboard** (React or Vue.js) that consumes the API's admin endpoints over HTTP 
  with the admin JWT. It is a pure client — no business logic lives in it.
- **Microsoft SQL Server** as the single system of record.

```
deltahomes-backend/
├── src/main/java/com/deltahomes/
│   ├── controller/      # REST Controllers (thin → services → repositories)
│   ├── service/         # Business logic services
│   ├── repository/      # Spring Data JPA repositories
│   ├── entity/          # JPA entities
│   ├── dto/             # Data transfer objects
│   ├── security/        # JWT, Security config
│   ├── config/          # Application configuration
│   └── exception/       # Exception handling
├── src/main/resources/
│   ├── db/migration/    # Flyway/Liquibase migrations
│   └── application.yml  # Configuration
└── src/test/            # Tests (JUnit 5 + Mockito)
```

> **Rule:** if a stage ever feels like it needs a new module, a message broker, a
> background queue, or a new infrastructure service, **stop and reconsider** — the
> simplicity constraint is deliberate. Extensions are listed as "Future / Aspirational"
> stages only.

---

## 2. Source-of-truth hierarchy (read before anything else)

| Priority | Document | Role |
|---|---|---|
| 1 | `00-master.md` | Spring Boot target stack, module structure, cross-cutting conventions |
| 2 | Data Model documentation | Exact REST API contract + full relational data model |
| 3 | Business Logic documentation | Exact service-layer behavior, rules, error messages |
| 4 | Product Spec | Product intent / aspirational features (Google/Apple login, PostHog, payments, Projects, Property Timeline) — used **only** to spec the Aspirational stages below |
| 5 | This `Modules/` folder | The buildable, incremental breakdown of items 1–4 |

**Conflict rule:** where the Product Spec (item 4) disagrees with items 1–3 (e.g.
`BIGINT` vs UUID PKs), items 1–3 win for all **Parity** stages. Aspirational stages
(item 5, marked `ASPIRATIONAL`) are the only places the product spec can extend the
contract, and each such stage says so explicitly.

---

## 3. File index & reading order

| File | Stage | Module | Type |
|---|---|---|---|
| `00-master.md` | — | Global contract, NFRs, build order | Contract |
| `01-foundation.md` | 0 | Project skeleton + cross-cutting infrastructure | Parity |
| `02-auth.md` | 1 | **Auth & Authorization** (first module to build) | Parity |
| `03-lookups.md` | 2 | Lookups (cities, districts, services, features, plans) | Parity |
| `04-properties.md` | 3 | Properties | Parity |
| `05-companies.md` | 4 | Companies & Follow | Parity |
| `06-saved-items.md` | 5 | Saved Items | Parity |
| `07-reviews.md` | 6 | Reviews | Parity |
| `08-appointments.md` | 7 | Appointments | Parity |
| `09-chat.md` | 8 | Chat | Parity |
| `10-broadcasts.md` | 9 | Broadcasts | Parity |
| `11-search.md` | 10 | Search | Parity (stub) |
| `12-notifications.md` | 11 | Notifications | Parity |
| `13-admin.md` | 12 | Admin API + Dashboard | Parity |
| `14-commerce.md` | 13 | Commerce (plans, subscriptions, payments, coupons) | Parity + aspirational |
| `15-social-analytics.md` | 14 | Google/Apple login + PostHog analytics | Aspirational |
| `16-projects-timeline.md` | 15 | Projects + Property Timeline | Aspirational |
| `17-background-jobs.md` | 16 | Background job queue | Aspirational |

---

## 4. Stage dependency graph

```
 0 Foundation ─────────────────────────────────────────────┐
 │                                                         │
 ├─ 1 Auth & Identity ──┐                                  │
 │                      ├─ 5 Saved Items      ──┐          │
 ├─ 2 Lookups ──────┐   ├─ 6 Reviews          ├─ 12 Admin ─┴─ 13 Commerce ──┐
 ├─ 3 Properties ───┼───┼─ 7 Appointments     │                               ├─ 14 Social/Analytics
 │  (needs 2)       ├─ 4 Companies/Follow ────┼─ 8 Chat                       ├─ 15 Projects/Timeline
 │                  │                          ├─ 9 Broadcasts                 └─ 16 Background Jobs
 │                  │                          ├─ 10 Search
 │                  │                          └─ 11 Notifications
 └──────────────────┘
```

**Build rule:** implement strictly in stage order. Each stage file lists its
dependencies; do not build a stage whose dependencies are not complete and green.

---

## 5. How an AI agent should use these files (incremental protocol)

1. **Read `00-master.md` once, fully.** It defines the wire contract, error shapes,
   security model, config schema, non-functional requirements, and naming rules. Every
   later stage file references these; do not re-derive them.
2. **For each stage, read ONLY the stage's own spec file** plus `00-master.md`. Stage
   files are self-contained: they restate their endpoints, DTOs, entities, rules,
   tests, and Definition of Done so you rarely need to open the three source docs again.
3. **Implement the stage.** Follow the "Entry point files" and "Implementation steps"
   sections exactly. Keep the file/folder layout from `01-foundation.md`.
4. **Run the stage's tests** (unit + integration per `00-master.md §Testing`). A stage
   is **done** only when its Definition of Done checklist is fully ticked.
5. **Do not gold-plate.** If a stage says "stub", build the stub — not a full
   implementation. If a rule is not in the stage spec, do not add it.
6. When all stages are done, run the full test suite and the final acceptance sweep.

---

## 6. Global guardrails (violating any of these fails a stage)

1. **Do not invent endpoints, columns, enums, or rules** not present in the stage specs.
   Parity stages mirror the existing Java implementation exactly (UUID PKs, phone+email OTP only,
   no payments flow, no social login, no analytics).
2. **Keep raw SQL search queries** (`CONTAINS` over SQL Server FULLTEXT indexes)
   — do not translate to JPQL; output semantics will drift.
3. **Wire contract is fixed:** camelCase JSON, ISO-8601 datetimes, nulls omitted,
   0-based pagination, exact error shapes (§ `00-master.md §5–6`).
4. **Never return password hashes, OTP codes, or secrets** in any DTO.
5. **Never commit secrets**; all secrets come from environment variables/config.
6. **Enum columns are TEXT** (`@Enumerated(EnumType.STRING)`); JSON columns are `nvarchar(max)`
   stored as Java `String`.
7. **Sort columns are whitelisted** in `SortNormalizer` (never interpolate raw user input
   into SQL — a deliberate improvement over unsafe sorting).
