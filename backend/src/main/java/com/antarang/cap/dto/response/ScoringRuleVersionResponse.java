package com.antarang.cap.dto.response;

import com.antarang.cap.domain.enums.VersionStatus;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

public record ScoringRuleVersionResponse(
        UUID id,
        UUID scoringRuleId,
        int versionNumber,
        Map<String, Object> ruleDefinition,
        VersionStatus status,
        Instant publishedAt,
        UUID publishedBy,
        String versionNotes,
        Instant createdAt
) {
}
