package com.example.ecommerce.marketplace.application.ai;

/**
 * Result object returned after product ingestion.
 */
public class IngestProductResult {

    private final boolean success;
    private final Long productId;
    private final String sku;
    private final String message;
    private final String errorCode;

    private IngestProductResult(boolean success, Long productId, String sku, 
                                 String message, String errorCode) {
        this.success = success;
        this.productId = productId;
        this.sku = sku;
        this.message = message;
        this.errorCode = errorCode;
    }

    public static IngestProductResult success(Long productId, String sku) {
        return new IngestProductResult(
            true, productId, sku, "Product ingested successfully", null
        );
    }

    public static IngestProductResult failure(String message, String errorCode) {
        return new IngestProductResult(false, null, null, message, errorCode);
    }

    public boolean isSuccess() {
        return success;
    }

    public Long getProductId() {
        return productId;
    }

    public String getSku() {
        return sku;
    }

    public String getMessage() {
        return message;
    }

    public String getErrorCode() {
        return errorCode;
    }
}
