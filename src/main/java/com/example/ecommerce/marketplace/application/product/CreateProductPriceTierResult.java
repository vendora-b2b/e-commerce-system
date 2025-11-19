package com.example.ecommerce.marketplace.application.product;

import com.example.ecommerce.marketplace.domain.product.PriceTier;
import lombok.Getter;

/**
 * Result object for create product price tier operation.
 * Encapsulates success/failure status, created price tier, and error details.
 */
@Getter
public class CreateProductPriceTierResult {
    private final boolean success;
    private final PriceTier priceTier;
    private final String message;
    private final String errorCode;

    private CreateProductPriceTierResult(boolean success, PriceTier priceTier, String message, String errorCode) {
        this.success = success;
        this.priceTier = priceTier;
        this.message = message;
        this.errorCode = errorCode;
    }

    public static CreateProductPriceTierResult success(PriceTier priceTier) {
        return new CreateProductPriceTierResult(true, priceTier, null, null);
    }

    public static CreateProductPriceTierResult failure(String message, String errorCode) {
        return new CreateProductPriceTierResult(false, null, message, errorCode);
    }
}
