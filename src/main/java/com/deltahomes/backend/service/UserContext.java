package com.deltahomes.backend.service;

import com.deltahomes.backend.entity.user.User;
import com.deltahomes.backend.exception.BusinessException;
import com.deltahomes.backend.repository.UserRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

/**
 * Resolves the authenticated {@link User} from a Spring Security principal,
 * by phone or email (matching how JWT subjects are issued).
 */
@Service
public class UserContext {

    private final UserRepository userRepository;

    public UserContext(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public User currentUser(UserDetails principal) {
        return userRepository.findByPhone(principal.getUsername())
                .or(() -> userRepository.findByEmail(principal.getUsername()))
                .orElseThrow(() -> new BusinessException("User not found"));
    }
}
