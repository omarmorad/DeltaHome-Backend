package com.deltahomes.backend.repository;

import com.deltahomes.backend.entity.location.Feature;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface FeatureRepository extends JpaRepository<Feature, UUID> {

    @Query(value = """
            SELECT * FROM features f
            WHERE (CAST(:q AS text) = '' OR websearch_to_tsquery('simple', :q) @@ f.search_vector)
            """,
            countQuery = """
            SELECT count(*) FROM features f
            WHERE (CAST(:q AS text) = '' OR websearch_to_tsquery('simple', :q) @@ f.search_vector)
            """,
            nativeQuery = true)
    Page<Feature> searchIndex(@Param("q") String q, Pageable pageable);
}
