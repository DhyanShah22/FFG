package com.antarang.cap.dto.request;

import com.antarang.cap.domain.enums.ClusterType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CreateOrganizationalClusterRequest(
        @NotBlank String code,
        @NotBlank String name,
        String description,
        @NotNull ClusterType clusterType
) {
}
