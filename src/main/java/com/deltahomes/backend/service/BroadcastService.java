package com.deltahomes.backend.service;

import com.deltahomes.backend.entity.company.Company;
import com.deltahomes.backend.entity.enums.SubscriptionTier;
import com.deltahomes.backend.entity.marketing.Broadcast;
import com.deltahomes.backend.exception.BusinessException;
import com.deltahomes.backend.repository.BroadcastRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class BroadcastService {

    private final BroadcastRepository broadcastRepository;

    public BroadcastService(BroadcastRepository broadcastRepository) {
        this.broadcastRepository = broadcastRepository;
    }

    @Transactional
    public Broadcast createBroadcast(Company company, Broadcast broadcast) {
        // Quota enforcement
        long currentMonthCount = broadcastRepository.countByCompanyId(company.getId());
        int maxAllowed = getBroadcastCap(company.getPlan());

        if (currentMonthCount >= maxAllowed) {
            throw new BusinessException(
                "Broadcast quota exceeded. Plan allows " + maxAllowed + " per month.");
        }

        broadcast.setCompany(company);
        return broadcastRepository.save(broadcast);
    }

    private int getBroadcastCap(SubscriptionTier plan) {
        return switch (plan) {
            case BASIC -> 0;
            case PREMIUM -> 10;
            case ENTERPRISE -> 50;
        };
    }
}
