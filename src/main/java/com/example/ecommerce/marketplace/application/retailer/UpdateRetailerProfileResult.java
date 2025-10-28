package com.example.ecommerce.marketplace.application.retailer;

/**
 * Result object returned after retailer profile update attempt.
 */
public class UpdateRetailerProfileResult {

    private final boolean success;
    private final Long retailerId;
    private final String message;
    private final String errorCode;

    private UpdateRetailerProfileResult(boolean success, Long retailerId, String message, String errorCode) {
        this.success = success;
        this.retailerId = retailerId;
        this.message = message;
        this.errorCode = errorCode;
    }

    public static UpdateRetailerProfileResult success(Long retailerId) {
        return new UpdateRetailerProfileResult(true, retailerId, "Profile updated successfully", null);
    }

    public static UpdateRetailerProfileResult failure(String message, String errorCode) {
        return new UpdateRetailerProfileResult(false, null, message, errorCode);
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
