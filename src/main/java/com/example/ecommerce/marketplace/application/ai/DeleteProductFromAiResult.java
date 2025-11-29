package com.example.ecommerce.marketplace.application.ai;

/**
 * Result object returned after deleting a product from AI service.
 */
public class DeleteProductFromAiResult {

    private final boolean success;
    private final Long productId;
    private final String message;
    private final String errorCode;

    private DeleteProductFromAiResult(boolean success, Long productId, 
                                       String message, String errorCode) {
        this.success = success;
        this.productId = productId;
        this.message = message;
        this.errorCode = errorCode;
    }

    public static DeleteProductFromAiResult success(Long productId) {
        return new DeleteProductFromAiResult(
            true, productId, "Product deleted from AI service successfully", null
        );
    }

    public static DeleteProductFromAiResult failure(String message, String errorCode) {
        return new DeleteProductFromAiResult(false, null, message, errorCode);
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
