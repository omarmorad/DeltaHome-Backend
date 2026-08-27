package com.deltahomes.backend.controller;

import com.deltahomes.backend.dto.common.ApiResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/search")
public class SearchController {

    @GetMapping
    public ResponseEntity<ApiResponse<Map<String, Object>>> universalSearch(@RequestParam String q) {
        // Stub: Meilisearch/Typesense powered search
        // Searches properties, companies, and services simultaneously
        return ResponseEntity.ok(ApiResponse.ok(Map.of(
                "query", q,
                "results", "Meilisearch integration pending"
        )));
    }
}
