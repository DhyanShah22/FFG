package com.antarang.cap.dto.request;

/**
 * Optional logout payload. When {@code refreshToken} is supplied it is revoked
 * alongside the access token presented in the Authorization header.
 */
public record LogoutRequest(
        String refreshToken
) {
}
