package com.example.ecommerce.marketplace.application.ai;

/**
 * Result object returned after deleting a supplier from AI service.
 */
public class DeleteSupplierFromAiResult {

    private final boolean success;
    private final Long supplierId;
    private final String message;
    private final String errorCode;

    private DeleteSupplierFromAiResult(boolean success, Long supplierId, 
                                        String message, String errorCode) {
        this.success = success;
        this.supplierId = supplierId;
        this.message = message;
        this.errorCode = errorCode;
    }

    public static DeleteSupplierFromAiResult success(Long supplierId) {
        return new DeleteSupplierFromAiResult(
            true, supplierId, "Supplier deleted from AI service successfully", null
        );
    }

    public static DeleteSupplierFromAiResult failure(String message, String errorCode) {
        return new DeleteSupplierFromAiResult(false, null, message, errorCode);
    }

    public boolean isSuccess() {
        return success;
    }

    public Long getSupplierId() {
        return supplierId;
    }

    public String getMessage() {
        return message;
    }

    public String getErrorCode() {
        return errorCode;
    }
}
