# Delta Homes — 04 · Stage 3 · Properties (Listings & Filtered Search)

> **Stage 3.** The first public-facing read module. Users discover and filter published
> listings via SQL Server full-text search, anyone can view a single listing, and an
> authenticated user can create a new (draft) listing.

**Status:** Parity · **Dependencies:** Stage 0, 1, 2 · **Effort:** L

---

## 1. Scope

**In**
- `GET /api/v1/properties` — public index with FTS search + filters → `Paginated<PropertySummary>`
- `GET /api/v1/properties/{id}` — public, full property entity
- `POST /api/v1/properties` — authenticated, creates draft listing
- Entities: Property, PropertyImage, PropertyVideo

**Out**
- PUT/PATCH endpoints (not in parity)
- Admin moderation (stage 12)
- Property timeline (aspirational)

---

## 2. Endpoints

| Method & Path | Auth | Response |
|---|---|---|
| `GET /api/v1/properties` | Public | `Paginated<PropertySummary>` |
| `GET /api/v1/properties/{id}` | Public | `Property` |
| `POST /api/v1/properties` | Authenticated | `Property` (status = DRAFT) |

---

## 3. Enums

```java
public enum PropertyPurpose { SALE, RENT }

public enum PropertyStatus { 
    DRAFT, PENDING_REVIEW, PUBLISHED, 
    HIDDEN, SOLD, RENTED, ARCHIVED 
}

public enum FinishingLevel { UNFINISHED, SEMI_FINISHED, FINISHED, LUXURY }

public enum Readiness { READY, UNDER_CONSTRUCTION }
```

---

## 4. Entity

```java
@Entity
@Table(name = "properties")
@EntityListeners(AuditingEntityListener.class)
public class Property extends BaseEntity {
    
    @Column(name = "title_ar", length = 250, nullable = false)
    private String titleAr;
    
    @Column(name = "title_en", length = 250)
    private String titleEn;
    
    @Column(name = "description_ar", columnDefinition = "nvarchar(max)")
    private String descriptionAr;
    
    @Column(name = "description_en", columnDefinition = "nvarchar(max)")
    private String descriptionEn;
    
    @Column(name = "price", precision = 18, scale = 2, nullable = false)
    private BigDecimal price;
    
    @Column(name = "area_sqm", precision = 10, scale = 2, nullable = false)
    private BigDecimal areaSqm;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "purpose", length = 50, nullable = false)
    private PropertyPurpose purpose;
    
    @Column(name = "category", length = 50, nullable = false)
    private String category;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "status", length = 50, nullable = false)
    private PropertyStatus status = PropertyStatus.DRAFT;
    
    @Column(name = "bedrooms")
    private Integer bedrooms;
    
    @Column(name = "bathrooms")
    private Integer bathrooms;
    
    @Column(name = "floor_number")
    private Integer floorNumber;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "city_id")
    private City city;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "district_id")
    private District district;
    
    @Column(name = "street", length = 200)
    private String street;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "readiness", length = 50, nullable = false)
    private Readiness readiness;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "finishing_level", length = 50)
    private FinishingLevel finishingLevel;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_user_id", nullable = false)
    private User owner;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id")
    private Company company;
    
    @Column(name = "features", columnDefinition = "nvarchar(max)")
    private String features;
    
    @Column(name = "is_featured", nullable = false)
    private Boolean isFeatured = false;
}
```

---

## 5. Controller

```java
@RestController
@RequestMapping("/api/v1/properties")
@RequiredArgsConstructor
public class PropertyController {
    
    private final PropertyService propertyService;
    
    @GetMapping
    public ResponseEntity<PageResponse<PropertySummary>> getProperties(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) UUID cityId,
            @RequestParam(required = false) UUID districtId,
            @RequestParam(required = false) PropertyPurpose purpose,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt,desc") String sort) {
        
        return ResponseEntity.ok(propertyService.searchProperties(q, cityId, districtId, 
            purpose, minPrice, maxPrice, PageRequest.of(page, size, Sort.by(sort))));
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<Property> getProperty(@PathVariable UUID id) {
        return ResponseEntity.ok(propertyService.getProperty(id));
    }
    
    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Property> createProperty(
            @Valid @RequestBody PropertyRequest request,
            Principal principal) {
        return ResponseEntity.ok(propertyService.createProperty(request, principal.getName()));
    }
}
```

