package com.antarang.cap.common;

import java.time.Instant;

public record ErrorResponse(
        boolean success,
        String errorCode,
        String message,
        Object details,
        Instant timestamp,
        String path
) {

    public static ErrorResponse of(String message, String code) {
        return of(message, code, null, null);
    }

    public static ErrorResponse of(String message, String code, Object details) {
        return of(message, code, details, null);
    }

    public static ErrorResponse of(String message, String code, Object details, String path) {
        return new ErrorResponse(false, code, message, details, Instant.now(), path);
    }
}
