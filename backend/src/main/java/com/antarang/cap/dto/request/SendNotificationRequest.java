package com.antarang.cap.dto.request;

import com.antarang.cap.domain.enums.NotificationDeliveryChannel;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.Map;
import java.util.UUID;

public record SendNotificationRequest(
        @NotNull UUID userId,
        @NotBlank String templateCode,
        @NotNull NotificationDeliveryChannel channel,
        Map<String, String> variables
) {
}
