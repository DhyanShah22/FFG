package com.antarang.cap.dto.response;

public record FacilitatorDashboardResponse(
        int assignedStudents,
        int submittedCount,
        int inProgressCount,
        int reportsGenerated
) {
}
