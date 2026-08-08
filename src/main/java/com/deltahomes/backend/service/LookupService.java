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

    public Page<City> indexCities(String q, Pageable pageable) {
        return cityRepository.searchIndex(q == null ? "" : q.trim(), PageUtils.normalizeSort(pageable));
    }

    public Page<District> indexDistricts(String q, UUID cityId, Pageable pageable) {
        return districtRepository.searchIndex(
                q == null ? "" : q.trim(), cityId, PageUtils.normalizeSort(pageable));
    }

    public Page<Service> indexServices(String q, String category, Pageable pageable) {
        return serviceRepository.searchIndex(
                q == null ? "" : q.trim(), category, PageUtils.normalizeSort(pageable));
    }

    public Page<Feature> indexFeatures(String q, Pageable pageable) {
        return featureRepository.searchIndex(q == null ? "" : q.trim(), PageUtils.normalizeSort(pageable));
    }

    public Page<SubscriptionPlan> indexPlans(String q, SubscriptionTier tier,
                                             Boolean isActive, Pageable pageable) {
        return subscriptionPlanRepository.searchIndex(
                q == null ? "" : q.trim(),
                tier == null ? null : tier.name(),
                isActive,
                PageUtils.normalizeSort(pageable));
    }
}
