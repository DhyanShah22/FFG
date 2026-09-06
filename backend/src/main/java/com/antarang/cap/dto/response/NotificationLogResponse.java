package com.antarang.cap.dto.response;

import com.antarang.cap.domain.enums.NotificationDeliveryChannel;
import com.antarang.cap.domain.enums.NotificationLogStatus;

import java.time.Instant;
import java.util.UUID;

public record NotificationLogResponse(
        UUID id,
        UUID userId,
        String templateCode,
        NotificationDeliveryChannel channel,
        String recipient,
        NotificationLogStatus status,
        Instant sentAt,
        Instant createdAt
) {
}
