package com.antarang.cap.dto.request;

import com.antarang.cap.domain.enums.OtpChannel;
import com.antarang.cap.domain.enums.ProfileType;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;

/**
 * IF2AF0203 §III — Career Explorer may use mobile; others use email.
 */
public record ForgotPasswordRequest(
        @Email String email,
        String mobileNumber,
        ProfileType profileType,
        OtpChannel channel
) {
}
