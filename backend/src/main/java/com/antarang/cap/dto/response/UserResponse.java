package com.antarang.cap.dto.response;

import com.antarang.cap.domain.enums.ProfileType;
import com.antarang.cap.domain.enums.UserStatus;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public record UserResponse(
        UUID id,
        UUID tenantId,
        String email,
        String firstName,
        String lastName,
        String mobileNumber,
        LocalDate dateOfBirth,
        ProfileType profileType,
        UserStatus status,
        UUID primaryOrgUnitId,
        UUID preferredPlatformLanguageId,
        UUID preferredAssessmentLanguageId,
        List<String> roles,
        String role
) {
}
