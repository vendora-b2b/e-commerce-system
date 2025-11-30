package com.example.ecommerce.marketplace.application.ai;

/**
 * Command object for deleting a product from the AI vector database.
 */
public class DeleteProductFromAiCommand {

    private final Long productId;

    public DeleteProductFromAiCommand(Long productId) {
        this.productId = productId;
    }

    public Long getProductId() {
        return productId;
    }
}
