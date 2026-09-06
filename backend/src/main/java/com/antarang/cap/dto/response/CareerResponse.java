package com.antarang.cap.dto.response;

import java.util.Map;
import java.util.UUID;

public record CareerResponse(
        UUID id,
        UUID clusterId,
        String code,
        String name,
        String description,
        String educationPathway,
        Map<String, Object> skillsRequired,
        boolean isActive
) {
}
