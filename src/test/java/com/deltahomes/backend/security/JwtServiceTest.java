package com.deltahomes.backend.security;

import com.deltahomes.backend.entity.enums.UserRole;
import com.deltahomes.backend.entity.user.User;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.userdetails.UserDetails;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class JwtServiceTest {

    private static final String SECRET = "dHlwZS5hY2Nlc3MtdG9rZW4uZXhhbXBsZS5jb20uZGVsdGFob21lcy5iYWNrZW5k";

    private static JwtService jwtService;
    private static com.deltahomes.backend.entity.user.User appUser;
    private static UserDetails userDetails;

    @BeforeAll
    static void setUp() {
        jwtService = new JwtService(SECRET, 3_600_000L, 604_800_000L);

        appUser = new com.deltahomes.backend.entity.user.User();
        try {
            var idField = com.deltahomes.backend.entity.base.BaseEntity.class
                    .getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(appUser, java.util.UUID.randomUUID());
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException(e);
        }
        appUser.setName("Test User");
        appUser.setPhone("01012345678");
        appUser.setPasswordHash("hash");
        appUser.setRole(UserRole.CUSTOMER);

        userDetails = org.springframework.security.core.userdetails.User.builder()
                .username("01012345678")
                .password("hash")
                .authorities("ROLE_CUSTOMER")
                .build();
    }

    @Test
    void accessTokenCarriesAccessTypeClaim() {
        String token = jwtService.generateAccessToken(appUser);
        assertTrue(jwtService.isTokenValid(token, userDetails));
        assertFalse(jwtService.isRefreshToken(token));
    }

    @Test
    void refreshTokenCannotBeUsedAsAccessToken() {
        String refresh = jwtService.generateRefreshToken(appUser);
        assertFalse(jwtService.isTokenValid(refresh, userDetails),
                "A refresh token must never authenticate API requests");
        assertTrue(jwtService.isRefreshToken(refresh));
    }

    @Test
    void eachRefreshTokenHasUniqueJtiForRotation() {
        String first = jwtService.generateRefreshToken(appUser);
        String second = jwtService.generateRefreshToken(appUser);
        assertNotNull(jwtService.extractJti(first));
        assertNotEquals(jwtService.extractJti(first), jwtService.extractJti(second),
                "Rotated refresh tokens must have distinct jtis");
    }

    @Test
    void expiredTokensAreRejected() {
        JwtService shortLived = new JwtService(SECRET, -1_000L, -1_000L);
        String token = shortLived.generateAccessToken(appUser);
        assertFalse(shortLived.isTokenValid(token, userDetails));
        assertFalse(shortLived.isRefreshToken(shortLived.generateRefreshToken(appUser)));
    }

    @Test
    void tamperedTokensAreRejected() {
        String token = jwtService.generateAccessToken(appUser);
        String tampered = token.substring(0, token.length() - 2) + "xx";
        assertFalse(jwtService.isTokenValid(tampered, userDetails));
    }

    @Test
    void subjectMatchesPrincipalUsername() {
        String token = jwtService.generateAccessToken(appUser);
        assertEquals("01012345678", jwtService.extractPhone(token));
    }
}
