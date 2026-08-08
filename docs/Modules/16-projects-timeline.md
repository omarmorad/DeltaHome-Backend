# Delta Homes — 16 · Stage 15 · Projects & Timeline

> **Stage 15.** Construction projects and property timeline tracking.

**Status:** Aspirational · **Dependencies:** Stage 3 · **Effort:** L

---

## 1. Scope (Future)

- Major construction projects showcase
- Property construction timeline tracking
- Milestone progress visualization

---

## 2. Entities

```java
@Entity
@Table(name = "projects")
public class Project extends BaseEntity {
    
    @Column(name = "name_ar", length = 200, nullable = false)
    private String nameAr;
    
    @Column(name = "name_en", length = 200)
    private String nameEn;
    
    @Column(name = "description_ar", columnDefinition = "nvarchar(max)")
    private String descriptionAr;
    
    @Column(name = "description_en", columnDefinition = "nvarchar(max)")
    private String descriptionEn;
    
    @Column(name = "cover_image_url", length = 1000)
    private String coverImageUrl;
    
    @Column(name = "location", length = 300)
    private String location;
    
    @Column(name = "start_date")
    private OffsetDateTime startDate;
    
    @Column(name = "expected_end_date")
    private OffsetDateTime expectedEndDate;
    
    @Column(name = "completion_percentage", nullable = false)
    private Integer completionPercentage = 0;
    
    @OneToMany(mappedBy = "project", cascade = CascadeType.ALL)
    private List<ProjectMilestone> milestones = new ArrayList<>();
    
    @OneToMany(mappedBy = "project", cascade = CascadeType.ALL)
    private List<Property> properties = new ArrayList<>();
}

@Entity
@Table(name = "project_milestones")
public class ProjectMilestone extends BaseEntity {
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id", nullable = false)
    private Project project;
    
    @Column(name = "title_ar", length = 150, nullable = false)
    private String titleAr;
    
    @Column(name = "title_en", length = 150)
    private String titleEn;
    
    @Column(name = "description", columnDefinition = "nvarchar(max)")
    private String description;
    
    @Column(name = "order_index", nullable = false)
    private Integer orderIndex;
    
    @Column(name = "is_completed", nullable = false)
    private Boolean isCompleted = false;
    
    @Column(name = "completed_at")
    private OffsetDateTime completedAt;
}
```

---

## 3. Endpoints (Future)

| Method & Path | Response |
|---|---|
| `GET /api/v1/projects` | `Paginated<ProjectSummary>` |
| `GET /api/v1/projects/{id}` | `Project` with milestones |
| `GET /api/v1/projects/{id}/timeline` | `TimelineResponse` |

---

## 4. Definition of Done

- [ ] Project CRUD
- [ ] Milestone management
- [ ] Progress tracking