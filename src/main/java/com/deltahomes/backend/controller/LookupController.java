package com.deltahomes.backend.controller;

import com.deltahomes.backend.dto.common.PaginatedResponse;
import com.deltahomes.backend.entity.commerce.SubscriptionPlan;
import com.deltahomes.backend.entity.enums.SubscriptionTier;
import com.deltahomes.backend.entity.location.City;
import com.deltahomes.backend.entity.location.District;
import com.deltahomes.backend.entity.location.Feature;
import com.deltahomes.backend.entity.location.Service;
import com.deltahomes.backend.service.LookupService;
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
    public ResponseEntity<PaginatedResponse<City>> cities(@RequestParam(required = false) String q,
                                                          Pageable pageable) {
        return ResponseEntity.ok(lookupService.indexCities(q, pageable));
    }

    @GetMapping("/districts")
    public ResponseEntity<PaginatedResponse<District>> districts(@RequestParam(required = false) String q,
                                                                 @RequestParam(required = false) UUID cityId,
                                                                 Pageable pageable) {
        return ResponseEntity.ok(lookupService.indexDistricts(q, cityId, pageable));
    }

    @GetMapping("/services")
    public ResponseEntity<PaginatedResponse<Service>> services(@RequestParam(required = false) String q,
                                                               @RequestParam(required = false) String category,
                                                               Pageable pageable) {
        return ResponseEntity.ok(lookupService.indexServices(q, category, pageable));
    }

    @GetMapping("/features")
    public ResponseEntity<PaginatedResponse<Feature>> features(@RequestParam(required = false) String q,
                                                               Pageable pageable) {
        return ResponseEntity.ok(lookupService.indexFeatures(q, pageable));
    }

    @GetMapping("/plans")
    public ResponseEntity<PaginatedResponse<SubscriptionPlan>> plans(@RequestParam(required = false) String q,
                                                                     @RequestParam(required = false) SubscriptionTier tier,
                                                                     @RequestParam(required = false) Boolean isActive,
                                                                     Pageable pageable) {
        return ResponseEntity.ok(lookupService.indexPlans(q, tier, isActive, pageable));
    }
}
