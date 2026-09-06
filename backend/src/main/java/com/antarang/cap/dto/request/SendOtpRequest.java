package com.antarang.cap.dto.request;

import com.antarang.cap.domain.enums.OtpChannel;
import jakarta.validation.constraints.NotNull;

public record SendOtpRequest(
        @NotNull OtpChannel channel,
        String destination
) {
}
