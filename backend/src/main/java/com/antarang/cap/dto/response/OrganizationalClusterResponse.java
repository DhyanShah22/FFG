package com.antarang.cap.dto.response;

import com.antarang.cap.domain.enums.ClusterType;

import java.util.UUID;

public record OrganizationalClusterResponse(
        UUID id,
        UUID tenantId,
        String code,
        String name,
        String description,
        ClusterType clusterType
) {
}
