package com.deltahomes.backend.controller;

import com.deltahomes.backend.dto.common.ApiResponse;
import com.deltahomes.backend.dto.property.PropertyDtos.CreatePropertyRequest;
import com.deltahomes.backend.dto.property.PropertyDtos.PropertyDetailResponse;
import com.deltahomes.backend.dto.property.PropertyDtos.UpdatePropertyRequest;
import com.deltahomes.backend.dto.summary.PropertySummary;
import com.deltahomes.backend.entity.enums.PropertyPurpose;
import com.deltahomes.backend.entity.enums.PropertyStatus;
import com.deltahomes.backend.entity.user.User;
import com.deltahomes.backend.service.PropertyService;
import com.deltahomes.backend.service.UserContext;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/properties")
public class PropertyController {

    private final PropertyService propertyService;
    private final UserContext userContext;

    public PropertyController(PropertyService propertyService, UserContext userContext) {
        this.propertyService = propertyService;
        this.userContext = userContext;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<java.util.List<PropertySummary>>> index(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) UUID cityId,
            @RequestParam(required = false) UUID districtId,
            @RequestParam(required = false) PropertyPurpose purpose,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice,
            Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.page(propertyService.index(
                q, cityId, districtId, purpose, minPrice, maxPrice,
                PropertyStatus.PUBLISHED, pageable)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<PropertyDetailResponse>> getProperty(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.ok(propertyService.getPropertyDetail(id)));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<PropertyDetailResponse>> createProperty(
            @AuthenticationPrincipal UserDetails principal,
            @Valid @RequestBody CreatePropertyRequest request) {
        User owner = userContext.currentUser(principal);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok(propertyService.createProperty(owner, request)));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<ApiResponse<PropertyDetailResponse>> updateProperty(
            @AuthenticationPrincipal UserDetails principal,
            @PathVariable UUID id,
            @Valid @RequestBody UpdatePropertyRequest request) {
        User actor = userContext.currentUser(principal);
        return ResponseEntity.ok(ApiResponse.ok(propertyService.updateProperty(id, actor, request)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProperty(
            @AuthenticationPrincipal UserDetails principal,
            @PathVariable UUID id) {
        User actor = userContext.currentUser(principal);
        propertyService.deleteProperty(id, actor);
        return ResponseEntity.noContent().build();
    }
}
