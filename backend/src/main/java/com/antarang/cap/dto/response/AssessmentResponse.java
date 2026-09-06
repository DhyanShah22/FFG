package com.antarang.cap.dto.response;

import com.antarang.cap.domain.enums.AssessmentType;

import java.util.UUID;

public record AssessmentResponse(
        UUID id,
        UUID tenantId,
        AssessmentType code,
        String name,
        String description,
        boolean isActive
) {
}
