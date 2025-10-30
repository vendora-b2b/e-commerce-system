package com.example.ecommerce.marketplace.application.inventory;

/**
 * Result object returned after inventory restock attempt.
 */
public class RestockInventoryResult {

    private final boolean success;
    private final String message;
    private final String errorCode;

    private RestockInventoryResult(boolean success, String message, String errorCode) {
        this.success = success;
        this.message = message;
        this.errorCode = errorCode;
    }

    public static RestockInventoryResult success() {
        return new RestockInventoryResult(true, "Inventory restocked successfully", null);
    }

    public static RestockInventoryResult failure(String message, String errorCode) {
        return new RestockInventoryResult(false, message, errorCode);
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
