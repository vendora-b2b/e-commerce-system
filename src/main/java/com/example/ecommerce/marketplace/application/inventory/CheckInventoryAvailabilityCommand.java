package com.example.ecommerce.marketplace.application.inventory;

/**
 * Command object for checking inventory availability.
 * Contains necessary data for availability check.
 */
public class CheckInventoryAvailabilityCommand {

    private final Long productId;
    private final Integer requestedQuantity;

    public CheckInventoryAvailabilityCommand(Long productId, Integer requestedQuantity) {
        this.productId = productId;
        this.requestedQuantity = requestedQuantity;
    }

    public Long getProductId() {
        return productId;
    }

    public Integer getRequestedQuantity() {
        return requestedQuantity;
    }
}
