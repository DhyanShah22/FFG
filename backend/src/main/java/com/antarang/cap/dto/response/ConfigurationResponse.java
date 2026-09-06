package com.antarang.cap.dto.response;

import java.util.Map;
import java.util.UUID;

public record ConfigurationResponse(
        UUID id,
        UUID configurationGroupId,
        String code,
        String value,
        int displayOrder,
        Map<String, Object> metadata,
        boolean isActive
) {
}
