package com.antarang.cap.dto.response;

import com.antarang.cap.domain.enums.OtpChannel;
import com.antarang.cap.domain.enums.ProfileType;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

public record SignupSessionResponse(
        UUID sessionId,
        UUID tenantId,
        ProfileType profileType,
        boolean emailVerified,
        boolean mobileVerified,
        Instant expiresAt,
        Instant lastActivityAt,
        Map<String, Object> sessionData
) {
}
