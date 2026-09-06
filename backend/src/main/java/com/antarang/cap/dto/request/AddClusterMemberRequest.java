package com.antarang.cap.dto.request;

import com.antarang.cap.domain.enums.ClusterMemberType;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record AddClusterMemberRequest(
        @NotNull ClusterMemberType memberType,
        @NotNull UUID memberId
) {
}
