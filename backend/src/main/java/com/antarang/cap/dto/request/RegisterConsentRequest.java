package com.antarang.cap.dto.request;

import com.antarang.cap.domain.enums.ConsentType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * IF2AF0203 §II.3 — consent must be captured before account creation completes.
 */
public record RegisterConsentRequest(
        @NotNull Boolean consentGiven,
        @NotNull ConsentType consentType,
        @NotBlank String consentTextVersion,
        String guardianName,
        String guardianContact
) {
}
