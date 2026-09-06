package com.antarang.cap.dto.response;

import com.antarang.cap.domain.enums.ClusterMemberType;

import java.time.Instant;
import java.util.UUID;

public record ClusterMemberResponse(
        UUID id,
        UUID clusterId,
        ClusterMemberType memberType,
        UUID memberId,
        boolean active,
        Instant addedAt
) {
}
