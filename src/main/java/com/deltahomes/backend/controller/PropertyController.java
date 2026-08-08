package com.deltahomes.backend.controller;

import com.deltahomes.backend.dto.common.PaginatedResponse;
import com.deltahomes.backend.dto.summary.PropertySummary;
import com.deltahomes.backend.entity.enums.PropertyPurpose;
import com.deltahomes.backend.entity.enums.PropertyStatus;
import com.deltahomes.backend.entity.property.Property;
import com.deltahomes.backend.entity.user.User;
import com.deltahomes.backend.service.PropertyService;
import com.deltahomes.backend.service.UserContext;
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
    public ResponseEntity<PaginatedResponse<PropertySummary>> index(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) UUID cityId,
            @RequestParam(required = false) UUID districtId,
            @RequestParam(required = false) PropertyPurpose purpose,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice,
            Pageable pageable) {
        return ResponseEntity.ok(propertyService.index(
                q, cityId, districtId, purpose, minPrice, maxPrice,
                PropertyStatus.PUBLISHED, pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Property> getProperty(@PathVariable UUID id) {
        return ResponseEntity.ok(propertyService.getPropertyById(id));
    }

    @PostMapping
    public ResponseEntity<Property> createProperty(@AuthenticationPrincipal UserDetails principal,
                                                   @RequestBody Property property) {
        User owner = userContext.currentUser(principal);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(propertyService.createProperty(owner, property));
    }
}
