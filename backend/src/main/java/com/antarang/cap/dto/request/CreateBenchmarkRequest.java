package com.antarang.cap.dto.request;

import com.antarang.cap.domain.enums.BenchmarkLabel;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.util.UUID;

public record CreateBenchmarkRequest(
        @NotNull UUID assessmentId,
        @NotBlank String domainCode,
        UUID gradeConfigId,
        UUID ageGroupConfigId,
        @NotNull BigDecimal minScore,
        @NotNull BigDecimal maxScore,
        @NotNull BenchmarkLabel benchmarkLabel,
        String interpretation
) {
}