---

## 6. Service

```java
@Service
@RequiredArgsConstructor
public class PropertyService {
    
    private final PropertyRepository propertyRepository;
    private final UserRepository userRepository;
    private final SortNormalizer sortNormalizer;
    
    public PageResponse<PropertySummary> searchProperties(String q, UUID cityId, 
            UUID districtId, PropertyPurpose purpose, BigDecimal minPrice, 
            BigDecimal maxPrice, Pageable pageable) {
        
        Sort normalized = sortNormalizer.normalize(pageable.getSort(), 
            Set.of("created_at", "price", "title_ar"));
        
        Page<Property> page = propertyRepository.searchProperties(
            q, cityId, districtId, purpose, minPrice, maxPrice, 
            PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), normalized));
        
        return PageResponse.from(page.map(this::toSummary));
    }
    
    public Property getProperty(UUID id) {
        return propertyRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Property", id));
    }
    
    @Transactional
    public Property createProperty(PropertyRequest request, String userIdentifier) {
        User owner = userRepository.findByPhoneOrEmail(userIdentifier)
            .orElseThrow(() -> new BusinessException("User not found"));
        
        Property property = new Property();
        property.setTitleAr(request.titleAr());
        property.setTitleEn(request.titleEn());
        property.setDescriptionAr(request.descriptionAr());
        property.setDescriptionEn(request.descriptionEn());
        property.setPrice(request.price());
        property.setAreaSqm(request.areaSqm());
        property.setPurpose(request.purpose());
        property.setCategory(request.category());
        property.setStatus(PropertyStatus.DRAFT); // Force draft
        property.setOwner(owner);
        // Set other fields...
        
        return propertyRepository.save(property);
    }
    
    private PropertySummary toSummary(Property p) {
        return new PropertySummary(
            p.getId(),
            p.getTitleAr(),
            p.getDescriptionAr(),
            p.getPrice(),
            p.getPurpose(),
            p.getCategory(),
            p.getStatus(),
            p.getCity() != null ? p.getCity().getNameAr() : null,
            p.getDistrict() != null ? p.getDistrict().getNameAr() : null,
            p.getStreet(),
            p.getReadiness(),
            p.getFinishingLevel(),
            p.getIsFeatured(),
            p.getFeatures(),
            p.getCreatedAt()
        );
    }
}
```

---

## 7. Repository

```java
public interface PropertyRepository extends JpaRepository<Property, UUID> {
    
    @Query(value = "SELECT * FROM properties p WHERE p.status = 'PUBLISHED' AND " +
           "(:q IS NULL OR :q = '' OR CONTAINS((p.title_ar, p.title_en, p.description_ar, p.description_en), :q)) " +
           "AND (:cityId IS NULL OR p.city_id = :cityId) " +
           "AND (:districtId IS NULL OR p.district_id = :districtId) " +
           "AND (:purpose IS NULL OR p.purpose = :purpose) " +
           "AND (:minPrice IS NULL OR p.price >= :minPrice) " +
           "AND (:maxPrice IS NULL OR p.price <= :maxPrice)",
           countQuery = "SELECT COUNT(*) FROM properties p WHERE p.status = 'PUBLISHED' AND " +
           "(:q IS NULL OR :q = '' OR CONTAINS((p.title_ar, p.title_en, p.description_ar, p.description_en), :q))",
           nativeQuery = true)
    Page<Property> searchProperties(@Param("q") String q, @Param("cityId") UUID cityId,
            @Param("districtId") UUID districtId, @Param("purpose") String purpose,
            @Param("minPrice") BigDecimal minPrice, @Param("maxPrice") BigDecimal maxPrice,
            Pageable pageable);
}
```

---

## 8. Definition of Done

- [ ] GET /properties returns paginated results with FTS search
- [ ] Filters work correctly (cityId, districtId, purpose, price range)
- [ ] Status forced to PUBLISHED in public listings
- [ ] POST creates draft property
- [ ] Full-text indexes created