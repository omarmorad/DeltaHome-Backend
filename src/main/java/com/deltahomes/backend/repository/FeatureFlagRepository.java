package com.deltahomes.backend.repository;

import com.deltahomes.backend.entity.admin.FeatureFlag;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface FeatureFlagRepository extends JpaRepository<FeatureFlag, UUID> {
}
