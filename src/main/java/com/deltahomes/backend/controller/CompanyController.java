package com.deltahomes.backend.controller;

import com.deltahomes.backend.dto.common.PaginatedResponse;
import com.deltahomes.backend.dto.company.CompanyDtos;
import com.deltahomes.backend.dto.social.SocialDtos;
import com.deltahomes.backend.dto.summary.CompanySummary;
import com.deltahomes.backend.entity.Follower;
import com.deltahomes.backend.entity.company.Company;
import com.deltahomes.backend.entity.enums.CompanyType;
import com.deltahomes.backend.entity.user.User;
import com.deltahomes.backend.exception.BusinessException;
import com.deltahomes.backend.repository.UserRepository;
import com.deltahomes.backend.service.CompanyService;
import com.deltahomes.backend.service.FollowService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/companies")
public class CompanyController {

    private final CompanyService companyService;
    private final FollowService followService;
    private final UserRepository userRepository;

    public CompanyController(CompanyService companyService,
                             FollowService followService,
                             UserRepository userRepository) {
        this.companyService = companyService;
        this.followService = followService;
        this.userRepository = userRepository;
    }

    @GetMapping
    public ResponseEntity<PaginatedResponse<CompanySummary>> index(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) CompanyType type,
            Pageable pageable) {
        return ResponseEntity.ok(companyService.index(q, type, true, pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Company> getCompany(@PathVariable UUID id) {
        return ResponseEntity.ok(companyService.getCompanyById(id));
    }

    @GetMapping("/followed")
    public ResponseEntity<List<SocialDtos.CompanySummaryResponse>> getFollowedCompanies(
            @AuthenticationPrincipal UserDetails principal) {
        return ResponseEntity.ok(followService.getFollowedCompanies(currentUser(principal)));
    }

    @PostMapping
    public ResponseEntity<Company> createCompany(@AuthenticationPrincipal UserDetails principal,
                                                 @Valid @RequestBody CompanyDtos.CreateCompanyRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(companyService.createCompany(currentUser(principal), request));
    }

    @PostMapping("/{id}/follow")
    public ResponseEntity<SocialDtos.FollowResponse> followCompany(
            @AuthenticationPrincipal UserDetails principal,
            @PathVariable UUID id) {
        Follower follower = followService.follow(currentUser(principal), id);
        return ResponseEntity.ok(new SocialDtos.FollowResponse(
                id, follower.getCompany().getName(), true));
    }

    @DeleteMapping("/{id}/follow")
    public ResponseEntity<Void> unfollowCompany(@AuthenticationPrincipal UserDetails principal,
                                                @PathVariable UUID id) {
        followService.unfollow(currentUser(principal), id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/is-following")
    public ResponseEntity<Map<String, Boolean>> isFollowing(@AuthenticationPrincipal UserDetails principal,
                                                            @PathVariable UUID id) {
        return ResponseEntity.ok(Map.of("following",
                followService.isFollowing(currentUser(principal), id)));
    }

    private User currentUser(UserDetails principal) {
        return userRepository.findByPhone(principal.getUsername())
                .or(() -> userRepository.findByEmail(principal.getUsername()))
                .orElseThrow(() -> new BusinessException("User not found"));
    }
}
