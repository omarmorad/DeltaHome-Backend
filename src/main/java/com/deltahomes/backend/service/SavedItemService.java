package com.deltahomes.backend.service;

import com.deltahomes.backend.dto.common.PaginatedResponse;
import com.deltahomes.backend.dto.social.SocialDtos;
import com.deltahomes.backend.entity.SavedItem;
import com.deltahomes.backend.entity.enums.EntityType;
import com.deltahomes.backend.entity.user.User;
import com.deltahomes.backend.exception.BusinessException;
import com.deltahomes.backend.repository.CompanyRepository;
import com.deltahomes.backend.repository.PropertyRepository;
import com.deltahomes.backend.repository.SavedItemRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class SavedItemService {

    private final SavedItemRepository savedItemRepository;
    private final PropertyRepository propertyRepository;
    private final CompanyRepository companyRepository;

    public SavedItemService(SavedItemRepository savedItemRepository,
                            PropertyRepository propertyRepository,
                            CompanyRepository companyRepository) {
        this.savedItemRepository = savedItemRepository;
        this.propertyRepository = propertyRepository;
        this.companyRepository = companyRepository;
    }

    @Transactional
    public SavedItem save(User user, EntityType entityType, UUID entityId) {
        assertEntityExists(entityType, entityId);
        if (savedItemRepository.existsByUserIdAndEntityTypeAndEntityId(user.getId(), entityType, entityId)) {
            throw new BusinessException("Item already saved");
        }
        SavedItem item = new SavedItem();
        item.setUser(user);
        item.setEntityType(entityType);
        item.setEntityId(entityId);
        return savedItemRepository.save(item);
    }

    @Transactional
    public void unsave(User user, EntityType entityType, UUID entityId) {
        savedItemRepository.deleteByUserIdAndEntityTypeAndEntityId(user.getId(), entityType, entityId);
    }

    @Transactional(readOnly = true)
    public PaginatedResponse<SocialDtos.SavedItemResponse> listSaved(User user, EntityType entityType,
                                                                     Pageable pageable) {
        if (entityType == null) {
            throw new BusinessException("entityType is required");
        }
        Page<SavedItem> page = savedItemRepository.findByUserIdAndEntityType(user.getId(), entityType, pageable);
        return PaginatedResponse.from(page.map(SocialDtos.SavedItemResponse::from));
    }

    public boolean isSaved(User user, EntityType entityType, UUID entityId) {
        return savedItemRepository.existsByUserIdAndEntityTypeAndEntityId(user.getId(), entityType, entityId);
    }

    private void assertEntityExists(EntityType entityType, UUID entityId) {
        switch (entityType) {
            case PROPERTY -> {
                if (!propertyRepository.existsById(entityId)) {
                    throw new BusinessException("Property not found");
                }
            }
            case COMPANY -> {
                if (!companyRepository.existsById(entityId)) {
                    throw new BusinessException("Company not found");
                }
            }
            case TECHNICIAN, SERVICE_PROVIDER -> {
                // Technicians/service providers are modeled on companies in this version.
                if (!companyRepository.existsById(entityId)) {
                    throw new BusinessException("Provider not found");
                }
            }
            default -> throw new BusinessException("Unsupported entity type");
        }
    }
}
