package com.antarang.cap.dto.response;

import com.antarang.cap.domain.enums.LifecycleStatus;
import com.antarang.cap.domain.enums.ReportTemplateType;
import com.antarang.cap.domain.enums.VersionStatus;

import java.util.UUID;

public record ReportTemplateResponse(
        UUID id,
        UUID tenantId,
        String code,
        String name,
        ReportTemplateType templateType,
        LifecycleStatus status,
        UUID currentVersionId,
        VersionStatus latestVersionStatus,
        boolean isActive
) {
}
