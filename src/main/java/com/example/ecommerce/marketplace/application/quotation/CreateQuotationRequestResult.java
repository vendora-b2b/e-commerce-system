package com.example.ecommerce.marketplace.application.quotation;

public class CreateQuotationRequestResult {
    private final String requestNumber;
    private final Long requestId;

    public CreateQuotationRequestResult(String requestNumber, Long requestId) {
        this.requestNumber = requestNumber;
        this.requestId = requestId;
    }

    public String getRequestNumber() { return requestNumber; }
    public Long getRequestId() { return requestId; }
}
