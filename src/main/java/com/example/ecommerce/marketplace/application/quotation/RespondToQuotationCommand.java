package com.example.ecommerce.marketplace.application.quotation;

import com.example.ecommerce.marketplace.domain.quotation.Quotation;
import com.example.ecommerce.marketplace.domain.quotation.QuotationItemStatus;
import com.example.ecommerce.marketplace.domain.quotation.RetailerAction;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Command for supplier to respond to a quotation.
 */
public class RespondToQuotationCommand {
    
    private final Long quotationId;
    private final List<ItemResponse> items;
    private final LocalDateTime validUntil;
    private final String supplierNotes;
    private final String termsAndConditions;
    
    public RespondToQuotationCommand(Long quotationId, List<ItemResponse> items, 
                                    LocalDateTime validUntil, String supplierNotes, 
                                    String termsAndConditions) {
        this.quotationId = quotationId;
        this.items = items;
        this.validUntil = validUntil;
        this.supplierNotes = supplierNotes;
        this.termsAndConditions = termsAndConditions;
    }
    
    public Long getQuotationId() {
        return quotationId;
    }
    
    public List<ItemResponse> getItems() {
        return items;
    }
    
    public LocalDateTime getValidUntil() {
        return validUntil;
    }
    
    public String getSupplierNotes() {
        return supplierNotes;
    }
    
    public String getTermsAndConditions() {
        return termsAndConditions;
    }
    
    public static class ItemResponse {
        private final Long quotationItemId;
        private final QuotationItemStatus itemStatus;
        private final Integer offeredQuantity;
        private final Double offeredPrice;
        private final LocalDate offeredDeliveryDate;
        private final Integer leadTimeDays;
        private final String supplierNotes;
        private final String rejectionReason;
        
        public ItemResponse(Long quotationItemId, QuotationItemStatus itemStatus, 
                          Integer offeredQuantity, Double offeredPrice,
                          LocalDate offeredDeliveryDate, Integer leadTimeDays,
                          String supplierNotes, String rejectionReason) {
            this.quotationItemId = quotationItemId;
            this.itemStatus = itemStatus;
            this.offeredQuantity = offeredQuantity;
            this.offeredPrice = offeredPrice;
            this.offeredDeliveryDate = offeredDeliveryDate;
            this.leadTimeDays = leadTimeDays;
            this.supplierNotes = supplierNotes;
            this.rejectionReason = rejectionReason;
        }
        
        public Quotation.QuotationItemResponse toDomain() {
            return new Quotation.QuotationItemResponse(
                    quotationItemId, itemStatus, offeredQuantity, offeredPrice,
                    offeredDeliveryDate, leadTimeDays, supplierNotes, rejectionReason
            );
        }
        
        public Long getQuotationItemId() { return quotationItemId; }
        public QuotationItemStatus getItemStatus() { return itemStatus; }
        public Integer getOfferedQuantity() { return offeredQuantity; }
        public Double getOfferedPrice() { return offeredPrice; }
        public LocalDate getOfferedDeliveryDate() { return offeredDeliveryDate; }
        public Integer getLeadTimeDays() { return leadTimeDays; }
        public String getSupplierNotes() { return supplierNotes; }
        public String getRejectionReason() { return rejectionReason; }
    }
}
