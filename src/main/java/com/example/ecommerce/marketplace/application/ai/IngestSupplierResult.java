package com.example.ecommerce.marketplace.application.ai;

/**
 * Result object returned after supplier ingestion into AI service.
 */
public class IngestSupplierResult {

    private final boolean success;
    private final Long supplierId;
    private final String message;
    private final String errorCode;

    private IngestSupplierResult(boolean success, Long supplierId, 
                                  String message, String errorCode) {
        this.success = success;
        this.supplierId = supplierId;
        this.message = message;
        this.errorCode = errorCode;
    }

    public static IngestSupplierResult success(Long supplierId) {
        return new IngestSupplierResult(
            true, supplierId, "Supplier ingested successfully", null
        );
    }

    public static IngestSupplierResult failure(String message, String errorCode) {
        return new IngestSupplierResult(false, null, message, errorCode);
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
