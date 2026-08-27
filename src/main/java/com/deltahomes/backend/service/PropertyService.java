package com.deltahomes.backend.service;

import com.deltahomes.backend.dto.common.PaginatedResponse;
import com.deltahomes.backend.dto.property.PropertyDtos.CreatePropertyRequest;
import com.deltahomes.backend.dto.property.PropertyDtos.PropertyDetailResponse;
import com.deltahomes.backend.dto.property.PropertyDtos.PropertyDetailResponse.OwnerInfo;
import com.deltahomes.backend.dto.property.PropertyDtos.UpdatePropertyRequest;
import com.deltahomes.backend.dto.summary.PropertySummary;
import com.deltahomes.backend.entity.enums.PropertyPurpose;
import com.deltahomes.backend.entity.enums.PropertyStatus;
import com.deltahomes.backend.entity.location.City;
import com.deltahomes.backend.entity.location.District;
import com.deltahomes.backend.entity.property.Property;
import com.deltahomes.backend.entity.user.User;
import com.deltahomes.backend.exception.BusinessException;
import com.deltahomes.backend.exception.ResourceNotFoundException;
import com.deltahomes.backend.repository.CityRepository;
import com.deltahomes.backend.repository.DistrictRepository;
import com.deltahomes.backend.repository.PropertyRepository;
import com.deltahomes.backend.util.PageUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.UUID;

@Service
public class PropertyService {

    private final PropertyRepository propertyRepository;
    private final CityRepository cityRepository;
    private final DistrictRepository districtRepository;
    private final I18nService i18n;

    public PropertyService(PropertyRepository propertyRepository,
                           CityRepository cityRepository,
                           DistrictRepository districtRepository,
                           I18nService i18n) {
        this.propertyRepository = propertyRepository;
        this.cityRepository = cityRepository;
        this.districtRepository = districtRepository;
        this.i18n = i18n;
    }

    @Transactional(readOnly = true)
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

    @Transactional(readOnly = true)
    public PropertyDetailResponse getPropertyDetail(UUID id) {
        Property p = getPropertyEntity(id);
        return toDetail(p);
    }

    @Transactional
    public PropertyDetailResponse createProperty(User owner, CreatePropertyRequest request) {
        City city = cityRepository.findById(request.cityId())
                .orElseThrow(() -> new ResourceNotFoundException("City", request.cityId()));
        District district = districtRepository.findById(request.districtId())
                .orElseThrow(() -> new ResourceNotFoundException("District", request.districtId()));

        Property property = new Property();
        applyRequest(property, request.title(), request.titleAr(), request.description(),
                request.descriptionAr(), request.price(),
                request.purpose(), request.category(), city, district, request.street(),
                request.latitude(), request.longitude(), request.readiness(),
                request.finishingLevel(), request.features());

        // Server-controlled fields — clients can never set these.
        property.setOwner(owner);
        property.setStatus(PropertyStatus.DRAFT);
        property.setIsFeatured(false);

        return toDetail(propertyRepository.save(property));
    }

    @Transactional
    public PropertyDetailResponse updateProperty(UUID id, User actor, UpdatePropertyRequest request) {
        Property existing = getOwnedProperty(id, actor);
        if (existing.getStatus() == PropertyStatus.ARCHIVED) {
            throw new BusinessException("Archived properties cannot be updated");
        }
        City city = request.cityId() == null ? existing.getCity()
                : cityRepository.findById(request.cityId())
                        .orElseThrow(() -> new ResourceNotFoundException("City", request.cityId()));
        District district = request.districtId() == null ? existing.getDistrict()
                : districtRepository.findById(request.districtId())
                        .orElseThrow(() -> new ResourceNotFoundException("District", request.districtId()));

        if (request.title() != null) existing.setTitle(request.title());
        if (request.titleAr() != null) existing.setTitleAr(request.titleAr());
        if (request.description() != null) existing.setDescription(request.description());
        if (request.descriptionAr() != null) existing.setDescriptionAr(request.descriptionAr());
        if (request.price() != null) existing.setPrice(request.price());
        if (request.purpose() != null) existing.setPurpose(request.purpose());
        if (request.category() != null) existing.setCategory(request.category());
        existing.setCity(city);
        existing.setDistrict(district);
        if (request.street() != null) existing.setStreet(request.street());
        if (request.latitude() != null) existing.setLatitude(request.latitude());
        if (request.longitude() != null) existing.setLongitude(request.longitude());
        if (request.readiness() != null) existing.setReadiness(request.readiness());
        if (request.finishingLevel() != null) existing.setFinishingLevel(request.finishingLevel());
        if (request.features() != null) existing.setFeatures(request.features());

        return toDetail(propertyRepository.save(existing));
    }

    @Transactional
    public void deleteProperty(UUID id, User actor) {
        Property existing = getOwnedProperty(id, actor);
        propertyRepository.delete(existing);
    }

    // ---------- Helpers ----------

