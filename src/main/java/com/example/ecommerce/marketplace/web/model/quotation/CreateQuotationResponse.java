package com.example.ecommerce.marketplace.web.model.quotation;

import com.example.ecommerce.marketplace.application.quotation.CreateQuotationResult;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;
import java.util.stream.Collectors;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CreateQuotationResponse {
    
    private List<QuotationSummary> quotations;
    private String message;
    
    public static CreateQuotationResponse from(CreateQuotationResult result) {
        List<QuotationSummary> summaries = result.getQuotations().stream()
                .map(q -> new QuotationSummary(
                        q.getQuotationId(),
                        q.getQuotationNumber(),
                        q.getSupplierId(),
                        q.getSupplierName(),
                        q.getItemCount(),
                        q.getStatus()
                ))
                .collect(Collectors.toList());
        
        return new CreateQuotationResponse(summaries, result.getMessage());
    }
    
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class QuotationSummary {
        private Long quotationId;
        private String quotationNumber;
        private Long supplierId;
        private String supplierName;
        private int itemCount;
        private String status;
    }
}
