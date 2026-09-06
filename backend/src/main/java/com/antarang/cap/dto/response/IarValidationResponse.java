package com.antarang.cap.dto.response;

import java.util.List;

public record IarValidationResponse(
        boolean valid,
        List<ValidationError> errors
) {
    public record ValidationError(String path, String message) {
    }
}
