package com.deltahomes.backend.config;

import com.deltahomes.backend.repository.UserRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final UserRepository userRepository;

    public JwtAuthenticationFilter(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String authHeader = request.getHeader("Authorization");

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            // Stub: JWT token validation and user lookup
            // TODO: Implement JWT parsing and validation
            // Example: String phone = jwtService.extractPhone(token);
            // User user = userRepository.findByPhone(phone).orElse(null);
            // if (user != null && jwtService.isTokenValid(token, user)) {
            //     UsernamePasswordAuthenticationToken auth =
            //         new UsernamePasswordAuthenticationToken(user, null, List.of());
            //     auth.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
            //     SecurityContextHolder.getContext().setAuthentication(auth);
            // }
        }

        filterChain.doFilter(request, response);
    }
}
