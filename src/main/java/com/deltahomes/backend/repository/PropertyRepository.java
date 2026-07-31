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

@Repository
public interface PropertyRepository extends JpaRepository<Property, Long> {

    Page<Property> findByOwnerIdAndStatusNot(Long ownerId, PropertyStatus status, Pageable pageable);

    Page<Property> findByStatus(PropertyStatus status, Pageable pageable);

    @Query("SELECT p FROM Property p WHERE p.status = 'PUBLISHED' " +
           "AND (:cityId IS NULL OR p.city.id = :cityId) " +
           "AND (:districtId IS NULL OR p.district.id = :districtId) " +
           "AND (:purpose IS NULL OR p.purpose = :purpose) " +
           "AND (:minPrice IS NULL OR p.price >= :minPrice) " +
           "AND (:maxPrice IS NULL OR p.price <= :maxPrice)")
    Page<Property> searchProperties(@Param("cityId") Long cityId,
                                    @Param("districtId") Long districtId,
                                    @Param("purpose") String purpose,
                                    @Param("minPrice") Double minPrice,
                                    @Param("maxPrice") Double maxPrice,
                                    Pageable pageable);

    // Duplicate detection (V1): exact-match on owner_id + price + district_id
    List<Property> findByOwnerIdAndPriceAndDistrictId(Long ownerId, java.math.BigDecimal price, Long districtId);
}
