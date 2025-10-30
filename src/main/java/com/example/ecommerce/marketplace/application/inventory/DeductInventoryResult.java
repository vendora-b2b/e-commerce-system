package com.example.ecommerce.marketplace.application.inventory;

/**
 * Result object returned after inventory deduction attempt.
 */
public class DeductInventoryResult {

    private final boolean success;
    private final String message;
    private final String errorCode;

    private DeductInventoryResult(boolean success, String message, String errorCode) {
        this.success = success;
        this.message = message;
        this.errorCode = errorCode;
    }

    public static DeductInventoryResult success() {
        return new DeductInventoryResult(true, "Inventory deducted successfully", null);
    }

    public static DeductInventoryResult failure(String message, String errorCode) {
        return new DeductInventoryResult(false, message, errorCode);
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
