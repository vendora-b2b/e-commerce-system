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
public class RespondToQuotationResponse {
    
    private Long quotationId;
    private String status;
    private LocalDateTime respondedAt;
    private String message;
    
    public static RespondToQuotationResponse from(Quotation quotation) {
        return new RespondToQuotationResponse(
                quotation.getId(),
                quotation.getStatus().name(),
                quotation.getRespondedAt(),
                "Quotation response submitted successfully. Awaiting retailer review."
        );
    }
}
