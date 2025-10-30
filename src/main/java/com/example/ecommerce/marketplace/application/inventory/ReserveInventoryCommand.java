package com.example.ecommerce.marketplace.application.inventory;

/**
 * Command object for reserving inventory.
 * Contains necessary data for stock reservation.
 */
public class ReserveInventoryCommand {

    private final Long productId;
    private final Integer quantity;

    public ReserveInventoryCommand(Long productId, Integer quantity) {
        this.productId = productId;
        this.quantity = quantity;
    }

    public Long getProductId() {
        return productId;
    }

    public Integer getQuantity() {
        return quantity;
    }
}
