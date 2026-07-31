package com.deltahomes.backend.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/search")
public class SearchController {

    @GetMapping
    public ResponseEntity<Map<String, Object>> universalSearch(@RequestParam String q) {
        // Stub: Meilisearch/Typesense powered search
        // Searches properties, companies, and services simultaneously
        return ResponseEntity.ok(Map.of(
            "query", q,
            "results", "Meilisearch integration pending"
        ));
    }
}
