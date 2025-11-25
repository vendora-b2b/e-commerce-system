package com.example.ecommerce.marketplace.application.quotation;

import com.example.ecommerce.marketplace.domain.quotation.QuotationRequest;
import lombok.Getter;
import org.springframework.data.domain.Page;

/**
 * Result for listing quotation requests use case.
 */
@Getter
public class ListQuotationRequestsResult {
    private final boolean success;
    private final Page<QuotationRequest> requests;
    private final String errorMessage;
    private final String errorCode;

    private ListQuotationRequestsResult(boolean success, Page<QuotationRequest> requests, 
                                       String errorMessage, String errorCode) {
        this.success = success;
        this.requests = requests;
        this.errorMessage = errorMessage;
        this.errorCode = errorCode;
    }

    public static ListQuotationRequestsResult success(Page<QuotationRequest> requests) {
        return new ListQuotationRequestsResult(true, requests, null, null);
    }

    public static ListQuotationRequestsResult failure(String errorMessage, String errorCode) {
        return new ListQuotationRequestsResult(false, null, errorMessage, errorCode);
    }
}