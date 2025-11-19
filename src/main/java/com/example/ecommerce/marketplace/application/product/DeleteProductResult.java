package com.example.ecommerce.marketplace.application.product;

/**
 * Result object returned after product deletion attempt.
 */
public class DeleteProductResult {

    private final boolean success;
    private final String message;
    private final String errorCode;

    private DeleteProductResult(boolean success, String message, String errorCode) {
        this.success = success;
        this.message = message;
        this.errorCode = errorCode;
    }

    public static DeleteProductResult success() {
        return new DeleteProductResult(true, "Product deleted successfully", null);
    }

    public static DeleteProductResult failure(String message, String errorCode) {
        return new DeleteProductResult(false, message, errorCode);
    }

    public boolean isSuccess() {
        return success;
    }

    public String getMessage() {
        return message;
    }

    public String getErrorCode() {
        return errorCode;
    }
}
