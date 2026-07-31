package com.deltahomes.backend.controller;

import com.deltahomes.backend.entity.property.Property;
import com.deltahomes.backend.service.PropertyService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/properties")
public class PropertyController {

    private final PropertyService propertyService;

    public PropertyController(PropertyService propertyService) {
        this.propertyService = propertyService;
    }

    @GetMapping
    public ResponseEntity<Page<Property>> searchProperties(
            @RequestParam(required = false) UUID cityId,
            @RequestParam(required = false) UUID districtId,
            @RequestParam(required = false) String purpose,
            @RequestParam(required = false) Double minPrice,
            @RequestParam(required = false) Double maxPrice,
            Pageable pageable) {

        Page<Property> properties = propertyService.searchProperties(
                cityId, districtId, purpose, minPrice, maxPrice, pageable);
        return ResponseEntity.ok(properties);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Property> getProperty(@PathVariable UUID id) {
        return ResponseEntity.ok(propertyService.getPropertyById(id));
    }

    @PostMapping
    public ResponseEntity<Property> createProperty(@RequestBody Property property) {
        return ResponseEntity.ok(propertyService.createProperty(property));
    }
}
