package com.antarang.cap.exception;

import com.antarang.cap.common.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleResourceNotFound(
            ResourceNotFoundException ex,
            HttpServletRequest request
    ) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ErrorResponse.of(ex.getMessage(), "RESOURCE_NOT_FOUND", null, request.getRequestURI()));
    }

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ErrorResponse> handleBusinessException(
            BusinessException ex,
            HttpServletRequest request
    ) {
        HttpStatus status = switch (ex.getCode()) {
            case "DUPLICATE_RESOURCE", "QUESTION_IN_USE", "QUESTION_IN_PUBLISHED_VERSION" -> HttpStatus.CONFLICT;
            case "ACCESS_DENIED" -> HttpStatus.FORBIDDEN;
            case "UNAUTHORIZED" -> HttpStatus.UNAUTHORIZED;
            case "RESOURCE_NOT_FOUND", "CONFIG_GROUP_NOT_FOUND", "CONFIGURATION_NOT_FOUND" -> HttpStatus.NOT_FOUND;
            case "INVALID_STATE", "QUESTIONNAIRE_PUBLISHED", "IAR_VALIDATION_FAILED",
                 "ATTEMPT_NOT_IN_PROGRESS", "ATTEMPT_EXPIRED", "SCORING_RULE_MISSING",
                 "MANDATORY_RESPONSE_MISSING", "MANDATORY_UNANSWERED", "NO_SCORES",
                 "NO_RECOMMENDATION_RUN", "TEMPLATE_NOT_FOUND" -> HttpStatus.UNPROCESSABLE_ENTITY;
            case "INTEGRATION_UNAUTHORIZED" -> HttpStatus.UNAUTHORIZED;
            case "MAX_ATTEMPTS_EXCEEDED" -> HttpStatus.BAD_REQUEST;
            default -> HttpStatus.BAD_REQUEST;
        };
        Object details = ex.getData();
        return ResponseEntity.status(status)
                .body(ErrorResponse.of(ex.getMessage(), ex.getCode(), details, request.getRequestURI()));
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ErrorResponse> handleBadCredentials(
            BadCredentialsException ex,
            HttpServletRequest request
    ) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(ErrorResponse.of("Invalid login credentials", "AUTH_INVALID_CREDENTIALS", null, request.getRequestURI()));
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ErrorResponse> handleAuthenticationException(
            AuthenticationException ex,
            HttpServletRequest request
    ) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(ErrorResponse.of(ex.getMessage(), "UNAUTHORIZED", null, request.getRequestURI()));
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleAccessDenied(
            AccessDeniedException ex,
            HttpServletRequest request
    ) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(ErrorResponse.of("Access denied", "ACCESS_DENIED", null, request.getRequestURI()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationException(
            MethodArgumentNotValidException ex,
            HttpServletRequest request
    ) {
        List<Map<String, String>> details = new ArrayList<>();
        for (FieldError fieldError : ex.getBindingResult().getFieldErrors()) {
            Map<String, String> item = new HashMap<>();
            item.put("field", fieldError.getField());
            item.put("message", fieldError.getDefaultMessage());
            details.add(item);
        }
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ErrorResponse.of("Validation failed", "VALIDATION_ERROR", details, request.getRequestURI()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(Exception ex, HttpServletRequest request) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ErrorResponse.of("An unexpected error occurred", "SYSTEM_ERROR", null, request.getRequestURI()));
    }
}
