package com.antarang.cap.dto.request;

import com.antarang.cap.domain.enums.ReportTemplateType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CreateReportTemplateRequest(
        @NotBlank String code,
        @NotBlank String name,
        @NotNull ReportTemplateType templateType
) {
}
