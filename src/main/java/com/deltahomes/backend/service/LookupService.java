package com.deltahomes.backend.service;

import com.deltahomes.backend.dto.common.PaginatedResponse;
import com.deltahomes.backend.entity.commerce.SubscriptionPlan;
import com.deltahomes.backend.entity.enums.SubscriptionTier;
import com.deltahomes.backend.entity.location.City;
import com.deltahomes.backend.entity.location.District;
import com.deltahomes.backend.entity.location.Feature;
import com.deltahomes.backend.entity.location.Service;
import com.deltahomes.backend.repository.CityRepository;
import com.deltahomes.backend.repository.DistrictRepository;
import com.deltahomes.backend.repository.FeatureRepository;
import com.deltahomes.backend.repository.ServiceRepository;
import com.deltahomes.backend.repository.SubscriptionPlanRepository;
import com.deltahomes.backend.util.PageUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

@org.springframework.stereotype.Service
public class LookupService {

    private final CityRepository cityRepository;
    private final DistrictRepository districtRepository;
    private final ServiceRepository serviceRepository;
    private final FeatureRepository featureRepository;
    private final SubscriptionPlanRepository subscriptionPlanRepository;

    public LookupService(CityRepository cityRepository,
                         DistrictRepository districtRepository,
                         ServiceRepository serviceRepository,
                         FeatureRepository featureRepository,
                         SubscriptionPlanRepository subscriptionPlanRepository) {
        this.cityRepository = cityRepository;
        this.districtRepository = districtRepository;
        this.serviceRepository = serviceRepository;
        this.featureRepository = featureRepository;
        this.subscriptionPlanRepository = subscriptionPlanRepository;
    }

    public PaginatedResponse<City> indexCities(String q, Pageable pageable) {
        Page<City> page = cityRepository.searchIndex(q == null ? "" : q.trim(), PageUtils.normalizeSort(pageable));
        return PaginatedResponse.from(page);
    }

    public PaginatedResponse<District> indexDistricts(String q, UUID cityId, Pageable pageable) {
        Page<District> page = districtRepository.searchIndex(
                q == null ? "" : q.trim(), cityId, PageUtils.normalizeSort(pageable));
        return PaginatedResponse.from(page);
    }

    public PaginatedResponse<Service> indexServices(String q, String category, Pageable pageable) {
        Page<Service> page = serviceRepository.searchIndex(
                q == null ? "" : q.trim(), category, PageUtils.normalizeSort(pageable));
        return PaginatedResponse.from(page);
    }

    public PaginatedResponse<Feature> indexFeatures(String q, Pageable pageable) {
        Page<Feature> page = featureRepository.searchIndex(q == null ? "" : q.trim(), PageUtils.normalizeSort(pageable));
        return PaginatedResponse.from(page);
    }

    public PaginatedResponse<SubscriptionPlan> indexPlans(String q, SubscriptionTier tier,
                                                          Boolean isActive, Pageable pageable) {
        Page<SubscriptionPlan> page = subscriptionPlanRepository.searchIndex(
                q == null ? "" : q.trim(),
                tier == null ? null : tier.name(),
                isActive,
                PageUtils.normalizeSort(pageable));
        return PaginatedResponse.from(page);
    }
}
