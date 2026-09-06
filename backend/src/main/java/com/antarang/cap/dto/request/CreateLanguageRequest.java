package com.antarang.cap.dto.request;

import jakarta.validation.constraints.NotBlank;

public record CreateLanguageRequest(
        @NotBlank String code,
        @NotBlank String name,
        Boolean isActive
) {
}
