package com.antarang.cap.dto.request;

import com.antarang.cap.domain.enums.UserStatus;
import jakarta.validation.constraints.NotNull;

public record UpdateUserStatusRequest(
        @NotNull UserStatus status,
        String reason
) {
}
