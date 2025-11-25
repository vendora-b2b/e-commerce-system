package com.example.ecommerce.marketplace.application.quotation;

import com.example.ecommerce.marketplace.domain.quotation.QuotationRequest;
import lombok.Getter;
import java.time.LocalDateTime;

/**
 * Result for updating quotation request status use case.
 */
@Getter
public class UpdateQuotationRequestStatusResult {
    private final boolean success;
    private final Long requestId;
    private final String status;
    private final LocalDateTime updatedAt;
    private final String errorMessage;
    private final String errorCode;

    private UpdateQuotationRequestStatusResult(boolean success, Long requestId, String status,
                                              LocalDateTime updatedAt, String errorMessage, String errorCode) {
        this.success = success;
        this.requestId = requestId;
        this.status = status;
        this.updatedAt = updatedAt;
        this.errorMessage = errorMessage;
        this.errorCode = errorCode;
    }

    public static UpdateQuotationRequestStatusResult success(QuotationRequest request) {
        // Convert domain status to API status
        String apiStatus = mapDomainStatusToApi(request.getStatus().toString());
        return new UpdateQuotationRequestStatusResult(true, request.getId(), apiStatus, 
                                                     LocalDateTime.now(), null, null);
    }

    public static UpdateQuotationRequestStatusResult failure(String errorMessage, String errorCode) {
        return new UpdateQuotationRequestStatusResult(false, null, null, null, errorMessage, errorCode);
    }

    private static String mapDomainStatusToApi(String domainStatus) {
        // Map domain statuses to API statuses
        switch (domainStatus) {
            case "OFFER_ACCEPTED":
                return "ACCEPTED";
            case "CANCELLED":
                return "CANCELLED";
            case "DRAFT":
                return "DRAFT";
            default:
                return domainStatus; // fallback
        }
    }
}