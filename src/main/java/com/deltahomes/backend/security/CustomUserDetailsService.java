package com.deltahomes.backend.security;

import com.deltahomes.backend.entity.enums.UserStatus;
import com.deltahomes.backend.entity.user.User;
import com.deltahomes.backend.repository.UserRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

/**
 * Phone number is the primary login identifier, so the username used throughout
 * Spring Security is the user's phone number.
 */
@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    public CustomUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String phone) throws UsernameNotFoundException {
        User user = userRepository.findByPhone(phone)
                .orElseThrow(() -> new UsernameNotFoundException("No user found with phone: " + phone));

        return org.springframework.security.core.userdetails.User.builder()
                .username(user.getPhone())
                .password(user.getPasswordHash())
                .authorities("ROLE_" + user.getRole().name())
                .disabled(user.getStatus() != UserStatus.ACTIVE)
                .build();
    }
}
