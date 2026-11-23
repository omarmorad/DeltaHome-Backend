# Delta Homes Backend

Backend API for Delta Homes, a multi-sided home-lifecycle marketplace for property owners, service companies and technicians. Listings, service requests, visit booking, real-time chat and search.

**Stack:** Java 21, Spring Boot, Spring Security (JWT), Spring Data JPA / Hibernate, PostgreSQL, Flyway, Redis (cache), Meilisearch (search), Firebase (push), Twilio (SMS OTP), S3-compatible storage, MapStruct, Maven.

## Highlights

- Role-based access control with short-lived access tokens and rotating, revocable refresh tokens.
- OTP login by SMS with rate limits (5 sends per 15 minutes per phone and purpose, 60 s resend cooldown, 5 attempts).
- Schema owned by Flyway migrations (`db/migration`); Hibernate only validates.
- Redis caching, Meilisearch full-text search, push notifications through Firebase.
- Demo data seeder for local development.

## Run locally

Requirements: JDK 21, Maven, PostgreSQL, Redis (Meilisearch optional for search).

```bash
cp .env.example .env      # fill in values, export them or use your IDE run config
createdb deltahome
mvn spring-boot:run
```

The API starts on `http://localhost:8080`. All configuration comes from environment variables listed in `.env.example`; `src/main/resources/application.yml` holds no secrets.

## Security note

Any credentials committed earlier in this repository's history were test values only and are not in use.
