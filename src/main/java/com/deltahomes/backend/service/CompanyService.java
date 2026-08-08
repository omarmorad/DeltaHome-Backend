package com.deltahomes.backend.service;

import com.deltahomes.backend.dto.common.PaginatedResponse;
import com.deltahomes.backend.dto.company.CompanyDtos;
import com.deltahomes.backend.dto.summary.CompanySummary;
import com.deltahomes.backend.entity.company.Company;
import com.deltahomes.backend.entity.enums.CompanyType;
import com.deltahomes.backend.entity.enums.SubscriptionTier;
import com.deltahomes.backend.entity.user.User;
import com.deltahomes.backend.exception.ResourceNotFoundException;
import com.deltahomes.backend.repository.CompanyRepository;
import com.deltahomes.backend.util.PageUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.UUID;

@Service
public class CompanyService {

    private final CompanyRepository companyRepository;

    public CompanyService(CompanyRepository companyRepository) {
        this.companyRepository = companyRepository;
    }

    public PaginatedResponse<CompanySummary> index(String q, CompanyType type, Boolean verified,
                                                   Pageable pageable) {
        Page<CompanySummary> page = companyRepository.searchIndex(
                q == null ? "" : q.trim(),
                type == null ? null : type.name(),
                verified,
                PageUtils.normalizeSort(pageable));
        return PaginatedResponse.from(page);
    }

    public Company getCompanyById(UUID id) {
        return companyRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Company", id));
    }

    @Transactional
    public Company createCompany(User owner, CompanyDtos.CreateCompanyRequest request) {
        Company company = new Company();
        company.setOwner(owner);
        company.setName(request.name().trim());
        company.setType(request.type());
        company.setDescription(request.description());
        company.setLogoUrl(request.logoUrl());
        company.setCoverUrl(request.coverUrl());
        company.setPhone(request.phone());
        company.setWhatsapp(request.whatsapp());
        company.setEmail(request.email());
        company.setWebsite(request.website());
        company.setCoverageArea(request.coverageArea());
        // Server-controlled defaults — never trust client-supplied metrics.
        company.setVerified(false);
        company.setFollowersCount(0);
        company.setReputationScore(BigDecimal.ZERO);
        company.setPlan(SubscriptionTier.BASIC);
        return companyRepository.save(company);
    }

    public Page<Company> getVerifiedCompanies(Pageable pageable) {
        return companyRepository.findByVerifiedTrue(pageable);
    }
}
