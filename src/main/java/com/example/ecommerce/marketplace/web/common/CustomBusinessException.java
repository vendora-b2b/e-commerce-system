package com.example.ecommerce.marketplace.web.common;

/**
 * Custom business exception for domain-specific errors.
 * Use this for business rule violations that should be exposed to the client.
 */
public class CustomBusinessException extends RuntimeException {
    
    private final String errorCode;
    
    public CustomBusinessException(String errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }
    
    public CustomBusinessException(String errorCode, String message, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
    }
    
    public String getErrorCode() {
        return errorCode;
    }
}