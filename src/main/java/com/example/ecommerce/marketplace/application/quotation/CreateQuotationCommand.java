package com.example.ecommerce.marketplace.application.quotation;

import java.time.LocalDate;
import java.util.List;

/**
 * Command for creating a new quotation.
 */
public class CreateQuotationCommand {
    
    private final List<QuotationItemRequest> items;
    private final String notes;
    
    public CreateQuotationCommand(List<QuotationItemRequest> items, String notes) {
        this.items = items;
        this.notes = notes;
    }
    
    public List<QuotationItemRequest> getItems() {
        return items;
    }
    
    public String getNotes() {
        return notes;
    }
    
    public static class QuotationItemRequest {
        private final Long variantId;
        private final Integer requestedQuantity;
        private final Double targetPrice;
        private final LocalDate deliveryDate;
        private final String notes;
        
        public QuotationItemRequest(Long variantId, Integer requestedQuantity, Double targetPrice,
                                   LocalDate deliveryDate, String notes) {
            this.variantId = variantId;
            this.requestedQuantity = requestedQuantity;
            this.targetPrice = targetPrice;
            this.deliveryDate = deliveryDate;
            this.notes = notes;
        }
        
        public Long getVariantId() {
            return variantId;
        }
        
        public Integer getRequestedQuantity() {
            return requestedQuantity;
        }
        
        public Double getTargetPrice() {
            return targetPrice;
        }
        
        public LocalDate getDeliveryDate() {
            return deliveryDate;
        }
        
        public String getNotes() {
            return notes;
        }
    }
}
