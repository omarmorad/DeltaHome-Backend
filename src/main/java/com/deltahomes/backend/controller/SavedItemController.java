package com.deltahomes.backend.controller;

import com.deltahomes.backend.dto.common.ApiResponse;
import com.deltahomes.backend.dto.social.SocialDtos;
import com.deltahomes.backend.entity.enums.EntityType;
import com.deltahomes.backend.entity.user.User;
import com.deltahomes.backend.exception.BusinessException;
import com.deltahomes.backend.service.SavedItemService;
import com.deltahomes.backend.service.UserContext;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/saved-items")
public class SavedItemController {

    private final SavedItemService savedItemService;
    private final UserContext userContext;

    public SavedItemController(SavedItemService savedItemService, UserContext userContext) {
        this.savedItemService = savedItemService;
        this.userContext = userContext;
    }

    @PostMapping
    public ResponseEntity<ApiResponse<SocialDtos.SavedItemResponse>> save(@AuthenticationPrincipal UserDetails principal,
                                                                          @RequestBody Map<String, Object> body) {
        EntityType entityType = parseEntityType(body.get("entityType"));
        UUID entityId = UUID.fromString(body.get("entityId").toString());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok(SocialDtos.SavedItemResponse.from(
                        savedItemService.save(currentUser(principal), entityType, entityId))));
    }

    @DeleteMapping("/{entityType}/{entityId}")
    public ResponseEntity<Void> unsave(@AuthenticationPrincipal UserDetails principal,
                                       @PathVariable EntityType entityType,
                                       @PathVariable UUID entityId) {
        savedItemService.unsave(currentUser(principal), entityType, entityId);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteSaved(@AuthenticationPrincipal UserDetails principal,
                                            @PathVariable UUID id) {
        savedItemService.deleteById(currentUser(principal), id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    public ResponseEntity<ApiResponse<java.util.List<SocialDtos.SavedItemResponse>>> listSaved(
            @AuthenticationPrincipal UserDetails principal,
            @RequestParam(required = false) EntityType entityType,
            @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.page(
                savedItemService.listSaved(currentUser(principal), entityType, pageable)));
    }

    private User currentUser(UserDetails principal) {
        return userContext.currentUser(principal);
    }

    private static EntityType parseEntityType(Object value) {
        try {
            return EntityType.valueOf(value.toString().toUpperCase());
        } catch (Exception e) {
            throw new BusinessException("Invalid entityType");
        }
    }
}
