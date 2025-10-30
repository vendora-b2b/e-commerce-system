package com.example.ecommerce.marketplace.application.product;

/**
 * Result object returned after product update attempt.
 */
public class UpdateProductResult {

    private final boolean success;
    private final Long productId;
    private final String message;
    private final String errorCode;

    private UpdateProductResult(boolean success, Long productId, String message, String errorCode) {
        this.success = success;
        this.productId = productId;
        this.message = message;
        this.errorCode = errorCode;
    }

    public static UpdateProductResult success(Long productId) {
        return new UpdateProductResult(true, productId, "Product updated successfully", null);
    }

    public static UpdateProductResult failure(String message, String errorCode) {
        return new UpdateProductResult(false, null, message, errorCode);
    }

    public boolean isSuccess() {
        return success;
    }

    public Long getProductId() {
        return productId;
    }

    public String getMessage() {
        return message;
    }

    public String getErrorCode() {
        return errorCode;
    }
}
