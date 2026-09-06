package com.antarang.cap.dto.response;

public record StudentDashboardResponse(
        int assessmentsAvailable,
        int attemptsInProgress,
        int attemptsSubmitted,
        int reportsReady,
        boolean recommendationsReady
) {
}
