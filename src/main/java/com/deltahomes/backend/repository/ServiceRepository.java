package com.deltahomes.backend.repository;

import com.deltahomes.backend.entity.location.Service;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface ServiceRepository extends JpaRepository<Service, UUID> {

    @Query(value = """
            SELECT * FROM services s
            WHERE (CAST(:q AS text) = '' OR websearch_to_tsquery('simple', :q) @@ s.search_vector)
              AND (CAST(:category AS text) IS NULL OR s.category = CAST(:category AS text))
            """,
            countQuery = """
            SELECT count(*) FROM services s
            WHERE (CAST(:q AS text) = '' OR websearch_to_tsquery('simple', :q) @@ s.search_vector)
              AND (CAST(:category AS text) IS NULL OR s.category = CAST(:category AS text))
            """,
            nativeQuery = true)
    Page<Service> searchIndex(@Param("q") String q, @Param("category") String category, Pageable pageable);
}
