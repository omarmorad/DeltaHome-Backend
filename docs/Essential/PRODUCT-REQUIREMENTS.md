# Delta Homes — Product Requirements Document (PRD)

> **ماذا نبني؟ ولماذا؟ الأهداف، الجمهور، المميزات، مؤشرات النجاح.**  
> **What are we building and why? Goals, Audience, Bilingual Mobile App, Arabic Admin Dashboard, Feature Scope, and KPIs.**

---

## 1. Executive Summary & Vision (الرؤية والهدف)

The Egyptian real estate, interior design, finishing, and home maintenance markets are highly fragmented. Home seekers face non-transparent pricing, unverified listings, and unreliable service providers, while real estate brokerages and finishing firms struggle to reach qualified customers efficiently.

**Delta Homes** addresses this gap by serving as a unified **Egyptian Home-Lifecycle Marketplace**. It accompanies users throughout their entire home ownership or rental journey—from property discovery and booking viewings to hiring finishing firms and ordering ongoing home maintenance.

To cater to Egypt's diverse market, the platform delivers a **bilingual experience (Arabic & English)** for mobile applications, while offering a dedicated **Arabic-first Admin Dashboard (RTL)** for platform operation teams.

---

## 2. Target Audience & User Personas (الجمهور المستهدف وشخصيات المستخدمين)

### 2.1 Property Seekers (Buyers & Renters)
- **Need**: Search verified residential and commercial listings, filter by location/price/amenities, schedule viewings, chat directly with brokers, save favorite properties, and read authentic reviews.
- **Language Preference**: **Bilingual (Arabic & English)** — Users can seamlessly switch between Arabic and English interfaces on the mobile application.
- **Key Actions**: Register via phone/email OTP, search listings in Arabic or English, save favorites, book viewings, leave reviews.

### 2.2 Property Owners & Individual Brokers
- **Need**: Post property listings with bilingual titles and descriptions, manage viewing requests, communicate with prospective buyers/renters.
- **Language Preference**: **Bilingual (Arabic & English)** mobile interface.
- **Key Actions**: Create property listings (providing Arabic and/or English details), manage appointment schedules, respond to direct chats.

### 2.3 Specialized Companies (Real Estate Agencies, Finishing, Maintenance)
- **Need**: Establish verified business presence, list property portfolios, showcase services, receive direct leads, broadcast targeted promotions.
- **Language Preference**: **Bilingual (Arabic & English)** profile management.
- **Key Actions**: Purchase subscription plans, manage company team members, publish broadcasts in Arabic and English.

### 2.4 System Administrators & Moderators
- **Need**: Oversee user and company verifications, audit listings for fraud, moderate reviews, manage subscription plans and coupons, monitor platform metrics.
- **Language Preference**: **Arabic-First Interface (`ar-EG`)** with Right-to-Left (RTL) layout.
- **Key Actions**: Review verification requests in Arabic, handle content reports, configure location lookups, inspect platform audit logs.

---

## 3. Core Feature Scope (المميزات الرئيسية)

```
                       ┌─────────────────────────────────────────┐
                       │           Delta Homes Platform          │
                       └───────────────────┬─────────────────────┘
                                           │
        ┌───────────────────┬──────────────┴──────┬───────────────────┐
        ▼                   ▼                     ▼                   ▼
┌──────────────┐    ┌──────────────┐      ┌──────────────┐    ┌──────────────┐
│  Bilingual   │    │ Services &   │      │ Engagement   │    │ Commercial & │
│  Discovery   │    │ Companies    │      │ & Chat       │    │ Admin (RTL)  │
├──────────────┤    ├──────────────┤      ├──────────────┤    ├──────────────┤
│• AR/EN App   │    │• Directory   │      │• Viewings    │    │• Plans       │
│• SQL Server  │    │• Finishing   │      │• Direct Chat │    │• Subscriptions│
│  Full-Text   │    │• Maintenance │      │• Reviews     │    │• Arabic Admin│
│• Lookups     │    │• Follow      │      │• Broadcasts  │    │• Fraud & Audit│
└──────────────┘    └──────────────┘      └──────────────┘    └──────────────┘
```

### 3.1 Authentication & Profile Management
- OTP-based authentication (Phone number and Email verification via SHA-256 OTP hashes).
- Role-based authorization (`CUSTOMER`, `OWNER`, `OFFICE`, `COMPANY`, `TECHNICIAN`, `ADMIN`).
- User profile updates with bilingual language preferences.

### 3.2 Property Discovery & Lookups
- Hierarchical location management (Governorates, Cities, Districts) with localized Arabic and English names (`nameAr`, `nameEn`).
- Property catalog for Sale and Rent with flexible feature tagging.
- Microsoft SQL Server Full-Text Search (`FULLTEXT INDEX`) supporting combined Arabic and English search queries.
- Saved properties and saved search alert criteria.

### 3.3 Company Directory & Services
- Multi-category company profiles (Real Estate Brokerage, Finishing/Decor, Maintenance Services).
- Company team member management with role delegation (`OWNER`, `MANAGER`, `AGENT`).
- User-to-Company follow mechanism.

### 3.4 Appointments & Viewing Requests
- Interactive viewing booking flow between seekers and property owners/brokers.
- Lifecycle states: `PENDING` ➔ `ACCEPTED` / `REJECTED` ➔ `COMPLETED` / `CANCELLED`.

### 3.5 Interaction-Gated Reviews & Trust System
- Entity-generic review framework (rate and review Properties, Companies, Services).
- Interaction verification gate (users can only review properties after confirmed viewings).

### 3.6 Communication & Broadcasts
- Real-time messaging / direct chat between users and property agents.
- Targeted broadcast notifications sent by subscribed companies in Arabic and/or English.

### 3.7 Monetization & Commercial Subscriptions
- Tiered subscription plans (Free, Basic, Premium, Enterprise).
- Coupon code discount engine for plan subscriptions.
- Arabic Admin commercial management interface.

---

## 4. Key Performance Indicators (مؤشرات النجاح - KPIs)

| Category | Metric | Target Objective |
|---|---|---|
| **User Acquisition** | Monthly Active Users (MAU) | Consistent month-over-month growth across Arabic & English mobile users |
| **Engagement** | Saved Properties & Inquiries | Average of 3+ interactions per active user session |
| **Lead Conversion** | Viewing Completion Rate | > 65% of booked viewings progressing to COMPLETED state |
| **Monetization** | Paid Subscription Conversion | > 15% of active companies upgrading to paid tiers |
| **Trust & Quality** | Verified Listings & Fraud | < 1% flagged content; sub-24h Arabic admin report resolution |

---

## 5. Technology Stack Summary

- **Backend**: Spring Boot 3.x (Java 17+)
- **Database**: Microsoft SQL Server 2022+ with Full-Text Search
- **ORM**: Spring Data JPA + Hibernate
- **Security**: Spring Security + JWT
- **Localization**: Spring MessageSource with ResourceBundle (Arabic & English)
- **Admin Dashboard**: React or Vue.js with RTL support
- **Testing**: JUnit 5, Mockito, Testcontainers
- **Build**: Maven or Gradle
