package com.antarang.cap.dto.response;

import com.antarang.cap.domain.enums.AuditAction;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

public record AuditLogResponse(
        UUID id,
        String entityName,
        UUID entityId,
        AuditAction action,
        Map<String, Object> oldValue,
        Map<String, Object> newValue,
        UUID performedBy,
        Instant performedAt,
        String ipAddress
) {
}
