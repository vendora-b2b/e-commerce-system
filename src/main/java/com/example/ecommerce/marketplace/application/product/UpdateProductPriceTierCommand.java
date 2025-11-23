package com.example.ecommerce.marketplace.application.product;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Command object for updating a product price tier.
 * Encapsulates all data needed for price tier update.
 */
@Getter
@AllArgsConstructor
public class UpdateProductPriceTierCommand {
    private final Long productId;
    private final Long tierId;
    private final Integer minQuantity;
    private final Integer maxQuantity;
    private final Double discountPercent;
}
