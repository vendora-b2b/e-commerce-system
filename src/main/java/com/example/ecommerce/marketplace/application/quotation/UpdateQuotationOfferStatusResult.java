package com.example.ecommerce.marketplace.application.quotation;

import com.example.ecommerce.marketplace.domain.quotation.QuotationOffer;
import lombok.Getter;
import java.time.LocalDateTime;

/**
 * Result for updating quotation offer status use case.
 */
@Getter
public class UpdateQuotationOfferStatusResult {
    private final boolean success;
    private final Long offerId;
    private final String status;
    private final LocalDateTime updatedAt;
    private final String errorMessage;
    private final String errorCode;

    private UpdateQuotationOfferStatusResult(boolean success, Long offerId, String status,
                                            LocalDateTime updatedAt, String errorMessage, String errorCode) {
        this.success = success;
        this.offerId = offerId;
        this.status = status;
        this.updatedAt = updatedAt;
        this.errorMessage = errorMessage;
        this.errorCode = errorCode;
    }

    public static UpdateQuotationOfferStatusResult success(QuotationOffer offer) {
        // Convert domain status to API status
        String apiStatus = mapDomainStatusToApi(offer.getStatus().toString());
        return new UpdateQuotationOfferStatusResult(true, offer.getId(), apiStatus, 
                                                   LocalDateTime.now(), null, null);
    }

    public static UpdateQuotationOfferStatusResult failure(String errorMessage, String errorCode) {
        return new UpdateQuotationOfferStatusResult(false, null, null, null, errorMessage, errorCode);
    }

    private static String mapDomainStatusToApi(String domainStatus) {
        // The API statuses match domain statuses for offers
        return domainStatus;
    }
}