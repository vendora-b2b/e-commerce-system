package com.example.ecommerce.marketplace.web.common;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Global exception handler for all REST controllers.
 * Provides consistent error response format across the application.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Handle validation errors from @Valid annotation.
     * Returns 400 BAD REQUEST with field-level error details.
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationErrors(
        MethodArgumentNotValidException ex,
        HttpServletRequest request
    ) {
        List<ErrorResponse.FieldError> fieldErrors = ex.getBindingResult()
            .getFieldErrors()
            .stream()
            .map(error -> new ErrorResponse.FieldError(
                error.getField(),
                error.getDefaultMessage()
            ))
            .collect(Collectors.toList());

        ErrorResponse errorResponse = ErrorResponse.of(
            HttpStatus.BAD_REQUEST.value(),
            "VALIDATION_ERROR",
            "Validation failed for one or more fields",
            request.getRequestURI(),
            fieldErrors
        );

        return ResponseEntity.badRequest().body(errorResponse);
    }

    /**
     * Handle constraint violation errors.
     * Returns 400 BAD REQUEST.
     */
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorResponse> handleConstraintViolation(
        ConstraintViolationException ex,
        HttpServletRequest request
    ) {
        List<ErrorResponse.FieldError> fieldErrors = ex.getConstraintViolations()
            .stream()
            .map(violation -> new ErrorResponse.FieldError(
                getFieldName(violation),
                violation.getMessage()
            ))
            .collect(Collectors.toList());

        ErrorResponse errorResponse = ErrorResponse.of(
            HttpStatus.BAD_REQUEST.value(),
            "VALIDATION_ERROR",
            "Constraint validation failed",
            request.getRequestURI(),
            fieldErrors
        );

        return ResponseEntity.badRequest().body(errorResponse);
    }

    /**
     * Handle type mismatch errors (e.g., passing string where integer expected).
     * Returns 400 BAD REQUEST.
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponse> handleTypeMismatch(
        MethodArgumentTypeMismatchException ex,
        HttpServletRequest request
    ) {
        Class<?> requiredType = ex.getRequiredType();
        String typeName = (requiredType != null) ? requiredType.getSimpleName() : "unknown";
        String message = String.format(
            "Invalid value for parameter '%s': expected type %s",
            ex.getName(),
            typeName
        );

        ErrorResponse errorResponse = ErrorResponse.of(
            HttpStatus.BAD_REQUEST.value(),
            "INVALID_PARAMETER",
            message,
            request.getRequestURI()
        );

        return ResponseEntity.badRequest().body(errorResponse);
    }

    /**
     * Handle custom business exceptions.
     * Returns 400 BAD REQUEST with the custom error code and message.
     */
    @ExceptionHandler(CustomBusinessException.class)
    public ResponseEntity<ErrorResponse> handleCustomBusinessException(
        CustomBusinessException ex,
        HttpServletRequest request
    ) {
        ErrorResponse errorResponse = ErrorResponse.of(
            HttpStatus.BAD_REQUEST.value(),
            ex.getErrorCode(),
            ex.getMessage(),
            request.getRequestURI()
        );

        return ResponseEntity.badRequest().body(errorResponse);
    }

    /**
     * Handle business logic exceptions (IllegalStateException).
     * Returns 400 BAD REQUEST with the actual error message.
     */
    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ErrorResponse> handleIllegalStateException(
        IllegalStateException ex,
        HttpServletRequest request
    ) {
        ErrorResponse errorResponse = ErrorResponse.of(
            HttpStatus.BAD_REQUEST.value(),
            "BUSINESS_RULE_VIOLATION",
            ex.getMessage(),
            request.getRequestURI()
        );

        return ResponseEntity.badRequest().body(errorResponse);
    }

    /**
     * Handle input validation exceptions (IllegalArgumentException).
     * Returns 400 BAD REQUEST with the actual error message.
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgumentException(
        IllegalArgumentException ex,
        HttpServletRequest request
    ) {
        ErrorResponse errorResponse = ErrorResponse.of(
            HttpStatus.BAD_REQUEST.value(),
            "INVALID_INPUT",
            ex.getMessage(),
            request.getRequestURI()
        );

        return ResponseEntity.badRequest().body(errorResponse);
    }

    /**
     * Handle authentication exceptions (UsernameNotFoundException).
     * Returns 401 UNAUTHORIZED with the actual error message.
     */
    @ExceptionHandler(org.springframework.security.core.userdetails.UsernameNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleUsernameNotFoundException(
        org.springframework.security.core.userdetails.UsernameNotFoundException ex,
        HttpServletRequest request
    ) {
        ErrorResponse errorResponse = ErrorResponse.of(
            HttpStatus.UNAUTHORIZED.value(),
            "AUTHENTICATION_FAILED",
            ex.getMessage(),
            request.getRequestURI()
        );

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(errorResponse);
    }

    /**
     * Handle 404 Not Found errors.
     */
    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(
        NoResourceFoundException ex,
        HttpServletRequest request
    ) {
        ErrorResponse errorResponse = ErrorResponse.of(
            HttpStatus.NOT_FOUND.value(),
            "NOT_FOUND",
            "The requested resource was not found",
            request.getRequestURI()
        );

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
    }

    /**
     * Handle all other unhandled exceptions.
     * Returns 500 INTERNAL SERVER ERROR.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(
        Exception ex,
        HttpServletRequest request
    ) {
        // Log the full exception for debugging
        System.err.println("Unhandled exception: " + ex.getClass().getName());
        ex.printStackTrace();

        ErrorResponse errorResponse = ErrorResponse.of(
            HttpStatus.INTERNAL_SERVER_ERROR.value(),
            "INTERNAL_SERVER_ERROR",
            "An unexpected error occurred. Please try again later.",
            request.getRequestURI()
        );

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
    }

    /**
     * Extract field name from constraint violation.
     */
    private String getFieldName(ConstraintViolation<?> violation) {
        String path = violation.getPropertyPath().toString();
        String[] parts = path.split("\\.");
        return parts.length > 0 ? parts[parts.length - 1] : path;
    }
}
