package com.antarang.cap.dto.response;

import com.antarang.cap.domain.enums.BenchmarkLabel;

import java.math.BigDecimal;

public record DomainScoreResponse(
        String domainCode,
        BigDecimal rawScore,
        BigDecimal normalizedScore,
        BenchmarkLabel benchmarkLabel,
        String interpretation
) {
}
