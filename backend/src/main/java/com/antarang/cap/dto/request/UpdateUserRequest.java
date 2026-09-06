package com.antarang.cap.dto.request;

import com.antarang.cap.domain.enums.ProfileType;
import com.antarang.cap.domain.enums.UserStatus;
import jakarta.validation.constraints.Email;

import java.time.LocalDate;
import java.util.UUID;

/**
 * Administrative user update. All fields are optional; only non-null values are applied.
 */
public record UpdateUserRequest(
        @Email String email,
        ProfileType profileType,
        UserStatus status,
        String firstName,
        String lastName,
        String mobileNumber,
        LocalDate dateOfBirth,
        UUID primaryOrgUnitId,
        UUID preferredPlatformLanguageId,
        UUID preferredAssessmentLanguageId
) {
}
