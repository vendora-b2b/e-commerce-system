package com.example.ecommerce.marketplace.application.retailer;

/**
 * Result object returned after retailer registration attempt.
 */
public class RegisterRetailerResult {

    private final boolean success;
    private final Long retailerId;
    private final String message;
    private final String errorCode;

    private RegisterRetailerResult(boolean success, Long retailerId, String message, String errorCode) {
        this.success = success;
        this.retailerId = retailerId;
        this.message = message;
        this.errorCode = errorCode;
    }

    public static RegisterRetailerResult success(Long retailerId) {
        return new RegisterRetailerResult(true, retailerId, "Retailer registered successfully", null);
    }

    public static RegisterRetailerResult failure(String message, String errorCode) {
        return new RegisterRetailerResult(false, null, message, errorCode);
    }

    public boolean isSuccess() {
        return success;
    }

    public Long getRetailerId() {
        return retailerId;
    }

    public String getMessage() {
        return message;
    }

    public String getErrorCode() {
        return errorCode;
    }
}
