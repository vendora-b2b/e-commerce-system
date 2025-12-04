package com.example.ecommerce.marketplace.application.quotation;

import java.util.List;

/**
 * Result of creating quotations.
 * Multiple quotations can be created if items belong to different suppliers.
 */
public class CreateQuotationResult {
    
    private final List<QuotationSummary> quotations;
    private final String message;
    
    public CreateQuotationResult(List<QuotationSummary> quotations, String message) {
        this.quotations = quotations;
        this.message = message;
    }
    
    public List<QuotationSummary> getQuotations() {
        return quotations;
    }
    
    public String getMessage() {
        return message;
    }
    
    public static class QuotationSummary {
        private final Long quotationId;
        private final String quotationNumber;
        private final Long supplierId;
        private final String supplierName;
        private final int itemCount;
        private final String status;
        
        public QuotationSummary(Long quotationId, String quotationNumber, Long supplierId,
                               String supplierName, int itemCount, String status) {
            this.quotationId = quotationId;
            this.quotationNumber = quotationNumber;
            this.supplierId = supplierId;
            this.supplierName = supplierName;
            this.itemCount = itemCount;
            this.status = status;
        }
        
        public Long getQuotationId() {
            return quotationId;
        }
        
        public String getQuotationNumber() {
            return quotationNumber;
        }
        
        public Long getSupplierId() {
            return supplierId;
        }
        
        public String getSupplierName() {
            return supplierName;
        }
        
        public int getItemCount() {
            return itemCount;
        }
        
        public String getStatus() {
            return status;
        }
    }
}
