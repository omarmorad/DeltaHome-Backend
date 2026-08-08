package com.deltahomes.backend.controller;

import com.deltahomes.backend.dto.common.PaginatedResponse;
import com.deltahomes.backend.dto.summary.CitySummary;
import com.deltahomes.backend.dto.summary.DistrictSummary;
import com.deltahomes.backend.dto.summary.FeatureSummary;
import com.deltahomes.backend.dto.summary.ServiceSummary;
import com.deltahomes.backend.dto.summary.SubscriptionPlanSummary;
import com.deltahomes.backend.entity.commerce.SubscriptionPlan;
import com.deltahomes.backend.entity.enums.SubscriptionTier;
import com.deltahomes.backend.entity.location.City;
import com.deltahomes.backend.entity.location.District;
import com.deltahomes.backend.entity.location.Feature;
import com.deltahomes.backend.entity.location.Service;
import com.deltahomes.backend.service.LookupService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1")
public class LookupController {

    private final LookupService lookupService;

    public LookupController(LookupService lookupService) {
        this.lookupService = lookupService;
    }

    @GetMapping("/cities")
    public ResponseEntity<PaginatedResponse<CitySummary>> cities(@RequestParam(required = false) String q,
                                                                 Pageable pageable) {
        Page<CitySummary> cityPage = lookupService.indexCities(q, pageable).map(this::toCitySummary);
        return ResponseEntity.ok(PaginatedResponse.from(cityPage));
    }

    @GetMapping("/districts")
    public ResponseEntity<PaginatedResponse<DistrictSummary>> districts(@RequestParam(required = false) String q,
                                                                        @RequestParam(required = false) UUID cityId,
                                                                        Pageable pageable) {
        Page<DistrictSummary> districtPage = lookupService.indexDistricts(q, cityId, pageable).map(this::toDistrictSummary);
        return ResponseEntity.ok(PaginatedResponse.from(districtPage));
    }

    @GetMapping("/services")
    public ResponseEntity<PaginatedResponse<ServiceSummary>> services(@RequestParam(required = false) String q,
                                                                      @RequestParam(required = false) String category,
                                                                      Pageable pageable) {
        Page<ServiceSummary> servicePage = lookupService.indexServices(q, category, pageable).map(this::toServiceSummary);
        return ResponseEntity.ok(PaginatedResponse.from(servicePage));
    }

    @GetMapping("/features")
    public ResponseEntity<PaginatedResponse<FeatureSummary>> features(@RequestParam(required = false) String q,
                                                                      Pageable pageable) {
        Page<FeatureSummary> featurePage = lookupService.indexFeatures(q, pageable).map(this::toFeatureSummary);
        return ResponseEntity.ok(PaginatedResponse.from(featurePage));
    }

    @GetMapping("/plans")
    public ResponseEntity<PaginatedResponse<SubscriptionPlanSummary>> plans(@RequestParam(required = false) String q,
                                                                            @RequestParam(required = false) SubscriptionTier tier,
                                                                            @RequestParam(required = false) Boolean isActive,
                                                                            Pageable pageable) {
        Page<SubscriptionPlanSummary> planPage = lookupService.indexPlans(q, tier, isActive, pageable).map(this::toSubscriptionPlanSummary);
        return ResponseEntity.ok(PaginatedResponse.from(planPage));
    }

    // Mapping methods
    private CitySummary toCitySummary(City city) {
        return new CitySummary(
                city.getId(),
                city.getName(),
                city.getNameAr(),
                city.getIsActive(),
                city.getCreatedAt()
        );
    }

    private DistrictSummary toDistrictSummary(District district) {
        return new DistrictSummary(
                district.getId(),
                district.getName(),
                district.getNameAr(),
                district.getCity() != null ? district.getCity().getId() : null,
                district.getCity() != null ? district.getCity().getName() : null,
                district.getCity() != null ? district.getCity().getNameAr() : null,
                district.getCreatedAt()
        );
    }

    private ServiceSummary toServiceSummary(Service service) {
        return new ServiceSummary(
                service.getId(),
                service.getName(),
                service.getNameAr(),
                service.getNameEn() != null ? service.getNameEn() : "",
                service.getCategory() != null ? service.getCategory() : "",
                service.getIconUrl() != null ? service.getIconUrl() : "",
                service.getCreatedAt()
        );
    }

    private FeatureSummary toFeatureSummary(Feature feature) {
        return new FeatureSummary(
                feature.getId(),
                feature.getName(),
                feature.getNameAr(),
                feature.getNameEn() != null ? feature.getNameEn() : "",
                feature.getIcon() != null ? feature.getIcon() : "",
                feature.getCreatedAt()
        );
    }

    private SubscriptionPlanSummary toSubscriptionPlanSummary(SubscriptionPlan plan) {
        return new SubscriptionPlanSummary(
                plan.getId(),
                plan.getName(),
                plan.getNameAr() != null ? plan.getNameAr() : "",
                plan.getNameEn() != null ? plan.getNameEn() : "",
                plan.getTier() != null ? plan.getTier().name() : "",
                plan.getPrice(),
                plan.getListingQuota(),
                plan.getBroadcastQuota(),
                plan.getIsActive(),
                plan.getCreatedAt()
        );
    }
}
