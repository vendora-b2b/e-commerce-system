package com.example.ecommerce.marketplace.application.inventory;

/**
 * Command object for releasing reserved inventory.
 * Contains necessary data for stock release.
 */
public class ReleaseInventoryCommand {

    private final Long productId;
    private final Integer quantity;

    public ReleaseInventoryCommand(Long productId, Integer quantity) {
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
