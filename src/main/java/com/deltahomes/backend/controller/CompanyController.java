package com.deltahomes.backend.controller;

import com.deltahomes.backend.entity.company.Company;
import com.deltahomes.backend.service.CompanyService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/companies")
public class CompanyController {

    private final CompanyService companyService;

    public CompanyController(CompanyService companyService) {
        this.companyService = companyService;
    }

    @GetMapping("/{id}")
    public ResponseEntity<Company> getCompany(@PathVariable Long id) {
        return ResponseEntity.ok(companyService.getCompanyById(id));
    }

    @PostMapping("/{id}/follow")
    public ResponseEntity<Void> followCompany(@PathVariable Long id) {
        // Stub: follow logic
        return ResponseEntity.ok().build();
    }
}
