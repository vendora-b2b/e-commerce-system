package com.example.ecommerce.marketplace.application.product;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Command object for creating a product variant.
 * Encapsulates all data needed to create a variant.
 */
@Getter
@AllArgsConstructor
public class CreateProductVariantCommand {
    private final Long productId;
    private final String sku;
    private final String color;
    private final String size;
    private final Double priceAdjustment;
}
