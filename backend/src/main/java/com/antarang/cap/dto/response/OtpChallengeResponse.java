package com.antarang.cap.dto.response;

import com.antarang.cap.domain.enums.OtpChannel;

import java.util.UUID;

public record OtpChallengeResponse(
        UUID challengeId,
        OtpChannel channel,
        String destination,
        long expiresInSeconds,
        long resendAvailableInSeconds,
        String simulatedOtp
) {
}
