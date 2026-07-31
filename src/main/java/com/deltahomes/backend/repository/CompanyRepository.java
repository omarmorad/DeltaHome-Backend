package com.deltahomes.backend.repository;

import com.deltahomes.backend.entity.company.Company;
import com.deltahomes.backend.entity.enums.CompanyType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CompanyRepository extends JpaRepository<Company, Long> {
    Page<Company> findByType(CompanyType type, Pageable pageable);
    Page<Company> findByVerifiedTrue(Pageable pageable);
}
