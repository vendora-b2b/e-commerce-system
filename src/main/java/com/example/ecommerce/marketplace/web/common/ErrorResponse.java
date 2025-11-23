package com.example.ecommerce.marketplace.web.common;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Standard error response structure for all API errors.
 * Provides consistent error format across the application.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ErrorResponse {

    private int status;
    private String error;
    private String message;
    private LocalDateTime timestamp;
    private String path;
    private List<FieldError> errors;

    /**
     * Field-level error details for validation errors.
     */
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class FieldError {
        private String field;
        private String message;
    }

    /**
     * Creates a simple error response without field errors.
     */
    public static ErrorResponse of(int status, String error, String message, String path) {
        return new ErrorResponse(
            status,
            error,
            message,
            LocalDateTime.now(),
            path,
            null
        );
    }

    /**
     * Creates an error response with field-level validation errors.
     */
    public static ErrorResponse of(int status, String error, String message, String path, List<FieldError> errors) {
        return new ErrorResponse(
            status,
            error,
            message,
            LocalDateTime.now(),
            path,
            errors
        );
    }
}
