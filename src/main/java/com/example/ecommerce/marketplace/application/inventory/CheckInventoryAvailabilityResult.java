package com.example.ecommerce.marketplace.application.inventory;

/**
 * Result object returned after checking inventory availability.
 */
public class CheckInventoryAvailabilityResult {

    private final boolean success;
    private final boolean available;
    private final boolean sufficientStock;
    private final Integer availableQuantity;
    private final String status;
    private final String message;
    private final String errorCode;

    private CheckInventoryAvailabilityResult(boolean success, boolean available, boolean sufficientStock,
                                            Integer availableQuantity, String status, String message, String errorCode) {
        this.success = success;
        this.available = available;
        this.sufficientStock = sufficientStock;
        this.availableQuantity = availableQuantity;
        this.status = status;
        this.message = message;
        this.errorCode = errorCode;
    }

    public static CheckInventoryAvailabilityResult success(boolean available, boolean sufficientStock,
                                                          Integer availableQuantity, String status) {
        return new CheckInventoryAvailabilityResult(true, available, sufficientStock,
            availableQuantity, status, "Availability checked successfully", null);
    }

    public static CheckInventoryAvailabilityResult failure(String message, String errorCode) {
        return new CheckInventoryAvailabilityResult(false, false, false, null, null, message, errorCode);
    }

    public boolean isSuccess() {
        return success;
    }

    public boolean isAvailable() {
        return available;
    }

    public boolean isSufficientStock() {
        return sufficientStock;
    }

    public Integer getAvailableQuantity() {
        return availableQuantity;
    }

    public String getStatus() {
        return status;
    }

    public String getMessage() {
        return message;
    }

    public String getErrorCode() {
        return errorCode;
    }
}
