package com.example.ecommerce.marketplace.application.inventory;

/**
 * Command object for restocking inventory.
 * Contains necessary data for adding new stock.
 */
public class RestockInventoryCommand {

    private final Long productId;
    private final Integer quantity;

    public RestockInventoryCommand(Long productId, Integer quantity) {
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
