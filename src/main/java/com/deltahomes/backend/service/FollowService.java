package com.deltahomes.backend.service;

import com.deltahomes.backend.dto.social.SocialDtos;
import com.deltahomes.backend.entity.Follower;
import com.deltahomes.backend.entity.company.Company;
import com.deltahomes.backend.entity.user.User;
import com.deltahomes.backend.exception.BusinessException;
import com.deltahomes.backend.exception.ResourceNotFoundException;
import com.deltahomes.backend.repository.CompanyRepository;
import com.deltahomes.backend.repository.FollowerRepository;
import com.deltahomes.backend.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class FollowService {

    private final FollowerRepository followerRepository;
    private final CompanyRepository companyRepository;
    private final UserRepository userRepository;

    public FollowService(FollowerRepository followerRepository,
                         CompanyRepository companyRepository,
                         UserRepository userRepository) {
        this.followerRepository = followerRepository;
        this.companyRepository = companyRepository;
        this.userRepository = userRepository;
    }

    public User resolveUser(String identifier) {
        return userRepository.findByPhone(identifier)
                .or(() -> userRepository.findByEmail(identifier))
                .orElseThrow(() -> new BusinessException("User not found"));
    }

    @Transactional
    public Follower follow(User user, UUID companyId) {
        Company company = getCompany(companyId);
        if (followerRepository.existsByUserIdAndCompanyId(user.getId(), companyId)) {
            throw new BusinessException("Already following this company");
        }
        Follower follower = new Follower();
        follower.setUser(user);
        follower.setCompany(company);
        followerRepository.save(follower);
        company.setFollowersCount(company.getFollowersCount() + 1);
        companyRepository.save(company);
        return follower;
    }

    @Transactional
    public void unfollow(User user, UUID companyId) {
        if (!followerRepository.existsByUserIdAndCompanyId(user.getId(), companyId)) {
            throw new BusinessException("Not following this company");
        }
        followerRepository.deleteByUserIdAndCompanyId(user.getId(), companyId);
        Company company = getCompany(companyId);
        company.setFollowersCount(Math.max(0, company.getFollowersCount() - 1));
        companyRepository.save(company);
    }

    public boolean isFollowing(User user, UUID companyId) {
        return followerRepository.existsByUserIdAndCompanyId(user.getId(), companyId);
    }

    public long getFollowersCount(UUID companyId) {
        return followerRepository.countByCompanyId(companyId);
    }

    @Transactional(readOnly = true)
    public List<SocialDtos.CompanySummaryResponse> getFollowedCompanies(User user) {
        return followerRepository.findByUserId(user.getId()).stream()
                .map(Follower::getCompany)
                .map(SocialDtos.CompanySummaryResponse::from)
                .toList();
    }

    private Company getCompany(UUID companyId) {
        return companyRepository.findById(companyId)
                .orElseThrow(() -> new ResourceNotFoundException("Company", companyId));
    }
}
