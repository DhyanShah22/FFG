package com.antarang.cap.dto.request;

import com.antarang.cap.domain.enums.OtpChannel;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

public record VerifyOtpRequest(
        @NotNull OtpChannel channel,
        @NotBlank @Pattern(regexp = "\\d{4}", message = "OTP must be 4 digits") String otp
) {
}
