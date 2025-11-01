package com.example.ecommerce.marketplace.application.quotation;

/**
 * Result object returned after quotation request creation attempt.
 */
public class CreateQuotationRequestResult {
    
    private final boolean success;
    private final String requestNumber;
    private final Long requestId;
    private final String message;
    private final String errorCode;

    private CreateQuotationRequestResult(boolean success, String requestNumber, Long requestId, String message, String errorCode) {
        this.success = success;
        this.requestNumber = requestNumber;
        this.requestId = requestId;
        this.message = message;
        this.errorCode = errorCode;
    }

    public static CreateQuotationRequestResult success(String requestNumber, Long requestId) {
        return new CreateQuotationRequestResult(true, requestNumber, requestId, "Quotation request created successfully", null);
    }

    public static CreateQuotationRequestResult failure(String message, String errorCode) {
        return new CreateQuotationRequestResult(false, null, null, message, errorCode);
    }

    public boolean isSuccess() { return success; }
    public String getRequestNumber() { return requestNumber; }
    public Long getRequestId() { return requestId; }
    public String getMessage() { return message; }
    public String getErrorCode() { return errorCode; }
}
