package com.deltahomes.backend.repository;

import com.deltahomes.backend.entity.marketing.BroadcastDelivery;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface BroadcastDeliveryRepository extends JpaRepository<BroadcastDelivery, UUID> {
}
