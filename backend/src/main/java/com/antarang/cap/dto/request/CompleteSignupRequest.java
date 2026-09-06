package com.antarang.cap.dto.request;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

/**
 * Finalizes sign-up after OTP verification using server-side session data (IF2AF0203).
 */
public record CompleteSignupRequest(
        @NotNull UUID signupSessionId
) {
}
