# Delta Homes — 06 · Stage 5 · Saved Items

> **Stage 5.** Bookmark functionality for properties and companies.

**Status:** Parity · **Dependencies:** Stage 0, 1 · **Effort:** S

---

## 1. Endpoints

| Method & Path | Auth | Response |
|---|---|---|
| `GET /api/v1/saved-items` | Authenticated | `Paginated<SavedItemSummary>` |
| `POST /api/v1/saved-items` | Authenticated | `201 SavedItem` |
| `DELETE /api/v1/saved-items/{id}` | Authenticated | `204` |

---

## 2. Entity

```java
@Entity
@Table(name = "saved_items", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"user_id", "entity_type", "entity_id"})
})
public class SavedItem extends BaseEntity {
    
    @Column(name = "user_id", nullable = false)
    private UUID userId;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "entity_type", length = 50, nullable = false)
    private EntityType entityType;
    
    @Column(name = "entity_id", nullable = false)
    private UUID entityId;
}
```

---

## 3. Controller & Service

```java
@RestController
@RequestMapping("/api/v1/saved-items")
@PreAuthorize("isAuthenticated()")
@RequiredArgsConstructor
public class SavedItemController {
    
    private final SavedItemService savedItemService;
    
    @GetMapping
    public ResponseEntity<PageResponse<SavedItemSummary>> getSavedItems(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(savedItemService.getSavedItems(PageRequest.of(page, size)));
    }
    
    @PostMapping
    public ResponseEntity<SavedItem> saveItem(
            @RequestBody @Valid SaveItemRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(savedItemService.saveItem(request));
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> unsaveItem(@PathVariable UUID id) {
        savedItemService.unsaveItem(id);
        return ResponseEntity.noContent().build();
    }
}
```

---

## 4. Definition of Done

- [ ] Save/unsave items
- [ ] List saved items
- [ ] Duplicate save returns appropriate error