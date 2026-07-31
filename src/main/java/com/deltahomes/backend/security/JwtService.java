package com.deltahomes.backend.security;

import com.deltahomes.backend.entity.user.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.function.Function;

/**
 * Issues and validates stateless JWT access + refresh tokens.
 * Tokens carry a "type" claim ("access" | "refresh") so the two cannot be used interchangeably.
 */
@Service
public class JwtService {

    private static final String CLAIM_TYPE = "type";
    private static final String TYPE_ACCESS = "access";
    private static final String TYPE_REFRESH = "refresh";

    private final SecretKey signingKey;
    private final long accessExpirationMs;
    private final long refreshExpirationMs;

    public JwtService(
            @Value("${jwt.secret}") String secret,
            @Value("${jwt.expiration-ms:86400000}") long accessExpirationMs,
            @Value("${jwt.refresh-expiration-ms:604800000}") long refreshExpirationMs) {
        this.signingKey = Keys.hmacShaKeyFor(decodeSecret(secret));
        this.accessExpirationMs = accessExpirationMs;
        this.refreshExpirationMs = refreshExpirationMs;
    }

    private static byte[] decodeSecret(String secret) {
        try {
            return Decoders.BASE64.decode(secret);
        } catch (IllegalArgumentException e) {
            // Fall back to raw UTF-8 bytes when the secret is not base64-encoded.
            return secret.getBytes(StandardCharsets.UTF_8);
        }
    }

    public String generateAccessToken(User user) {
        return generateToken(user, TYPE_ACCESS, accessExpirationMs);
    }

    public String generateRefreshToken(User user) {
        return generateToken(user, TYPE_REFRESH, refreshExpirationMs);
    }

    private String generateToken(User user, String type, long expirationMs) {
        String subject = user.getPhone() != null ? user.getPhone() : user.getEmail();
        return Jwts.builder()
                .subject(subject)
                .claim("userId", user.getId())
                .claim("role", user.getRole().name())
                .claim(CLAIM_TYPE, type)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + expirationMs))
                .signWith(signingKey)
                .compact();
    }

    public long getAccessTokenExpirationSeconds() {
        return accessExpirationMs / 1000;
    }

    public String extractPhone(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    private <T> T extractClaim(String token, Function<Claims, T> resolver) {
        return resolver.apply(extractAllClaims(token));
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(signingKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    /** Validates that the token is a non-expired access token issued to the given user. */
    public boolean isTokenValid(String token, UserDetails userDetails) {
        try {
            Claims claims = extractAllClaims(token);
            return TYPE_ACCESS.equals(claims.get(CLAIM_TYPE, String.class))
                    && userDetails.getUsername().equals(claims.getSubject())
                    && claims.getExpiration().after(new Date());
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    /** Validates that the token is a non-expired refresh token. */
    public boolean isRefreshToken(String token) {
        try {
            Claims claims = extractAllClaims(token);
            return TYPE_REFRESH.equals(claims.get(CLAIM_TYPE, String.class))
                    && claims.getExpiration().after(new Date());
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }
}
