package com.antarang.cap.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

import java.util.UUID;

public record IntegrationCreateStudentRequest(
        @NotBlank String externalSystem,
        @NotBlank String externalStudentId,
        @NotBlank String firstName,
        String lastName,
        @Email String email,
        String mobileNumber,
        UUID primaryOrgUnitId,
        String grade
) {
}
