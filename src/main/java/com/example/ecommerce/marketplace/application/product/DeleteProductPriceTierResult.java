package com.example.ecommerce.marketplace.application.product;

import lombok.Getter;

/**
 * Result object for deleting a product price tier.
 * Contains success/failure status and error details if applicable.
 */
@Getter
public class DeleteProductPriceTierResult {
    private final boolean success;
    private final String errorMessage;
    private final String errorCode;

    private DeleteProductPriceTierResult(boolean success, String errorMessage, String errorCode) {
        this.success = success;
        this.errorMessage = errorMessage;
        this.errorCode = errorCode;
    }

    /**
     * Creates a successful result.
     */
    public static DeleteProductPriceTierResult success() {
        return new DeleteProductPriceTierResult(true, null, null);
    }

    /**
     * Creates a failure result with error details.
     */
    public static DeleteProductPriceTierResult failure(String errorMessage, String errorCode) {
        return new DeleteProductPriceTierResult(false, errorMessage, errorCode);
    }
}
