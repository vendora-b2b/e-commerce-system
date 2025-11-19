package com.example.ecommerce.marketplace.application.product;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * Command object for deleting a product price tier.
 * Encapsulates the data needed to delete a price tier.
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class DeleteProductPriceTierCommand {
    private Long productId;
    private Long tierId;
}
