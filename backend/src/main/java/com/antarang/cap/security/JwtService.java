package com.antarang.cap.security;

import com.antarang.cap.config.JwtProperties;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import java.util.Map;
import java.util.UUID;

@Service
public class JwtService {

    private final JwtProperties jwtProperties;
    private final SecretKey secretKey;

    public JwtService(JwtProperties jwtProperties) {
        this.jwtProperties = jwtProperties;
        this.secretKey = Keys.hmacShaKeyFor(jwtProperties.getSecret().getBytes(StandardCharsets.UTF_8));
    }

    public String generateAccessToken(UserPrincipal principal) {
        return buildToken(principal, jwtProperties.getAccessTokenExpirationMs(), Map.of("type", "access"));
    }

    public String generateRefreshToken(UserPrincipal principal) {
        return buildToken(principal, jwtProperties.getRefreshTokenExpirationMs(), Map.of("type", "refresh"));
    }

    public boolean isTokenValid(String token) {
        try {
            Jwts.parser()
                    .verifyWith(secretKey)
                    .build()
                    .parseSignedClaims(token);
            return true;
        } catch (Exception ex) {
            return false;
        }
    }

    public UUID extractUserId(String token) {
        return UUID.fromString(extractAllClaims(token).getSubject());
    }

    public UUID extractTenantId(String token) {
        String tenantId = extractAllClaims(token).get("tenantId", String.class);
        return tenantId != null ? UUID.fromString(tenantId) : null;
    }

    public String extractEmail(String token) {
        return extractAllClaims(token).get("email", String.class);
    }

    public String extractTokenType(String token) {
        return extractAllClaims(token).get("type", String.class);
    }

    public String extractJti(String token) {
        return extractAllClaims(token).getId();
    }

    public Instant extractExpiration(String token) {
        return extractAllClaims(token).getExpiration().toInstant();
    }

    private String buildToken(UserPrincipal principal, long expirationMs, Map<String, Object> extraClaims) {
        Instant now = Instant.now();
        return Jwts.builder()
                .claims(extraClaims)
                .id(UUID.randomUUID().toString())
                .subject(principal.getId().toString())
                .claim("email", principal.getEmail())
                .claim("tenantId", principal.getTenantId().toString())
                .claim("role", principal.getPrimaryRole())
                .claim("primaryOrgUnitId", principal.getPrimaryOrgUnitId() != null
                        ? principal.getPrimaryOrgUnitId().toString()
                        : null)
                .claim("scopeType", principal.getScopeType())
                .claim("scopeId", principal.getScopeId() != null ? principal.getScopeId().toString() : null)
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plusMillis(expirationMs)))
                .signWith(secretKey)
                .compact();
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
