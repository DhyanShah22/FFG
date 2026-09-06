package com.antarang.cap.common;

import java.time.Instant;

public record ErrorDetail(
        String code,
        Object details
) {
}
