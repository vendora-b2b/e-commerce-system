package com.example.ecommerce.marketplace.web.model.quotation;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Response DTO for quotation offer details.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class QuotationOfferDetailResponse {
    
    private Long offerId;
    private String offerNumber;
    private Long quotationRequestId;
    private Long supplierId;
    private String status;
    private List<QuotationOfferItemDetail> offerItems;
    private Double totalAmount;
    
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime validUntil;
    
    private String notes;
    private String termsAndConditions;
    
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime createdAt;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class QuotationOfferItemDetail {
        private Long productId;
        private Long variantId;
        private Integer quantity;
        private Double quotedPrice;
        private String specifications;
        private String notes;
    }
}