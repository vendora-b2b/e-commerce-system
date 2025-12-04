package com.example.ecommerce.marketplace.web.model.quotation;

import com.example.ecommerce.marketplace.domain.quotation.Quotation;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CancelQuotationResponse {
    
    private Long quotationId;
    private String status;
    private LocalDateTime cancelledAt;
    private String message;
    
    public static CancelQuotationResponse from(Quotation quotation) {
        return new CancelQuotationResponse(
                quotation.getId(),
                quotation.getStatus().name(),
                quotation.getCancelledAt(),
                "Quotation cancelled successfully"
        );
    }
}
