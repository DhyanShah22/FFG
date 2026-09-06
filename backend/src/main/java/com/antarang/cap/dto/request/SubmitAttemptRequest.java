package com.antarang.cap.dto.request;

import com.antarang.cap.domain.enums.SubmissionReason;

public record SubmitAttemptRequest(
        Integer clientElapsedSeconds,
        SubmissionReason submissionReason
) {
}
