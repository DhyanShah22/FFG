package com.antarang.cap.dto.response;

import com.antarang.cap.domain.enums.AssignmentAction;

import java.time.Instant;
import java.util.UUID;

public record AssignmentHistoryResponse(
        UUID id,
        UUID assignmentId,
        UUID studentId,
        UUID oldFacilitatorId,
        UUID newFacilitatorId,
        AssignmentAction action,
        UUID performedBy,
        Instant performedAt,
        String remarks
) {
}
