package com.antarang.cap.dto.request;

import com.antarang.cap.domain.enums.ProfileType;
import com.antarang.cap.domain.enums.ScopeType;
import com.antarang.cap.security.PasswordPolicy;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public record CreateUserRequest(
        @NotBlank @Email String email,
        @NotBlank @Size(min = PasswordPolicy.SIGNUP_AND_RESET_MIN_LENGTH,
                message = "Password must be at least 13 characters") String password,
        @NotNull ProfileType profileType,
        String firstName,
        String lastName,
        String mobileNumber,
        LocalDate dateOfBirth,
        UUID primaryOrgUnitId,
        UUID preferredPlatformLanguageId,
        UUID preferredAssessmentLanguageId,
        ScopeType scopeType,
        UUID scopeId,
        List<UUID> roleIds
) {
}
