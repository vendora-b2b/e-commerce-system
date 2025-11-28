package com.example.ecommerce.marketplace.web.model.quotation;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Response DTO for quotation request details.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class QuotationRequestDetailResponse {
    
    private Long requestId;
    private String requestNumber;
    private Long retailerId;
    private Long supplierId;
    private String status;
    private List<QuotationRequestItemDetail> requestItems;
    
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime validUntil;
    
    private String notes;
    
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime createdAt;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class QuotationRequestItemDetail {
        private Long productId;
        private Long variantId;
        private Integer quantity;
        private String specifications;
    }
}