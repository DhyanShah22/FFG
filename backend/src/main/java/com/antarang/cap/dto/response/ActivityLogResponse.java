package com.antarang.cap.dto.response;

import com.antarang.cap.domain.enums.ActivityType;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

public record ActivityLogResponse(
        UUID id,
        UUID userId,
        ActivityType activityType,
        String description,
        Map<String, Object> metadata,
        Instant createdAt
) {
}
