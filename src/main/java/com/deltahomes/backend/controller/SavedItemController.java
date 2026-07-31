package com.deltahomes.backend.controller;

import com.deltahomes.backend.dto.social.SocialDtos;
import com.deltahomes.backend.entity.enums.EntityType;
import com.deltahomes.backend.entity.user.User;
import com.deltahomes.backend.exception.BusinessException;
import com.deltahomes.backend.repository.UserRepository;
import com.deltahomes.backend.service.SavedItemService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/saved-items")
public class SavedItemController {

    private final SavedItemService savedItemService;
    private final UserRepository userRepository;

    public SavedItemController(SavedItemService savedItemService, UserRepository userRepository) {
        this.savedItemService = savedItemService;
        this.userRepository = userRepository;
    }

    @PostMapping
    public ResponseEntity<SocialDtos.SavedItemResponse> save(@AuthenticationPrincipal UserDetails principal,
                                                             @RequestBody Map<String, Object> body) {
        EntityType entityType = parseEntityType(body.get("entityType"));
        UUID entityId = UUID.fromString(body.get("entityId").toString());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(SocialDtos.SavedItemResponse.from(
                        savedItemService.save(currentUser(principal), entityType, entityId)));
    }

    @DeleteMapping("/{entityType}/{entityId}")
    public ResponseEntity<Void> unsave(@AuthenticationPrincipal UserDetails principal,
                                       @PathVariable EntityType entityType,
                                       @PathVariable UUID entityId) {
        savedItemService.unsave(currentUser(principal), entityType, entityId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    public ResponseEntity<List<SocialDtos.SavedItemResponse>> listSaved(
            @AuthenticationPrincipal UserDetails principal,
            @RequestParam(required = false) EntityType entityType) {
        return ResponseEntity.ok(savedItemService.listSaved(currentUser(principal), entityType));
    }

    private User currentUser(UserDetails principal) {
        return userRepository.findByPhone(principal.getUsername())
                .or(() -> userRepository.findByEmail(principal.getUsername()))
                .orElseThrow(() -> new BusinessException("User not found"));
    }

    private static EntityType parseEntityType(Object value) {
        try {
            return EntityType.valueOf(value.toString().toUpperCase());
        } catch (Exception e) {
            throw new BusinessException("Invalid entityType");
        }
    }
}
