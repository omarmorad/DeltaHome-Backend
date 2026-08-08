# Delta Homes — 05 · Stage 4 · Companies & Follow

> **Stage 4.** Company profiles and follow functionality.

**Status:** Parity · **Dependencies:** Stage 0, 1, 2 · **Effort:** L

---

## 1. Endpoints

| Method & Path | Auth | Response |
|---|---|---|
| `GET /api/v1/companies` | Public | `Paginated<CompanySummary>` |
| `GET /api/v1/companies/{id}` | Public | `Company` |
| `POST /api/v1/companies` | Authenticated | `Company` |
| `POST /api/v1/companies/{id}/follow` | Authenticated | `FollowResponse` |
| `DELETE /api/v1/companies/{id}/follow` | Authenticated | `200` |

---

## 2. Entities

```java
@Entity
@Table(name = "companies")
public class Company extends BaseEntity {
    
    @Column(name = "name_ar", length = 200, nullable = false)
    private String nameAr;
    
    @Column(name = "name_en", length = 200)
    private String nameEn;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "type", length = 50, nullable = false)
    private CompanyType type;
    
    @Column(name = "logo_url", length = 1000)
    private String logoUrl;
    
    @Column(name = "cover_url", length = 1000)
    private String coverUrl;
    
    @Column(name = "phone", length = 30, nullable = false)
    private String phone;
    
    @Column(name = "whatsapp", length = 30)
    private String whatsapp;
    
    @Column(name = "email", length = 256)
    private String email;
    
    @Column(name = "website", length = 500)
    private String website;
    
    @Column(name = "verified", nullable = false)
    private Boolean verified = false;
    
    @Column(name = "followers_count", nullable = false)
    private Integer followersCount = 0;
    
    @Column(name = "reputation_score", precision = 3, scale = 2)
    private BigDecimal reputationScore = BigDecimal.ZERO;
}

@Entity
@Table(name = "followers", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"user_id", "company_id"})
})
public class Follower extends BaseEntity {
    
    @Column(name = "user_id", nullable = false)
    private UUID userId;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id", nullable = false)
    private Company company;
}
```

---

## 3. Controller

```java
@RestController
@RequestMapping("/api/v1/companies")
@RequiredArgsConstructor
public class CompanyController {
    
    private final CompanyService companyService;
    
    @GetMapping
    public ResponseEntity<PageResponse<CompanySummary>> getCompanies(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) CompanyType type,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(companyService.searchCompanies(q, type, PageRequest.of(page, size)));
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<Company> getCompany(@PathVariable UUID id) {
        return ResponseEntity.ok(companyService.getCompany(id));
    }
    
    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Company> createCompany(
            @Valid @RequestBody CompanyRequest request,
            Principal principal) {
        return ResponseEntity.ok(companyService.createCompany(request, principal.getName()));
    }
    
    @PostMapping("/{id}/follow")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<FollowResponse> followCompany(
            @PathVariable UUID id, Principal principal) {
        return ResponseEntity.ok(companyService.followCompany(id, principal.getName()));
    }
    
    @DeleteMapping("/{id}/follow")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Void> unfollowCompany(
            @PathVariable UUID id, Principal principal) {
        companyService.unfollowCompany(id, principal.getName());
        return ResponseEntity.ok().build();
    }
}
```

---

## 4. Service

```java
@Service
@RequiredArgsConstructor
public class CompanyService {
    
    private final CompanyRepository companyRepository;
    private final FollowerRepository followerRepository;
    private final UserRepository userRepository;
    
    public PageResponse<CompanySummary> searchCompanies(String q, CompanyType type, Pageable pageable) {
        Page<Company> page = companyRepository.searchCompanies(q, type, pageable);
        return PageResponse.from(page.map(this::toSummary));
    }
    
    public Company getCompany(UUID id) {
        return companyRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Company", id));
    }
    
    @Transactional
    public Company createCompany(CompanyRequest request, String userIdentifier) {
        User owner = userRepository.findByPhoneOrEmail(userIdentifier)
            .orElseThrow(() -> new BusinessException("User not found"));
        
        Company company = new Company();
        company.setNameAr(request.nameAr());
        company.setNameEn(request.nameEn());
        company.setType(request.type());
        company.setPhone(request.phone());
        company.setEmail(request.email());
        // Set other fields...
        
        return companyRepository.save(company);
    }
    
    @Transactional
    public FollowResponse followCompany(UUID companyId, String userIdentifier) {
        User user = userRepository.findByPhoneOrEmail(userIdentifier)
            .orElseThrow(() -> new BusinessException("User not found"));
        
        Company company = getCompany(companyId);
        
        if (followerRepository.existsByUserIdAndCompanyId(user.getId(), companyId)) {
            return new FollowResponse(companyId, true, company.getFollowersCount());
        }
        
        Follower follower = new Follower();
        follower.setUserId(user.getId());
        follower.setCompany(company);
        followerRepository.save(follower);
        
        company.setFollowersCount(company.getFollowersCount() + 1);
        companyRepository.save(company);
        
        return new FollowResponse(companyId, true, company.getFollowersCount());
    }
    
    @Transactional
    public void unfollowCompany(UUID companyId, String userIdentifier) {
        User user = userRepository.findByPhoneOrEmail(userIdentifier)
            .orElseThrow(() -> new BusinessException("User not found"));
        
        followerRepository.deleteByUserIdAndCompanyId(user.getId(), companyId);
        
        companyRepository.findById(companyId).ifPresent(company -> {
            company.setFollowersCount(Math.max(0, company.getFollowersCount() - 1));
            companyRepository.save(company);
        });
    }
}
```

---

## 5. Definition of Done

- [ ] Company CRUD endpoints
- [ ] Follow/unfollow functionality
- [ ] Followers count updated correctly