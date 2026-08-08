# Delta Homes — 11 · Stage 10 · Search

> **Stage 10.** Unified search endpoint using SQL Server Full-Text Search.

**Status:** Parity (stub) · **Dependencies:** Stage 0 · **Effort:** S

---

## 1. Endpoints

| Method & Path | Auth | Response |
|---|---|---|
| `GET /api/v1/search` | Public | `Paginated<SearchResult>` |

---

## 2. Implementation

```java
@RestController
@RequestMapping("/api/v1/search")
@RequiredArgsConstructor
public class SearchController {
    
    private final SearchService searchService;
    
    @GetMapping
    public ResponseEntity<PageResponse<SearchResult>> search(
            @RequestParam String q,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(searchService.search(q, PageRequest.of(page, size)));
    }
}
```

---

## 3. Definition of Done

- [ ] Unified search across properties and companies
- [ ] Full-text search support