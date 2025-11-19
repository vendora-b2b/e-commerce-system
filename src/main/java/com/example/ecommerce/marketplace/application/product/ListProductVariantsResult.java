package com.example.ecommerce.marketplace.application.product;

import com.example.ecommerce.marketplace.domain.product.ProductVariant;
import lombok.Getter;

import java.util.List;

/**
 * Result object for ListProductVariantsUseCase.
 * Encapsulates the outcome of listing product variants.
 */
@Getter
public class ListProductVariantsResult {
    private final boolean success;
    private final List<ProductVariant> variants;
    private final String errorMessage;
    private final String errorCode;

    private ListProductVariantsResult(boolean success, List<ProductVariant> variants, 
                                     String errorMessage, String errorCode) {
        this.success = success;
        this.variants = variants;
        this.errorMessage = errorMessage;
        this.errorCode = errorCode;
    }

    /**
     * Creates a success result with variants.
     */
    public static ListProductVariantsResult success(List<ProductVariant> variants) {
        return new ListProductVariantsResult(true, variants, null, null);
    }

    /**
     * Creates a failure result with error details.
     */
    public static ListProductVariantsResult failure(String errorMessage, String errorCode) {
        return new ListProductVariantsResult(false, null, errorMessage, errorCode);
    }
}
