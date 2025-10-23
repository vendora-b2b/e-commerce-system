package com.example.ecommerce.marketplace.application.supplier;

/**
 * Result object returned after supplier profile update attempt.
 */
public class UpdateSupplierProfileResult {

    private final boolean success;
    private final Long supplierId;
    private final String message;
    private final String errorCode;

    private UpdateSupplierProfileResult(boolean success, Long supplierId, String message, String errorCode) {
        this.success = success;
        this.supplierId = supplierId;
        this.message = message;
        this.errorCode = errorCode;
    }

    public static UpdateSupplierProfileResult success(Long supplierId) {
        return new UpdateSupplierProfileResult(true, supplierId, "Supplier profile updated successfully", null);
    }

    public static UpdateSupplierProfileResult failure(String message, String errorCode) {
        return new UpdateSupplierProfileResult(false, null, message, errorCode);
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
