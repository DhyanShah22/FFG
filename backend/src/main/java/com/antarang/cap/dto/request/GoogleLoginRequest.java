package com.antarang.cap.dto.request;

import com.antarang.cap.domain.enums.ProfileType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record GoogleLoginRequest(
        @NotBlank String idToken,
        @NotNull ProfileType profileType
) {
}
