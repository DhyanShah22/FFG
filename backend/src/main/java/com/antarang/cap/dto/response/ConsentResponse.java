package com.antarang.cap.dto.response;

import com.antarang.cap.domain.enums.ConsentType;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

public record ConsentResponse(
        UUID id,
        UUID userId,
        ConsentType consentType,
        String consentTextVersion,
        String guardianName,
        String guardianContact,
        boolean consentGiven,
        Instant consentGivenAt,
        Instant consentWithdrawnAt,
        Map<String, Object> metadata,
        Instant createdAt
) {
}
