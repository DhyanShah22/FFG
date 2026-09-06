package com.antarang.cap.dto.response;

import java.time.Instant;
import java.util.UUID;

public record FacilitatorStudentResponse(
        UUID id,
        UUID facilitatorId,
        UUID studentId,
        UUID orgUnitId,
        boolean active,
        Instant assignedAt,
        Instant unassignedAt
) {
}
