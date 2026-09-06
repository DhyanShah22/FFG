package com.antarang.cap.domain.enums;

import com.antarang.cap.exception.BusinessException;

public final class QuestionTypeMapper {

    private QuestionTypeMapper() {
    }

    public static QuestionType fromApi(String value) {
        if (value == null || value.isBlank()) {
            throw new BusinessException("questionType is required", "VALIDATION_ERROR");
        }
        return switch (value.trim().toUpperCase()) {
            case "SINGLE_CHOICE", "RADIO" -> QuestionType.RADIO;
            case "MULTI_CHOICE", "CHECKBOX" -> QuestionType.CHECKBOX;
            case "RATING" -> QuestionType.RATING;
            case "SHORT_TEXT" -> QuestionType.SHORT_TEXT;
            case "LONG_TEXT" -> QuestionType.LONG_TEXT;
            default -> {
                try {
                    yield QuestionType.valueOf(value.trim().toUpperCase());
                } catch (IllegalArgumentException ex) {
                    throw new BusinessException("Unsupported questionType: " + value, "VALIDATION_ERROR");
                }
            }
        };
    }
}
