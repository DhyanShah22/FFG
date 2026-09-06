package com.antarang.cap.dto.request;

/**
 * Category update. Code is immutable. All fields optional; only non-null values are applied.
 */
public record UpdateQuestionCategoryRequest(
        String name,
        String description,
        String domainCode,
        Boolean isActive
) {
}
