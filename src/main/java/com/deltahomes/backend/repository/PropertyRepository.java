package com.deltahomes.backend.repository;

import com.deltahomes.backend.entity.enums.PropertyStatus;
import com.deltahomes.backend.entity.property.Property;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface PropertyRepository extends JpaRepository<Property, UUID> {

    Page<Property> findByOwnerIdAndStatusNot(UUID ownerId, PropertyStatus status, Pageable pageable);

    Page<Property> findByStatus(PropertyStatus status, Pageable pageable);

    // Index: full-text search + optional filters, native query over the generated search_vector column.
    @Query(value = """
            SELECT p.id, p.title, p.description, p.price, p.purpose, p.category, p.status,
                   c.name AS cityName, d.name AS districtName, p.street, p.readiness,
                   p.finishing_level AS finishingLevel, p.is_featured AS isFeatured,
                   p.features, p.created_at AS createdAt
            FROM properties p
            LEFT JOIN cities c ON c.id = p.city_id
            LEFT JOIN districts d ON d.id = p.district_id
            WHERE (CAST(:status AS text) IS NULL OR p.status = CAST(:status AS text))
              AND (:q = '' OR websearch_to_tsquery('simple', :q) @@ p.search_vector)
              AND (CAST(:cityId AS uuid) IS NULL OR p.city_id = CAST(:cityId AS uuid))
              AND (CAST(:districtId AS uuid) IS NULL OR p.district_id = CAST(:districtId AS uuid))
              AND (CAST(:purpose AS text) IS NULL OR p.purpose = CAST(:purpose AS text))
              AND (CAST(:minPrice AS numeric) IS NULL OR p.price >= CAST(:minPrice AS numeric))
              AND (CAST(:maxPrice AS numeric) IS NULL OR p.price <= CAST(:maxPrice AS numeric))
            """,
            countQuery = """
            SELECT count(*) FROM properties p
            WHERE (CAST(:status AS text) IS NULL OR p.status = CAST(:status AS text))
              AND (:q = '' OR websearch_to_tsquery('simple', :q) @@ p.search_vector)
              AND (CAST(:cityId AS uuid) IS NULL OR p.city_id = CAST(:cityId AS uuid))
              AND (CAST(:districtId AS uuid) IS NULL OR p.district_id = CAST(:districtId AS uuid))
              AND (CAST(:purpose AS text) IS NULL OR p.purpose = CAST(:purpose AS text))
              AND (CAST(:minPrice AS numeric) IS NULL OR p.price >= CAST(:minPrice AS numeric))
              AND (CAST(:maxPrice AS numeric) IS NULL OR p.price <= CAST(:maxPrice AS numeric))
            """,
            nativeQuery = true)
    Page<com.deltahomes.backend.dto.summary.PropertySummary> searchIndex(@Param("status") String status,
                                                                         @Param("q") String q,
                                                                         @Param("cityId") UUID cityId,
                                                                         @Param("districtId") UUID districtId,
                                                                         @Param("purpose") String purpose,
                                                                         @Param("minPrice") java.math.BigDecimal minPrice,
                                                                         @Param("maxPrice") java.math.BigDecimal maxPrice,
                                                                         Pageable pageable);

    // Duplicate detection (V1): exact-match on owner_id + price + district_id
    List<Property> findByOwnerIdAndPriceAndDistrictId(UUID ownerId, java.math.BigDecimal price, UUID districtId);
}
