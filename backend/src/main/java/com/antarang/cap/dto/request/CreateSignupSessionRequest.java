package com.antarang.cap.dto.request;

import com.antarang.cap.domain.enums.ProfileType;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record CreateSignupSessionRequest(
        UUID tenantId,
        String tenantCode,
        @NotNull ProfileType profileType
) {
}
