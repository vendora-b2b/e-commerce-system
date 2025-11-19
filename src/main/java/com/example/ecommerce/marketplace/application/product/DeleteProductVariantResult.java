package com.example.ecommerce.marketplace.application.product;

import lombok.Getter;

/**
 * Result object for delete product variant operation.
 * Encapsulates success/failure status and error details.
 */
@Getter
public class DeleteProductVariantResult {
    private final boolean success;
    private final String message;
    private final String errorCode;

    private DeleteProductVariantResult(boolean success, String message, String errorCode) {
        this.success = success;
        this.message = message;
        this.errorCode = errorCode;
    }

    public static DeleteProductVariantResult success() {
        return new DeleteProductVariantResult(true, null, null);
    }

    public static DeleteProductVariantResult failure(String message, String errorCode) {
        return new DeleteProductVariantResult(false, message, errorCode);
    }
}
