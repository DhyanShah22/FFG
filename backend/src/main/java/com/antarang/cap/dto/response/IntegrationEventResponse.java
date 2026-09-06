package com.antarang.cap.dto.response;

import com.antarang.cap.domain.enums.IntegrationEventDirection;
import com.antarang.cap.domain.enums.IntegrationEventStatus;
import com.antarang.cap.domain.enums.IntegrationEventType;

import java.time.Instant;
import java.util.UUID;

public record IntegrationEventResponse(
        UUID id,
        UUID integrationClientId,
        IntegrationEventType eventType,
        IntegrationEventDirection direction,
        IntegrationEventStatus status,
        String errorMessage,
        Instant createdAt
) {
}
