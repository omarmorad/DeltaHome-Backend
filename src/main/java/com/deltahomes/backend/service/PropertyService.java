package com.deltahomes.backend.service;

import com.deltahomes.backend.entity.enums.PropertyStatus;
import com.deltahomes.backend.entity.property.Property;
import com.deltahomes.backend.exception.ResourceNotFoundException;
import com.deltahomes.backend.repository.PropertyRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class PropertyService {

    private final PropertyRepository propertyRepository;

    public PropertyService(PropertyRepository propertyRepository) {
        this.propertyRepository = propertyRepository;
    }

    public Page<Property> getPublishedProperties(Pageable pageable) {
        return propertyRepository.findByStatus(PropertyStatus.PUBLISHED, pageable);
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

    public Page<Property> searchProperties(UUID cityId, UUID districtId,
                                           String purpose, Double minPrice,
                                           Double maxPrice, Pageable pageable) {
        return propertyRepository.searchProperties(
                cityId, districtId, purpose, minPrice, maxPrice, pageable);
    }
}
