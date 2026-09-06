package com.antarang.cap.dto.request;

public record UpdateCareerClusterRequest(
        String name,
        String description,
        Boolean isActive
) {
}
