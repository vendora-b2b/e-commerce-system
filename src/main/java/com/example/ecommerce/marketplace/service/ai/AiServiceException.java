package com.example.ecommerce.marketplace.service.ai;

/**
 * Exception thrown when communication with the AI Service fails.
 * 
 * <p>This exception wraps errors that occur during HTTP calls to the Python AI Service,
 * including network errors, timeouts, and service-side errors.</p>
 */
public class AiServiceException extends RuntimeException {

    public AiServiceException(String message) {
        super(message);
    }

    public AiServiceException(String message, Throwable cause) {
        super(message, cause);
    }
}
