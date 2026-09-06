package com.antarang.cap.dto.response;

public record ReportDownloadResponse(
        String downloadUrl,
        int expiresInSeconds
) {
}
