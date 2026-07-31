package com.deltahomes.backend.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Creates a generated full-text search vector column + GIN index on every
 * searchable table. Runs after Hibernate's schema update so the tables exist,
 * and is idempotent (ADD COLUMN IF NOT EXISTS / CREATE INDEX IF NOT EXISTS).
 */
@Component
public class SearchVectorInitializer implements CommandLineRunner {

    private static final Map<String, String> EXPRESSIONS = new LinkedHashMap<>();

    static {
        EXPRESSIONS.put("properties",
                "to_tsvector('simple', coalesce(title,'') || ' ' || coalesce(description,''))");
        EXPRESSIONS.put("companies",
                "to_tsvector('simple', coalesce(name,'') || ' ' || coalesce(description,'') || ' ' || coalesce(phone,'') || ' ' || coalesce(email,'') || ' ' || coalesce(website,''))");
        EXPRESSIONS.put("cities",
                "to_tsvector('simple', coalesce(name,'') || ' ' || coalesce(name_ar,''))");
        EXPRESSIONS.put("districts",
                "to_tsvector('simple', coalesce(name,'') || ' ' || coalesce(name_ar,''))");
        EXPRESSIONS.put("services",
                "to_tsvector('simple', coalesce(name,'') || ' ' || coalesce(name_ar,'') || ' ' || coalesce(category,''))");
        EXPRESSIONS.put("features",
                "to_tsvector('simple', coalesce(name,'') || ' ' || coalesce(name_ar,''))");
        EXPRESSIONS.put("subscription_plans",
                "to_tsvector('simple', coalesce(name,''))");
        EXPRESSIONS.put("users",
                "to_tsvector('simple', coalesce(name,'') || ' ' || coalesce(phone,'') || ' ' || coalesce(email,''))");
        EXPRESSIONS.put("broadcasts",
                "to_tsvector('simple', coalesce(title,'') || ' ' || coalesce(body,''))");
        EXPRESSIONS.put("reviews",
                "to_tsvector('simple', coalesce(comment,''))");
        EXPRESSIONS.put("notifications",
                "to_tsvector('simple', coalesce(title,'') || ' ' || coalesce(body,''))");
        EXPRESSIONS.put("coupons",
                "to_tsvector('simple', coalesce(code,''))");
        EXPRESSIONS.put("reports",
                "to_tsvector('simple', coalesce(reason,'') || ' ' || coalesce(decision,''))");
        EXPRESSIONS.put("audit_logs",
                "to_tsvector('simple', coalesce(action,'') || ' ' || coalesce(reason,''))");
        EXPRESSIONS.put("messages",
                "to_tsvector('simple', coalesce(text_body,''))");
    }

    private final JdbcTemplate jdbcTemplate;

    public SearchVectorInitializer(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void run(String... args) {
        EXPRESSIONS.forEach((table, expression) -> {
            jdbcTemplate.execute("ALTER TABLE " + table
                    + " ADD COLUMN IF NOT EXISTS search_vector tsvector GENERATED ALWAYS AS ("
                    + expression + ") STORED");
            jdbcTemplate.execute("CREATE INDEX IF NOT EXISTS idx_" + table
                    + "_search_vector ON " + table + " USING GIN (search_vector)");
        });
    }
}
