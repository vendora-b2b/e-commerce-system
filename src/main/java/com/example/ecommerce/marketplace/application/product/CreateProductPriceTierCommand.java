package com.example.ecommerce.marketplace.application.product;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Command object for creating a product price tier.
 * Encapsulates all data needed for price tier creation.
 */
@Getter
@AllArgsConstructor
public class CreateProductPriceTierCommand {
    private final Long productId;
    private final Integer minQuantity;
    private final Integer maxQuantity;
    private final Double discountPercent;
}
