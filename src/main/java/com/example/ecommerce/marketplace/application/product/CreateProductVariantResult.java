package com.example.ecommerce.marketplace.application.product;

import com.example.ecommerce.marketplace.domain.product.ProductVariant;
import lombok.Getter;

/**
 * Result object for CreateProductVariantUseCase.
 * Encapsulates the outcome of creating a product variant.
 */
@Getter
public class CreateProductVariantResult {
    private final boolean success;
    private final ProductVariant variant;
    private final String errorMessage;
    private final String errorCode;

    private CreateProductVariantResult(boolean success, ProductVariant variant,
                                      String errorMessage, String errorCode) {
        this.success = success;
        this.variant = variant;
        this.errorMessage = errorMessage;
        this.errorCode = errorCode;
    }

    /**
     * Creates a success result with the created variant.
     */
    public static CreateProductVariantResult success(ProductVariant variant) {
        return new CreateProductVariantResult(true, variant, null, null);
    }

    /**
     * Creates a failure result with error details.
     */
    public static CreateProductVariantResult failure(String errorMessage, String errorCode) {
        return new CreateProductVariantResult(false, null, errorMessage, errorCode);
    }
}
