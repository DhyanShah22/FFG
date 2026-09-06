package com.antarang.cap.dto.request;

import com.antarang.cap.domain.enums.ConsentType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.Map;
import java.util.UUID;

public record CreateConsentRequest(
        UUID userId,
        @NotNull ConsentType consentType,
        @NotBlank String consentTextVersion,
        String guardianName,
        String guardianContact,
        boolean consentGiven,
        Map<String, Object> metadata
) {
}