    private Property getOwnedProperty(UUID id, User actor) {
        Property property = getPropertyEntity(id);
        boolean isOwner = property.getOwner().getId().equals(actor.getId());
        boolean isAdmin = "ADMIN".equals(actor.getRole().name());
        if (!isOwner && !isAdmin) {
            throw new AccessDeniedException("You do not own this property");
        }
        return property;
    }

    private Property getPropertyEntity(UUID id) {
        return propertyRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Property", id));
    }

    private static void applyRequest(Property property, String title, String titleAr,
                                     String description, String descriptionAr,
                                     BigDecimal price, PropertyPurpose purpose, String category,
                                     City city, District district, String street,
                                     BigDecimal latitude, BigDecimal longitude,
                                     com.deltahomes.backend.entity.enums.Readiness readiness,
                                     com.deltahomes.backend.entity.enums.FinishingLevel finishingLevel,
                                     String features) {
        property.setTitle(title.trim());
        property.setTitleAr(titleAr);
        property.setDescription(description);
        property.setDescriptionAr(descriptionAr);
        property.setPrice(price);
        property.setPurpose(purpose);
        property.setCategory(category.trim());
        property.setCity(city);
        property.setDistrict(district);
        property.setStreet(street);
        property.setLatitude(latitude);
        property.setLongitude(longitude);
        property.setReadiness(readiness);
        property.setFinishingLevel(finishingLevel);
        property.setFeatures(features);
    }

    /**
     * Locale-aware projection: `title`/`description` are resolved for the
     * request locale (Arabic content when serving `ar`, falling back to the
     * base column when no Arabic text was entered). Raw values of both
     * languages are always included so editors can see what is stored.
     */
    private PropertyDetailResponse toDetail(Property p) {
        boolean arabic = i18n.isArabic();
        String title = arabic && hasText(p.getTitleAr()) ? p.getTitleAr() : p.getTitle();
        String description = arabic && hasText(p.getDescriptionAr())
                ? p.getDescriptionAr() : p.getDescription();
        return new PropertyDetailResponse(
                p.getId(),
                title,
                p.getTitleAr(),
                description,
                p.getDescriptionAr(),
                p.getPrice(),
                p.getPurpose(),
                p.getCategory(),
                p.getCity() != null ? p.getCity().getId() : null,
                localized(p.getCity()),
                p.getDistrict() != null ? p.getDistrict().getId() : null,
                localizedDistrictName(p.getDistrict(), arabic),
                p.getStreet(),
                p.getLatitude(),
                p.getLongitude(),
                p.getStatus(),
                p.getHideReason(),
                p.getIsFeatured(),
                p.getReadiness(),
                p.getFinishingLevel(),
                p.getFeatures(),
                p.getOwner() != null
                        ? new OwnerInfo(p.getOwner().getId(), p.getOwner().getName())
                        : null,
                p.getCreatedAt(),
                p.getUpdatedAt()
        );
    }

    private static boolean hasText(String s) {
        return s != null && !s.isBlank();
    }

    private static String localized(City city) {
        return city != null ? city.getName() : null;
    }

    private static String localizedDistrictName(District district, boolean arabic) {
        if (district == null) {
            return null;
        }
        return arabic && hasText(district.getNameAr()) ? district.getNameAr() : district.getName();
    }

    private PropertySummary toSummary(Property p) {
        boolean arabic = i18n.isArabic();
        return new PropertySummary() {
            @Override public UUID getId() { return p.getId(); }
            @Override public String getTitle() {
                return arabic && hasText(p.getTitleAr()) ? p.getTitleAr() : p.getTitle();
            }
            @Override public String getDescription() {
                return arabic && hasText(p.getDescriptionAr()) ? p.getDescriptionAr() : p.getDescription();
            }
            @Override public BigDecimal getPrice() { return p.getPrice(); }
            @Override public String getPurpose() { return p.getPurpose() != null ? p.getPurpose().name() : null; }
            @Override public String getCategory() { return p.getCategory(); }
            @Override public String getStatus() { return p.getStatus() != null ? p.getStatus().name() : null; }
            @Override public String getCityName() {
                if (p.getCity() == null) return null;
                return arabic && hasText(p.getCity().getNameAr()) ? p.getCity().getNameAr() : p.getCity().getName();
            }
            @Override public String getDistrictName() {
                if (p.getDistrict() == null) return null;
                return arabic && hasText(p.getDistrict().getNameAr())
                        ? p.getDistrict().getNameAr() : p.getDistrict().getName();
            }
            @Override public String getStreet() { return p.getStreet(); }
            @Override public String getReadiness() { return p.getReadiness() != null ? p.getReadiness().name() : null; }
            @Override public String getFinishingLevel() { return p.getFinishingLevel() != null ? p.getFinishingLevel().name() : null; }
            @Override public Boolean getIsFeatured() { return p.getIsFeatured(); }
            @Override public String getFeatures() { return p.getFeatures(); }
            @Override public java.time.OffsetDateTime getCreatedAt() { return p.getCreatedAt(); }
        };
    }
}
