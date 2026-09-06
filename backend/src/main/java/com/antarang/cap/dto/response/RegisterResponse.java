package com.antarang.cap.dto.response;

import com.antarang.cap.domain.enums.ProfileType;
import com.antarang.cap.domain.enums.UserStatus;

import java.util.UUID;

public record RegisterResponse(
        UUID userId,
        ProfileType profileType,
        UserStatus status,
        boolean consentRequired,
        String consentType
) {
}
