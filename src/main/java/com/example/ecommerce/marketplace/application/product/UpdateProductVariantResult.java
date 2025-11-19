package com.example.ecommerce.marketplace.application.product;

import com.example.ecommerce.marketplace.domain.product.ProductVariant;
import lombok.Getter;

/**
 * Result object for UpdateProductVariantUseCase.
 * Encapsulates the outcome of updating a product variant.
 */
@Getter
public class UpdateProductVariantResult {
    private final boolean success;
    private final ProductVariant variant;
    private final String errorMessage;
    private final String errorCode;

    private UpdateProductVariantResult(boolean success, ProductVariant variant,
                                      String errorMessage, String errorCode) {
        this.success = success;
        this.variant = variant;
        this.errorMessage = errorMessage;
        this.errorCode = errorCode;
    }

    /**
     * Creates a success result with the updated variant.
     */
    public static UpdateProductVariantResult success(ProductVariant variant) {
        return new UpdateProductVariantResult(true, variant, null, null);
    }

    /**
     * Creates a failure result with error details.
     */
    public static UpdateProductVariantResult failure(String errorMessage, String errorCode) {
        return new UpdateProductVariantResult(false, null, errorMessage, errorCode);
    }
}
