package com.antarang.cap.dto.request;

import com.antarang.cap.domain.enums.ProfileType;
import com.antarang.cap.dto.request.signup.RegisterSignupDetails;
import com.antarang.cap.security.PasswordPolicy;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;
import java.util.UUID;

/**
 * IF2AF0203 sign-up payload. Common fields apply to all profile types;
 * role-specific fields live under {@link RegisterSignupDetails}.
 */
public record RegisterRequest(
        UUID tenantId,
        String tenantCode,
        ProfileType profileType,
        /** Required for Counsellor, Administrator, Data Analyst; required for Explorer when email channel selected. */
        @Email String email,
        /** Career Explorer — optional username sign-up path (§II.5.B). */
        String username,
        @NotBlank @Size(min = PasswordPolicy.SIGNUP_AND_RESET_MIN_LENGTH,
                message = "Password must be at least 13 characters") String password,
        /** Required — §II.2 all profiles. */
        @NotBlank String firstName,
        /** Optional — §II.2 all profiles. */
        String middleName,
        /** Optional — §II.2 all profiles. */
        String lastName,
        /** Required — §II.2 all profiles. */
        LocalDate dateOfBirth,
        /** Required — configuration id (GENDER group). */
        UUID genderConfigId,
        /** Required — §II.2 all profiles. */
        String country,
        /** Required — §II.2 all profiles (state/region name). */
        String state,
        /** Required — §II.2 all profiles. */
        String howDidYouFindOut,
        /** Required for Explorer when MOBILE or EMAIL_AND_MOBILE channel selected. */
        String mobileNumber,
        /** Required — consent must be given before account is created (§II.3). */
        @NotNull RegisterConsentRequest consent,
        RegisterSignupDetails signupDetails,
        UUID primaryOrgUnitId,
        UUID preferredPlatformLanguageId,
        UUID preferredAssessmentLanguageId
) {
}
