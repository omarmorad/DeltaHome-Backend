-- V3: Bilingual content columns for user-entered data.
-- The platform serves an Arabic-first audience: owners/companies/admins enter
-- content in BOTH languages; responses are localized per request locale
-- (Accept-Language or ?lang=), falling back to the base column when the
-- localized one is empty.

ALTER TABLE properties ADD COLUMN IF NOT EXISTS title_ar VARCHAR(200);
ALTER TABLE properties ADD COLUMN IF NOT EXISTS description_ar TEXT;

ALTER TABLE companies ADD COLUMN IF NOT EXISTS name_ar VARCHAR(200);
ALTER TABLE companies ADD COLUMN IF NOT EXISTS description_ar TEXT;
