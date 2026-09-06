package com.antarang.cap.dto.response;

import com.antarang.cap.domain.enums.OrgUnitType;

import java.util.List;
import java.util.Map;
import java.util.UUID;

public record OrgUnitResponse(
        UUID id,
        UUID tenantId,
        UUID parentOrgUnitId,
        OrgUnitType orgUnitType,
        String code,
        String name,
        String description,
        String address,
        Map<String, Object> metadata,
        List<OrgUnitResponse> children
) {
}
