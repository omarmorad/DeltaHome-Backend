package com.deltahomes.backend.service;

import com.deltahomes.backend.dto.common.PaginatedResponse;
import com.deltahomes.backend.dto.summary.PropertySummary;
import com.deltahomes.backend.entity.enums.PropertyPurpose;
import com.deltahomes.backend.entity.enums.PropertyStatus;
import com.deltahomes.backend.entity.property.Property;
import com.deltahomes.backend.exception.ResourceNotFoundException;
import com.deltahomes.backend.repository.PropertyRepository;
import com.deltahomes.backend.util.PageUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.UUID;

@Service
public class PropertyService {

    private final PropertyRepository propertyRepository;

    public PropertyService(PropertyRepository propertyRepository) {
        this.propertyRepository = propertyRepository;
    }

    public PaginatedResponse<PropertySummary> index(String q, UUID cityId, UUID districtId,
                                                    PropertyPurpose purpose, BigDecimal minPrice,
                                                    BigDecimal maxPrice, PropertyStatus status,
                                                    Pageable pageable) {
        Page<PropertySummary> page = propertyRepository.searchIndex(
                status == null ? null : status.name(),
                q == null ? "" : q.trim(),
                cityId,
                districtId,
                purpose == null ? null : purpose.name(),
                minPrice,
                maxPrice,
                PageUtils.normalizeSort(pageable));
        return PaginatedResponse.from(page);
    }

    public Property getPropertyById(UUID id) {
        return propertyRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Property", id));
    }

    @Transactional
    public Property createProperty(Property property) {
        property.setStatus(PropertyStatus.DRAFT);
        return propertyRepository.save(property);
    }

    @Transactional
    public Property updateProperty(UUID id, Property updates) {
        Property existing = getPropertyById(id);
        // Merge fields — expand as needed
        existing.setTitle(updates.getTitle());
        existing.setDescription(updates.getDescription());
        existing.setPrice(updates.getPrice());
        existing.setStatus(updates.getStatus());
        return propertyRepository.save(existing);
    }
}
