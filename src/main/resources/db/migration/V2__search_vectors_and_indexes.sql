-- V2: Postgres-specific schema refinements.
-- Idempotent where possible so it applies cleanly to both fresh databases
-- (after V1) and pre-existing databases created by the legacy ddl-auto:update
-- flow (which baseline at V1 and then receive this script).

-- ---------------------------------------------------------------------------
-- Columns added by code changes after the initial schema
-- ---------------------------------------------------------------------------

-- OtpService rate limiting keeps invalidated codes for send counting
ALTER TABLE otp_codes ADD COLUMN IF NOT EXISTS invalidated BOOLEAN NOT NULL DEFAULT FALSE;

-- Refresh-token rotation/revocation stores the active refresh jti per user
ALTER TABLE users ADD COLUMN IF NOT EXISTS refresh_token_id VARCHAR(64);

-- ---------------------------------------------------------------------------
-- Conversations: enforce one row per unordered user pair
-- ---------------------------------------------------------------------------

-- Normalize pair ordering to match ChatService (lower uuid first)
UPDATE conversations SET user_one_id = user_two_id, user_two_id = user_one_id
WHERE user_one_id > user_two_id;

-- Remove duplicate pairs created by the old race condition (keep newest)
DELETE FROM conversations a
USING conversations b
WHERE a.id <> b.id
  AND a.user_one_id = b.user_one_id
  AND a.user_two_id = b.user_two_id
  AND a.created_at < b.created_at;

DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM pg_constraint WHERE conname = 'uq_conversation_user_pair') THEN
        ALTER TABLE conversations
            ADD CONSTRAINT uq_conversation_user_pair UNIQUE (user_one_id, user_two_id);
    END IF;
END $$;

CREATE INDEX IF NOT EXISTS idx_conversations_user_one ON conversations (user_one_id);
CREATE INDEX IF NOT EXISTS idx_conversations_user_two ON conversations (user_two_id);

-- ---------------------------------------------------------------------------
-- Full-text search vectors + GIN indexes
-- (Mirrors SearchVectorInitializer; kept in migration form for fresh DBs.)
-- ---------------------------------------------------------------------------

ALTER TABLE properties ADD COLUMN IF NOT EXISTS search_vector tsvector GENERATED ALWAYS AS (to_tsvector('simple', coalesce(title,'') || ' ' || coalesce(description,''))) STORED;
CREATE INDEX IF NOT EXISTS idx_properties_search_vector ON properties USING GIN (search_vector);

ALTER TABLE companies ADD COLUMN IF NOT EXISTS search_vector tsvector GENERATED ALWAYS AS (to_tsvector('simple', coalesce(name,'') || ' ' || coalesce(description,'') || ' ' || coalesce(phone,'') || ' ' || coalesce(email,'') || ' ' || coalesce(website,''))) STORED;
CREATE INDEX IF NOT EXISTS idx_companies_search_vector ON companies USING GIN (search_vector);

ALTER TABLE cities ADD COLUMN IF NOT EXISTS search_vector tsvector GENERATED ALWAYS AS (to_tsvector('simple', coalesce(name,'') || ' ' || coalesce(name_ar,''))) STORED;
CREATE INDEX IF NOT EXISTS idx_cities_search_vector ON cities USING GIN (search_vector);

ALTER TABLE districts ADD COLUMN IF NOT EXISTS search_vector tsvector GENERATED ALWAYS AS (to_tsvector('simple', coalesce(name,'') || ' ' || coalesce(name_ar,''))) STORED;
CREATE INDEX IF NOT EXISTS idx_districts_search_vector ON districts USING GIN (search_vector);

ALTER TABLE services ADD COLUMN IF NOT EXISTS search_vector tsvector GENERATED ALWAYS AS (to_tsvector('simple', coalesce(name,'') || ' ' || coalesce(name_ar,'') || ' ' || coalesce(category,''))) STORED;
CREATE INDEX IF NOT EXISTS idx_services_search_vector ON services USING GIN (search_vector);

