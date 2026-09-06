package com.antarang.cap.security;

import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Tracks revoked JWT identifiers (jti) until their natural expiry.
 *
 * <p>This is an in-memory, single-instance implementation intended for the MVP.
 * A distributed cache (e.g. Redis) would be required for multi-instance
 * deployments or to survive restarts.</p>
 */
@Service
public class TokenDenylistService {

    private final Map<String, Instant> revoked = new ConcurrentHashMap<>();

    public void revoke(String jti, Instant expiresAt) {
        if (jti == null || expiresAt == null) {
            return;
        }
        purgeExpired();
        revoked.put(jti, expiresAt);
    }

    public boolean isRevoked(String jti) {
        if (jti == null) {
            return false;
        }
        Instant expiresAt = revoked.get(jti);
        if (expiresAt == null) {
            return false;
        }
        if (expiresAt.isBefore(Instant.now())) {
            revoked.remove(jti);
            return false;
        }
        return true;
    }

    private void purgeExpired() {
        Instant now = Instant.now();
        revoked.entrySet().removeIf(entry -> entry.getValue().isBefore(now));
    }
}
