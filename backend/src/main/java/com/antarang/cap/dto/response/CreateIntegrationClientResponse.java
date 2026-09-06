package com.antarang.cap.dto.response;

import java.util.UUID;

public record CreateIntegrationClientResponse(
        UUID id,
        String clientCode,
        String apiKey
) {
}
