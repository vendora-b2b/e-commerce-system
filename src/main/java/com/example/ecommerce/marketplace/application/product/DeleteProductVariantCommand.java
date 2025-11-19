package com.example.ecommerce.marketplace.application.product;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Command object for deleting a product variant.
 * Encapsulates the product and variant identifiers.
 */
@Getter
@AllArgsConstructor
public class DeleteProductVariantCommand {
    private final Long productId;
    private final Long variantId;
}
