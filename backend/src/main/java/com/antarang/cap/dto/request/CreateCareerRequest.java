package com.antarang.cap.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.Map;
import java.util.UUID;

public record CreateCareerRequest(
        @NotNull UUID clusterId,
        @NotBlank String code,
        @NotBlank String name,
        String description,
        String educationPathway,
        Map<String, Object> skillsRequired
) {
}
