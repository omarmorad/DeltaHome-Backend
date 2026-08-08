package com.deltahomes.backend.repository;

import com.deltahomes.backend.entity.enums.PropertyPurpose;
import com.deltahomes.backend.entity.enums.PropertyStatus;
import com.deltahomes.backend.entity.property.Property;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Repository
public interface PropertyRepository extends JpaRepository<Property, UUID> {

    Page<Property> findByOwnerIdAndStatusNot(UUID ownerId, PropertyStatus status, Pageable pageable);

    Page<Property> findByStatus(PropertyStatus status, Pageable pageable);

    /**
     * Index query with eager fetching of owner, city, and district relationships.
     * Uses JPQL with @EntityGraph to avoid LazyInitializationException.
     * Search uses LIKE for simplicity; full-text search can be added via native count query if needed.
     */
    @EntityGraph(attributePaths = {"owner", "city", "district"})
    @Query("SELECT p FROM Property p " +
           "WHERE (:status IS NULL OR p.status = :status) " +
           "AND (:q = '' OR LOWER(p.title) LIKE LOWER(CONCAT('%', :q, '%')) OR LOWER(p.description) LIKE LOWER(CONCAT('%', :q, '%'))) " +
           "AND (:cityId IS NULL OR p.city.id = :cityId) " +
           "AND (:districtId IS NULL OR p.district.id = :districtId) " +
           "AND (:purpose IS NULL OR p.purpose = :purpose) " +
           "AND (:minPrice IS NULL OR p.price >= :minPrice) " +
           "AND (:maxPrice IS NULL OR p.price <= :maxPrice)")
    Page<Property> searchIndex(@Param("status") PropertyStatus status,
                               @Param("q") String q,
                               @Param("cityId") UUID cityId,
                               @Param("districtId") UUID districtId,
                               @Param("purpose") PropertyPurpose purpose,
                               @Param("minPrice") BigDecimal minPrice,
                               @Param("maxPrice") BigDecimal maxPrice,
                               Pageable pageable);

    // Duplicate detection (V1): exact-match on owner_id + price + district_id
    List<Property> findByOwnerIdAndPriceAndDistrictId(UUID ownerId, BigDecimal price, UUID districtId);
}
