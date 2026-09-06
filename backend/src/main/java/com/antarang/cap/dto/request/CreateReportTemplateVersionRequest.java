package com.antarang.cap.dto.request;

import java.util.Map;

public record CreateReportTemplateVersionRequest(
        Map<String, Object> templateConfig,
        String versionNotes
) {
}
