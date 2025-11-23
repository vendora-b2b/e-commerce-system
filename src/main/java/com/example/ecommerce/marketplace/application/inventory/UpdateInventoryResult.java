package com.example.ecommerce.marketplace.application.inventory;

import com.example.ecommerce.marketplace.domain.inventory.Inventory;

/**
 * Result object for inventory update operation.
 * Follows the Result pattern to communicate success/failure.
 */
public class UpdateInventoryResult {

    private final boolean success;
    private final String errorCode;
    private final String errorMessage;
    private final Inventory inventory;

    private UpdateInventoryResult(boolean success, String errorCode, String errorMessage, Inventory inventory) {
        this.success = success;
        this.errorCode = errorCode;
        this.errorMessage = errorMessage;
        this.inventory = inventory;
    }

    public static UpdateInventoryResult success(Inventory inventory) {
        return new UpdateInventoryResult(true, null, null, inventory);
    }

    public static UpdateInventoryResult failure(String errorCode, String errorMessage) {
        return new UpdateInventoryResult(false, errorCode, errorMessage, null);
    }

    public boolean isSuccess() {
        return success;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public Inventory getInventory() {
        return inventory;
    }
}
