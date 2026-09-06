package com.antarang.cap.common;

import java.time.Instant;

public record ApiResponse<T>(
        boolean success,
        String message,
        T data,
        Instant timestamp
) {

    public static <T> ApiResponse<T> success(T data) {
        return success(data, "Request processed successfully");
    }

    public static <T> ApiResponse<T> success(T data, String message) {
        return new ApiResponse<>(true, message, data, Instant.now());
    }
}
