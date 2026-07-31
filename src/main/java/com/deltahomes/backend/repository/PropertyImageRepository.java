package com.deltahomes.backend.repository;

import com.deltahomes.backend.entity.property.PropertyImage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface PropertyImageRepository extends JpaRepository<PropertyImage, UUID> {
}
