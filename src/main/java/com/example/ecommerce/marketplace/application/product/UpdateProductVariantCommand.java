package com.example.ecommerce.marketplace.application.product;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

/**
 * Command object for updating a product variant.
 * Encapsulates all data needed for partial variant update.
 */
@Getter
@AllArgsConstructor
@Builder
public class UpdateProductVariantCommand {
    private final Long productId;
    private final Long variantId;
    private final String sku;
    private final String color;
    private final String size;
    private final Double priceAdjustment;
}
