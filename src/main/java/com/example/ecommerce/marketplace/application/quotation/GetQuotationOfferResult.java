package com.example.ecommerce.marketplace.application.quotation;

import com.example.ecommerce.marketplace.domain.quotation.QuotationOffer;
import lombok.Getter;

/**
 * Result for getting a quotation offer use case.
 */
@Getter
public class GetQuotationOfferResult {
    private final boolean success;
    private final QuotationOffer offer;
    private final String errorMessage;
    private final String errorCode;

    private GetQuotationOfferResult(boolean success, QuotationOffer offer, 
                                   String errorMessage, String errorCode) {
        this.success = success;
        this.offer = offer;
        this.errorMessage = errorMessage;
        this.errorCode = errorCode;
    }

    public static GetQuotationOfferResult success(QuotationOffer offer) {
        return new GetQuotationOfferResult(true, offer, null, null);
    }

    public static GetQuotationOfferResult failure(String errorMessage, String errorCode) {
        return new GetQuotationOfferResult(false, null, errorMessage, errorCode);
    }
}