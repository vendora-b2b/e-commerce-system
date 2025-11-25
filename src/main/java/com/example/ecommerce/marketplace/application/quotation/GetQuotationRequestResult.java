package com.example.ecommerce.marketplace.application.quotation;

import com.example.ecommerce.marketplace.domain.quotation.QuotationRequest;
import lombok.Getter;

/**
 * Result for getting a quotation request use case.
 */
@Getter
public class GetQuotationRequestResult {
    private final boolean success;
    private final QuotationRequest request;
    private final String errorMessage;
    private final String errorCode;

    private GetQuotationRequestResult(boolean success, QuotationRequest request, 
                                     String errorMessage, String errorCode) {
        this.success = success;
        this.request = request;
        this.errorMessage = errorMessage;
        this.errorCode = errorCode;
    }

    public static GetQuotationRequestResult success(QuotationRequest request) {
        return new GetQuotationRequestResult(true, request, null, null);
    }

    public static GetQuotationRequestResult failure(String errorMessage, String errorCode) {
        return new GetQuotationRequestResult(false, null, errorMessage, errorCode);
    }
}