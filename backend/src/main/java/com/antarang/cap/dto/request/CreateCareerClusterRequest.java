package com.antarang.cap.dto.request;

import jakarta.validation.constraints.NotBlank;

public record CreateCareerClusterRequest(
        @NotBlank String code,
        @NotBlank String name,
        String description
) {
}
