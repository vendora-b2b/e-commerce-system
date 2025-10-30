package com.example.ecommerce.marketplace.application.inventory;

/**
 * Result object returned after inventory release attempt.
 */
public class ReleaseInventoryResult {

    private final boolean success;
    private final String message;
    private final String errorCode;

    private ReleaseInventoryResult(boolean success, String message, String errorCode) {
        this.success = success;
        this.message = message;
        this.errorCode = errorCode;
    }

    public static ReleaseInventoryResult success() {
        return new ReleaseInventoryResult(true, "Inventory released successfully", null);
    }

    public static ReleaseInventoryResult failure(String message, String errorCode) {
        return new ReleaseInventoryResult(false, message, errorCode);
    }

    public boolean isSuccess() {
        return success;
    }

    public String getMessage() {
        return message;
    }

    public String getErrorCode() {
        return errorCode;
    }
}
