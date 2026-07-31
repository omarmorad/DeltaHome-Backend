package com.deltahomes.backend.repository;

import com.deltahomes.backend.entity.property.PropertyVideo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface PropertyVideoRepository extends JpaRepository<PropertyVideo, UUID> {
}
