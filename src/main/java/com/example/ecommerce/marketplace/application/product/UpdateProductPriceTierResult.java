package com.example.ecommerce.marketplace.application.product;

import com.example.ecommerce.marketplace.domain.product.PriceTier;
import lombok.Getter;

/**
 * Result object for update product price tier operation.
 * Encapsulates success/failure status, updated price tier, and error details.
 */
@Getter
public class UpdateProductPriceTierResult {
    private final boolean success;
    private final PriceTier priceTier;
    private final String message;
    private final String errorCode;

    private UpdateProductPriceTierResult(boolean success, PriceTier priceTier, String message, String errorCode) {
        this.success = success;
        this.priceTier = priceTier;
        this.message = message;
        this.errorCode = errorCode;
    }

    public static UpdateProductPriceTierResult success(PriceTier priceTier) {
        return new UpdateProductPriceTierResult(true, priceTier, null, null);
    }

    public static UpdateProductPriceTierResult failure(String message, String errorCode) {
        return new UpdateProductPriceTierResult(false, null, message, errorCode);
    }
}
