package com.antarang.cap.dto.request;

import java.util.Map;

/**
 * Merges signup form progress into the server-side session (IF2AF0203 §II.7–8).
 */
public record UpdateSignupSessionRequest(
        Map<String, Object> sessionData
) {
}
