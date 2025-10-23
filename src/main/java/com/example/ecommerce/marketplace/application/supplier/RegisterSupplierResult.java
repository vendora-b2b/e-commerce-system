package com.example.ecommerce.marketplace.application.supplier;

/**
 * Result object returned after supplier registration attempt.
 */
public class RegisterSupplierResult {

    private final boolean success;
    private final Long supplierId;
    private final String message;
    private final String errorCode;

    private RegisterSupplierResult(boolean success, Long supplierId, String message, String errorCode) {
        this.success = success;
        this.supplierId = supplierId;
        this.message = message;
        this.errorCode = errorCode;
    }

    public static RegisterSupplierResult success(Long supplierId) {
        return new RegisterSupplierResult(true, supplierId, "Supplier registered successfully", null);
    }

    public static RegisterSupplierResult failure(String message, String errorCode) {
        return new RegisterSupplierResult(false, null, message, errorCode);
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
