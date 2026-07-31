package com.deltahomes.backend.repository;

import com.deltahomes.backend.entity.moderation.FraudFlag;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface FraudFlagRepository extends JpaRepository<FraudFlag, UUID> {

    @Query(value = """
            SELECT * FROM fraud_flags ff
            WHERE (CAST(:flagType AS text) IS NULL OR ff.flag_type = CAST(:flagType AS text))
              AND (CAST(:status AS text) IS NULL OR ff.status = CAST(:status AS text))
            """,
            countQuery = """
            SELECT count(*) FROM fraud_flags ff
            WHERE (CAST(:flagType AS text) IS NULL OR ff.flag_type = CAST(:flagType AS text))
              AND (CAST(:status AS text) IS NULL OR ff.status = CAST(:status AS text))
            """,
            nativeQuery = true)
    Page<FraudFlag> searchIndex(@Param("flagType") String flagType,
                                @Param("status") String status,
                                Pageable pageable);
}
