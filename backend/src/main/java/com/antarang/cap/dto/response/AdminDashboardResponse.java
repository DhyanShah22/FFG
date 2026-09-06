package com.antarang.cap.dto.response;

public record AdminDashboardResponse(
        long usersActive,
        long attemptsSubmitted,
        long reportsGenerated,
        long configGroupsActive
) {
}
