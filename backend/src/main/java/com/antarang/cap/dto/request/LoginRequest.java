package com.antarang.cap.dto.request;

import com.antarang.cap.domain.enums.ProfileType;

/**
 * Accepts FFG {@code loginId} and/or legacy {@code email}.
 */
public record LoginRequest(
        String loginId,
        String email,
        String password,
        ProfileType profileType
) {
    public String resolvedLoginId() {
        if (loginId != null && !loginId.isBlank()) {
            return loginId.trim();
        }
        return email != null ? email.trim() : null;
    }
}
