# Delta Homes — 03 · Stage 2 · Lookups (Public Reference Data)

> **Stage 2.** The first **read-only, public, Parity** module. Cities, districts, services,
> features, and subscription plans exposed as five paged index endpoints with full-text search
> and optional equality filters. No auth, no writes, no business state.

**Status:** Parity · **Dependencies:** Stage 0 (foundation) · **Effort:** S

---

## 1. Scope

**In**
- Five public, paged, FTS-backed index endpoints: `/cities`, `/districts`, `/services`, `/features`, `/plans`
- Five JPA entities + their mappings
- Lookup service with FTS + filter pattern

**Out**
- Any write endpoint (read-only module)
- CMS/admin editing (stage 12)
- Subscription lifecycle (stage 13)

---

## 2. Endpoints

| Method & Path | Params | Response |
|---|---|---|
| `GET /api/v1/cities` | `q?`, paging | `Paginated<City>` |
| `GET /api/v1/districts` | `q?`, `cityId?`, paging | `Paginated<District>` |
| `GET /api/v1/services` | `q?`, `category?`, paging | `Paginated<Service>` |
| `GET /api/v1/features` | `q?`, paging | `Paginated<Feature>` |
| `GET /api/v1/plans` | `q?`, `tier?`, `isActive?`, paging | `Paginated<SubscriptionPlan>` |

---

## 3. Entities

### City Entity

```java
@Entity
@Table(name = "cities")
public class City extends BaseEntity {
    
    @Column(name = "name", length = 100, nullable = false)
    private String name;
    
    @Column(name = "name_ar", length = 100, nullable = false)
    private String nameAr;
    
    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;
}
```

### District Entity

```java
@Entity
@Table(name = "districts")
public class District extends BaseEntity {
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "city_id", nullable = false)
    private City city;
    
    @Column(name = "name", length = 100, nullable = false)
    private String name;
    
    @Column(name = "name_ar", length = 100, nullable = false)
    private String nameAr;
}
```

### Service Entity

```java
@Entity
@Table(name = "services")
public class Service extends BaseEntity {
    
    @Column(name = "name_ar", length = 150, nullable = false)
    private String nameAr;
    
    @Column(name = "name_en", length = 150)
    private String nameEn;
    
    @Column(name = "category", length = 50, nullable = false)
    private String category;
    
    @Column(name = "icon_url", length = 500)
    private String iconUrl;
}
```

### Feature Entity

```java
@Entity
@Table(name = "features")
public class Feature extends BaseEntity {
    
    @Column(name = "name_ar", length = 100, nullable = false)
    private String nameAr;
    
    @Column(name = "name_en", length = 100)
    private String nameEn;
    
    @Column(name = "icon", length = 50)
    private String icon;
}
```

### SubscriptionPlan Entity

```java
@Entity
@Table(name = "subscription_plans")
public class SubscriptionPlan extends BaseEntity {
    
    @Column(name = "name_ar", length = 100, nullable = false)
    private String nameAr;
    
    @Column(name = "name_en", length = 100)
    private String nameEn;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "tier", length = 50, nullable = false)
    private SubscriptionTier tier;
    
    @Column(name = "price", precision = 10, scale = 2, nullable = false)
    private BigDecimal price;
    
    @Column(name = "listing_quota", nullable = false)
    private Integer listingQuota;
    
    @Column(name = "broadcast_quota", nullable = false)
    private Integer broadcastQuota;
    
    @Column(name = "is_active", nullable = false)
    private Boolean isActive = true;
}
```

---

## 4. Controller

```java
@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class LookupController {
    
    private final LookupService lookupService;
    
    @GetMapping("/cities")
    public ResponseEntity<PageResponse<CitySummary>> getCities(
            @RequestParam(required = false) String q,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt,desc") String sort) {
        return ResponseEntity.ok(lookupService.searchCities(q, PageRequest.of(page, size, Sort.by(sort))));
    }
    
    @GetMapping("/districts")
    public ResponseEntity<PageResponse<DistrictSummary>> getDistricts(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) UUID cityId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt,desc") String sort) {
        return ResponseEntity.ok(lookupService.searchDistricts(q, cityId, PageRequest.of(page, size, Sort.by(sort))));
    }
    
    @GetMapping("/services")
    public ResponseEntity<PageResponse<ServiceSummary>> getServices(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) String category,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(lookupService.searchServices(q, category, PageRequest.of(page, size)));
    }
    
    @GetMapping("/features")
    public ResponseEntity<PageResponse<FeatureSummary>> getFeatures(
            @RequestParam(required = false) String q,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(lookupService.searchFeatures(q, PageRequest.of(page, size)));
    }
    
    @GetMapping("/plans")
    public ResponseEntity<PageResponse<PlanSummary>> getPlans(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) SubscriptionTier tier,
            @RequestParam(required = false) Boolean isActive,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(lookupService.searchPlans(q, tier, isActive, PageRequest.of(page, size)));
    }
}
```

---

## 5. Service

```java
@Service
@RequiredArgsConstructor
public class LookupService {
    
    private final CityRepository cityRepository;
    private final DistrictRepository districtRepository;
    private final ServiceRepository serviceRepository;
    private final FeatureRepository featureRepository;
    private final SubscriptionPlanRepository planRepository;
    
    public PageResponse<CitySummary> searchCities(String q, Pageable pageable) {
        Page<City> page = q != null && !q.isBlank()
            ? cityRepository.searchByFullText(q, pageable)
            : cityRepository.findAll(pageable);
        return PageResponse.from(page.map(this::toSummary));
    }
    
    // Similar methods for districts, services, features, plans
}
```

---

## 6. Repository

```java
public interface CityRepository extends JpaRepository<City, UUID> {
    
    @Query(value = "SELECT * FROM cities c WHERE " +
           "(:q = '' OR CONTAINS((c.name, c.name_ar), :query))",
           nativeQuery = true)
    Page<City> searchByFullText(@Param("query") String query, Pageable pageable);
}
```

---

## 7. Definition of Done

- [ ] All five endpoints return paginated data with FTS support
- [ ] Filter parameters work correctly
- [ ] DTO summaries never leak sensitive data
- [ ] Seed data loaded idempotently