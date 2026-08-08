package com.deltahomes.backend.repository;

import com.deltahomes.backend.entity.location.District;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface DistrictRepository extends JpaRepository<District, UUID> {

    @EntityGraph(attributePaths = {"city"})
    @Query(value = """
            SELECT * FROM districts d
            WHERE (CAST(:q AS text) = '' OR websearch_to_tsquery('simple', :q) @@ d.search_vector)
              AND (CAST(:cityId AS uuid) IS NULL OR d.city_id = CAST(:cityId AS uuid))
            """,
            countQuery = """
            SELECT count(*) FROM districts d
            WHERE (CAST(:q AS text) = '' OR websearch_to_tsquery('simple', :q) @@ d.search_vector)
              AND (CAST(:cityId AS uuid) IS NULL OR d.city_id = CAST(:cityId AS uuid))
            """,
            nativeQuery = true)
    Page<District> searchIndex(@Param("q") String q, @Param("cityId") UUID cityId, Pageable pageable);
}
