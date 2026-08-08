# Delta Homes — 15 · Stage 14 · Social Login & Analytics

> **Stage 14.** Social OAuth and analytics integration.

**Status:** Aspirational · **Dependencies:** Stage 1 · **Effort:** L

---

## 1. Scope (Future)

- Google OAuth 2.0 integration
- Apple Sign-In integration
- PostHog analytics pipeline

---

## 2. Implementation Notes

```java
// Social Auth Service
@Service
public class SocialAuthService {
    
    public AuthResponse authenticateWithGoogle(String idToken) {
        // Verify Google ID token
        GoogleIdToken.Payload payload = verifyGoogleToken(idToken);
        
        // Find or create user
        User user = findOrCreateSocialUser(payload);
        
        // Generate JWT
        return jwtService.generateToken(user, false);
    }
}
```

---

## 3. Definition of Done

- [ ] Google OAuth integration
- [ ] Apple Sign-In
- [ ] Analytics event tracking