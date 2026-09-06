package com.antarang.cap.dto.response;

import com.antarang.cap.domain.enums.VersionStatus;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

public record IarLogicVersionResponse(
        UUID id,
        UUID iarLogicConfigId,
        int versionNumber,
        Map<String, Object> logicDefinition,
        VersionStatus status,
        Instant publishedAt,
        UUID publishedBy,
        String versionNotes,
        Instant createdAt
) {
}