ALTER TABLE features ADD COLUMN IF NOT EXISTS search_vector tsvector GENERATED ALWAYS AS (to_tsvector('simple', coalesce(name,'') || ' ' || coalesce(name_ar,''))) STORED;
CREATE INDEX IF NOT EXISTS idx_features_search_vector ON features USING GIN (search_vector);

ALTER TABLE subscription_plans ADD COLUMN IF NOT EXISTS search_vector tsvector GENERATED ALWAYS AS (to_tsvector('simple', coalesce(name,''))) STORED;
CREATE INDEX IF NOT EXISTS idx_subscription_plans_search_vector ON subscription_plans USING GIN (search_vector);

ALTER TABLE users ADD COLUMN IF NOT EXISTS search_vector tsvector GENERATED ALWAYS AS (to_tsvector('simple', coalesce(name,'') || ' ' || coalesce(phone,'') || ' ' || coalesce(email,''))) STORED;
CREATE INDEX IF NOT EXISTS idx_users_search_vector ON users USING GIN (search_vector);

ALTER TABLE broadcasts ADD COLUMN IF NOT EXISTS search_vector tsvector GENERATED ALWAYS AS (to_tsvector('simple', coalesce(title,'') || ' ' || coalesce(body,''))) STORED;
CREATE INDEX IF NOT EXISTS idx_broadcasts_search_vector ON broadcasts USING GIN (search_vector);

ALTER TABLE reviews ADD COLUMN IF NOT EXISTS search_vector tsvector GENERATED ALWAYS AS (to_tsvector('simple', coalesce(comment,''))) STORED;
CREATE INDEX IF NOT EXISTS idx_reviews_search_vector ON reviews USING GIN (search_vector);

ALTER TABLE notifications ADD COLUMN IF NOT EXISTS search_vector tsvector GENERATED ALWAYS AS (to_tsvector('simple', coalesce(title,'') || ' ' || coalesce(body,''))) STORED;
CREATE INDEX IF NOT EXISTS idx_notifications_search_vector ON notifications USING GIN (search_vector);

ALTER TABLE coupons ADD COLUMN IF NOT EXISTS search_vector tsvector GENERATED ALWAYS AS (to_tsvector('simple', coalesce(code,''))) STORED;
CREATE INDEX IF NOT EXISTS idx_coupons_search_vector ON coupons USING GIN (search_vector);

ALTER TABLE reports ADD COLUMN IF NOT EXISTS search_vector tsvector GENERATED ALWAYS AS (to_tsvector('simple', coalesce(reason,'') || ' ' || coalesce(decision,''))) STORED;
CREATE INDEX IF NOT EXISTS idx_reports_search_vector ON reports USING GIN (search_vector);

ALTER TABLE audit_logs ADD COLUMN IF NOT EXISTS search_vector tsvector GENERATED ALWAYS AS (to_tsvector('simple', coalesce(action,'') || ' ' || coalesce(reason,''))) STORED;
CREATE INDEX IF NOT EXISTS idx_audit_logs_search_vector ON audit_logs USING GIN (search_vector);

ALTER TABLE messages ADD COLUMN IF NOT EXISTS search_vector tsvector GENERATED ALWAYS AS (to_tsvector('simple', coalesce(text_body,''))) STORED;
CREATE INDEX IF NOT EXISTS idx_messages_search_vector ON messages USING GIN (search_vector);

-- ---------------------------------------------------------------------------
-- Trigram indexes for substring search fallbacks
-- ---------------------------------------------------------------------------

CREATE EXTENSION IF NOT EXISTS pg_trgm;

CREATE INDEX IF NOT EXISTS idx_properties_title_trgm
    ON properties USING gin (title gin_trgm_ops);

CREATE INDEX IF NOT EXISTS idx_properties_description_trgm
    ON properties USING gin (description gin_trgm_ops);
