package com.example.ecommerce.marketplace.application.product;

import com.example.ecommerce.marketplace.domain.product.PriceTier;
import lombok.Getter;

import java.util.List;

/**
 * Result object for list product price tiers operation.
 * Encapsulates success/failure status, price tiers list, and error details.
 */
@Getter
public class ListProductPriceTiersResult {
    private final boolean success;
    private final List<PriceTier> priceTiers;
    private final String message;
    private final String errorCode;

    private ListProductPriceTiersResult(boolean success, List<PriceTier> priceTiers, String message, String errorCode) {
        this.success = success;
        this.priceTiers = priceTiers;
        this.message = message;
        this.errorCode = errorCode;
    }

    public static ListProductPriceTiersResult success(List<PriceTier> priceTiers) {
        return new ListProductPriceTiersResult(true, priceTiers, null, null);
    }

    public static ListProductPriceTiersResult failure(String message, String errorCode) {
        return new ListProductPriceTiersResult(false, null, message, errorCode);
    }
}
