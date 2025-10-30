package com.example.ecommerce.marketplace.application.inventory;

/**
 * Result object returned after inventory reservation attempt.
 */
public class ReserveInventoryResult {

    private final boolean success;
    private final String message;
    private final String errorCode;

    private ReserveInventoryResult(boolean success, String message, String errorCode) {
        this.success = success;
        this.message = message;
        this.errorCode = errorCode;
    }

    public static ReserveInventoryResult success() {
        return new ReserveInventoryResult(true, "Inventory reserved successfully", null);
    }

    public static ReserveInventoryResult failure(String message, String errorCode) {
        return new ReserveInventoryResult(false, message, errorCode);
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
