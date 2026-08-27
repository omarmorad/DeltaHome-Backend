package com.deltahomes.backend.controller;

import com.deltahomes.backend.dto.common.ApiResponse;
import com.deltahomes.backend.dto.summary.BroadcastSummary;
import com.deltahomes.backend.entity.enums.BroadcastType;
import com.deltahomes.backend.service.BroadcastService;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/broadcasts")
public class BroadcastController {

    private final BroadcastService broadcastService;

    public BroadcastController(BroadcastService broadcastService) {
        this.broadcastService = broadcastService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<BroadcastSummary>>> index(
            @RequestParam(defaultValue = "") String q,
            @RequestParam(required = false) UUID companyId,
            @RequestParam(required = false) BroadcastType type,
            @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.page(broadcastService.index(q, companyId, type, pageable)));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<Map<String, Object>>> createBroadcast(@RequestBody Map<String, Object> body) {
        // Stub: requires authenticated company user + persistence
        body.put("status", "stub - not persisted");
        return ResponseEntity.ok(ApiResponse.ok(body));
    }
}
