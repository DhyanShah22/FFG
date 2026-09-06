package com.antarang.cap.dto.request;

import java.util.List;
import java.util.Map;

public record UpdateReportTemplateVersionRequest(
        Map<String, Object> templateConfig,
        List<ReportSectionRequest> sections
) {
}
