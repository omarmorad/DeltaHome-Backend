package com.deltahomes.backend.repository;

import com.deltahomes.backend.dto.summary.UserSummary;
import com.deltahomes.backend.entity.user.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<User, UUID> {
    Optional<User> findByPhone(String phone);
    Optional<User> findByEmail(String email);
    boolean existsByPhone(String phone);

    @Query(value = """
            SELECT u.id, u.name, u.phone, u.email, u.role, u.status,
                   u.verification_level AS verificationLevel,
                   u.created_at AS createdAt, u.last_login_at AS lastLoginAt
            FROM users u
            WHERE (CAST(:role AS text) IS NULL OR u.role = CAST(:role AS text))
              AND (CAST(:status AS text) IS NULL OR u.status = CAST(:status AS text))
              AND (CAST(:q AS text) = '' OR websearch_to_tsquery('simple', :q) @@ u.search_vector)
            """,
            countQuery = """
            SELECT count(*) FROM users u
            WHERE (CAST(:role AS text) IS NULL OR u.role = CAST(:role AS text))
              AND (CAST(:status AS text) IS NULL OR u.status = CAST(:status AS text))
              AND (CAST(:q AS text) = '' OR websearch_to_tsquery('simple', :q) @@ u.search_vector)
            """,
            nativeQuery = true)
    Page<UserSummary> searchIndex(@Param("q") String q,
                                  @Param("role") String role,
                                  @Param("status") String status,
                                  Pageable pageable);
}
