package com.antarang.cap.dto.request;

import com.antarang.cap.security.PasswordPolicy;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;
import java.util.UUID;

/**
 * Self-service profile update. All fields are optional; only non-null values are applied.
 */
public record UpdateProfileRequest(
        @Email String email,
        @Size(min = PasswordPolicy.SIGNUP_AND_RESET_MIN_LENGTH,
                message = "Password must be at least 13 characters") String password,
        String firstName,
        String lastName,
        String mobileNumber,
        LocalDate dateOfBirth,
        UUID preferredPlatformLanguageId,
        UUID preferredAssessmentLanguageId
) {
}
