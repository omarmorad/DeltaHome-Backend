package com.deltahomes.backend.controller;

import com.deltahomes.backend.entity.marketing.Broadcast;
import com.deltahomes.backend.service.BroadcastService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/broadcasts")
public class BroadcastController {

    private final BroadcastService broadcastService;

    public BroadcastController(BroadcastService broadcastService) {
        this.broadcastService = broadcastService;
    }

    @PostMapping
    public ResponseEntity<Broadcast> createBroadcast(@RequestBody Broadcast broadcast) {
        // Stub: requires authenticated company user
        return ResponseEntity.ok(broadcast);
    }
}
