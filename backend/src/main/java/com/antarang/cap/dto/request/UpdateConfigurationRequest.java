package com.antarang.cap.dto.request;

import java.util.Map;

/**
 * Partial configuration update. All fields are optional; only non-null values are applied.
 * {@code value} maps to the configuration label/value.
 */
public record UpdateConfigurationRequest(
        String value,
        Integer displayOrder,
        Map<String, Object> metadata,
        Boolean isActive
) {
}
