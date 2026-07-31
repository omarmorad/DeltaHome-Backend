package com.deltahomes.backend.service;

import com.deltahomes.backend.entity.company.Company;
import com.deltahomes.backend.exception.ResourceNotFoundException;
import com.deltahomes.backend.repository.CompanyRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class CompanyService {

    private final CompanyRepository companyRepository;

    public CompanyService(CompanyRepository companyRepository) {
        this.companyRepository = companyRepository;
    }

    public Company getCompanyById(UUID id) {
        return companyRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Company", id));
    }

    public Page<Company> getVerifiedCompanies(Pageable pageable) {
        return companyRepository.findByVerifiedTrue(pageable);
    }
}
