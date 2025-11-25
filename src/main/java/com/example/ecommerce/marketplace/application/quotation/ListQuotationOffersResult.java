package com.example.ecommerce.marketplace.application.quotation;

import com.example.ecommerce.marketplace.domain.quotation.QuotationOffer;
import lombok.Getter;
import org.springframework.data.domain.Page;

/**
 * Result for listing quotation offers use case.
 */
@Getter
public class ListQuotationOffersResult {
    private final boolean success;
    private final Page<QuotationOffer> offers;
    private final String errorMessage;
    private final String errorCode;

    private ListQuotationOffersResult(boolean success, Page<QuotationOffer> offers, 
                                     String errorMessage, String errorCode) {
        this.success = success;
        this.offers = offers;
        this.errorMessage = errorMessage;
        this.errorCode = errorCode;
    }

    public static ListQuotationOffersResult success(Page<QuotationOffer> offers) {
        return new ListQuotationOffersResult(true, offers, null, null);
    }

    public static ListQuotationOffersResult failure(String errorMessage, String errorCode) {
        return new ListQuotationOffersResult(false, null, errorMessage, errorCode);
    }
}