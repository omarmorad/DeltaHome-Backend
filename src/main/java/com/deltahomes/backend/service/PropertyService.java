package com.deltahomes.backend.service;

import com.deltahomes.backend.dto.common.PaginatedResponse;
import com.deltahomes.backend.dto.summary.PropertySummary;
import com.deltahomes.backend.entity.enums.PropertyPurpose;
import com.deltahomes.backend.entity.enums.PropertyStatus;
import com.deltahomes.backend.entity.property.Property;
import com.deltahomes.backend.entity.user.User;
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
                status,
                q == null ? "" : q.trim(),
                cityId,
                districtId,
                purpose,
                minPrice,
                maxPrice,
                PageUtils.normalizeSort(pageable))
            .map(this::toSummary);
        return PaginatedResponse.from(page);
    }

    public Property getPropertyById(UUID id) {
        return propertyRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Property", id));
    }

    @Transactional
    public Property createProperty(User owner, Property property) {
        property.setId(null);
        property.setOwner(owner);
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

    private PropertySummary toSummary(Property p) {
        return new PropertySummary() {
            @Override public UUID getId() { return p.getId(); }
            @Override public String getTitle() { return p.getTitle(); }
            @Override public String getDescription() { return p.getDescription(); }
            @Override public BigDecimal getPrice() { return p.getPrice(); }
            @Override public String getPurpose() { return p.getPurpose() != null ? p.getPurpose().name() : null; }
            @Override public String getCategory() { return p.getCategory(); }
            @Override public String getStatus() { return p.getStatus() != null ? p.getStatus().name() : null; }
            @Override public String getCityName() { return p.getCity() != null ? p.getCity().getName() : null; }
            @Override public String getDistrictName() { return p.getDistrict() != null ? p.getDistrict().getName() : null; }
            @Override public String getStreet() { return p.getStreet(); }
            @Override public String getReadiness() { return p.getReadiness() != null ? p.getReadiness().name() : null; }
            @Override public String getFinishingLevel() { return p.getFinishingLevel() != null ? p.getFinishingLevel().name() : null; }
            @Override public Boolean getIsFeatured() { return p.getIsFeatured(); }
            @Override public String getFeatures() { return p.getFeatures(); }
            @Override public java.time.OffsetDateTime getCreatedAt() { return p.getCreatedAt(); }
        };
    }
}
