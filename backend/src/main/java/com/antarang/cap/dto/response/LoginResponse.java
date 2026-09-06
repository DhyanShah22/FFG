package com.antarang.cap.dto.response;

public record LoginResponse(
        String accessToken,
        String refreshToken,
        long expiresIn,
        AuthUserSummary user,
        // Legacy flat fields (aliases for existing clients)
        String role,
        java.util.UUID primaryOrgUnitId
) {
}
