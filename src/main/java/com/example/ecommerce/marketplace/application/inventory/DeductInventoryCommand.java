package com.example.ecommerce.marketplace.application.inventory;

/**
 * Command object for deducting inventory.
 * Contains necessary data for stock deduction.
 */
public class DeductInventoryCommand {

    private final Long productId;
    private final Integer quantity;

    public DeductInventoryCommand(Long productId, Integer quantity) {
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
