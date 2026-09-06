package com.antarang.cap.dto.response;

import com.antarang.cap.domain.enums.ProfileType;
import com.antarang.cap.domain.enums.UserStatus;

import java.util.List;
import java.util.UUID;

public record AuthUserSummary(
        UUID id,
        String firstName,
        String lastName,
        String email,
        ProfileType profileType,
        UserStatus status,
        List<String> roles,
        UUID tenantId,
        UUID primaryOrgUnitId,
        String preferredPlatformLanguageCode,
        String preferredAssessmentLanguageCode
) {
}
