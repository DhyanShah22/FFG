package com.antarang.cap.dto.response;

import java.util.UUID;

public record ConfigurationGroupResponse(
        UUID id,
        UUID tenantId,
        String code,
        String name,
        String description,
        boolean systemDefined
) {
}
