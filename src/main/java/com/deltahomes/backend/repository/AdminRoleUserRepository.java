package com.deltahomes.backend.repository;

import com.deltahomes.backend.entity.user.AdminRoleUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface AdminRoleUserRepository extends JpaRepository<AdminRoleUser, UUID> {
}
