package com.antarang.cap.dto.response;

import java.util.Map;
import java.util.UUID;

public record ReportSectionResponse(
        UUID id,
        String sectionCode,
        String sectionTitle,
        int displayOrder,
        Map<String, Object> sectionConfig
) {
}
