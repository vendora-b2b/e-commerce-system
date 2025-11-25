package com.example.ecommerce.marketplace.web.model.quotation;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Response DTO for listing quotation offers with pagination.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class QuotationOfferListResponse {

    private List<QuotationOfferSummary> content;
    private QuotationRequestListResponse.PageInfo page;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class QuotationOfferSummary {
        private Long offerId;
        private String offerNumber;
        private Long quotationRequestId;
        private Long supplierId;
        private Double totalAmount;
        private String status;
        
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
        private LocalDateTime validUntil;
        
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
        private LocalDateTime createdAt;
    }
}