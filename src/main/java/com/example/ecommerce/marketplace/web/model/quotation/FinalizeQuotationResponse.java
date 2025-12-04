package com.example.ecommerce.marketplace.web.model.quotation;

import com.example.ecommerce.marketplace.application.quotation.FinalizeQuotationResult;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class FinalizeQuotationResponse {
    
    private Long quotationId;
    private String status;
    private Long orderId;
    private int acceptedItemCount;
    private int rejectedItemCount;
    private String message;
    
    public static FinalizeQuotationResponse from(FinalizeQuotationResult result) {
        return new FinalizeQuotationResponse(
                result.getQuotationId(),
                result.getStatus(),
                result.getOrderId(),
                result.getAcceptedItemCount(),
                result.getRejectedItemCount(),
                result.getMessage()
        );
    }
}
