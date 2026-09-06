package com.antarang.cap.dto.request;

import java.util.Map;

public record ReportSectionRequest(
        String sectionCode,
        String sectionTitle,
        int displayOrder,
        Map<String, Object> sectionConfig
) {
}
