package com.deltahomes.backend.controller;

import com.deltahomes.backend.dto.common.PaginatedResponse;
import com.deltahomes.backend.dto.summary.BroadcastSummary;
import com.deltahomes.backend.entity.enums.BroadcastType;
import com.deltahomes.backend.entity.marketing.Broadcast;
import com.deltahomes.backend.service.BroadcastService;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/broadcasts")
public class BroadcastController {

    private final BroadcastService broadcastService;

    public BroadcastController(BroadcastService broadcastService) {
        this.broadcastService = broadcastService;
    }

    @GetMapping
    public ResponseEntity<PaginatedResponse<BroadcastSummary>> index(
            @RequestParam(defaultValue = "") String q,
            @RequestParam(required = false) UUID companyId,
            @RequestParam(required = false) BroadcastType type,
            @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(broadcastService.index(q, companyId, type, pageable));
    }

    @PostMapping
    public ResponseEntity<Broadcast> createBroadcast(@RequestBody Broadcast broadcast) {
        // Stub: requires authenticated company user
        return ResponseEntity.ok(broadcast);
    }
}
