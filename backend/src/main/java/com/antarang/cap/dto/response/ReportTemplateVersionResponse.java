package com.antarang.cap.dto.response;

import com.antarang.cap.domain.enums.VersionStatus;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

public record ReportTemplateVersionResponse(
        UUID id,
        UUID reportTemplateId,
        int versionNumber,
        Map<String, Object> templateConfig,
        VersionStatus status,
        Instant publishedAt,
        UUID publishedBy,
        String versionNotes
) {
}
