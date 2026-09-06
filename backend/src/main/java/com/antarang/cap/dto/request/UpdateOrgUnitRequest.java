package com.antarang.cap.dto.request;

import com.antarang.cap.domain.enums.OrgUnitType;

import java.util.Map;
import java.util.UUID;

/**
 * Org unit update. All fields are optional; only non-null values are applied.
 */
public record UpdateOrgUnitRequest(
        OrgUnitType orgUnitType,
        String code,
        String name,
        UUID parentOrgUnitId,
        String description,
        String address,
        Map<String, Object> metadata
) {
}
