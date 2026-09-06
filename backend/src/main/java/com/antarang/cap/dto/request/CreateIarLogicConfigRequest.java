package com.antarang.cap.dto.request;

import jakarta.validation.constraints.NotBlank;

public record CreateIarLogicConfigRequest(
        @NotBlank String code,
        @NotBlank String name,
        String description
) {
}
