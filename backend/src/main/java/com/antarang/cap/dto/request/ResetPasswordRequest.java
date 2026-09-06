package com.antarang.cap.dto.request;

import com.antarang.cap.security.PasswordPolicy;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public record ResetPasswordRequest(
        @NotNull UUID challengeId,
        @NotBlank String otp,
        @NotBlank @Size(min = PasswordPolicy.SIGNUP_AND_RESET_MIN_LENGTH,
                message = "Password must be at least 13 characters") String newPassword
) {
}
