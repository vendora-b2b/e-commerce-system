package com.example.ecommerce.marketplace.application.product;

/**
 * Result object returned after product creation attempt.
 */
public class CreateProductResult {

    private final boolean success;
    private final Long productId;
    private final String message;
    private final String errorCode;

    private CreateProductResult(boolean success, Long productId, String message, String errorCode) {
        this.success = success;
        this.productId = productId;
        this.message = message;
        this.errorCode = errorCode;
    }

    public static CreateProductResult success(Long productId) {
        return new CreateProductResult(true, productId, "Product created successfully", null);
    }

    public static CreateProductResult failure(String message, String errorCode) {
        return new CreateProductResult(false, null, message, errorCode);
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
