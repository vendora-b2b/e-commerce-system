package com.example.ecommerce.marketplace.application.quotation;

import com.example.ecommerce.marketplace.domain.quotation.RetailerAction;

import java.util.List;

/**
 * Command for retailer to finalize a quotation.
 */
public class FinalizeQuotationCommand {
    
    private final Long quotationId;
    private final List<ItemDecision> items;
    private final boolean createOrder;
    private final String notes;
    
    public FinalizeQuotationCommand(Long quotationId, List<ItemDecision> items, 
                                   boolean createOrder, String notes) {
        this.quotationId = quotationId;
        this.items = items;
        this.createOrder = createOrder;
        this.notes = notes;
    }
    
    public Long getQuotationId() {
        return quotationId;
    }
    
    public List<ItemDecision> getItems() {
        return items;
    }
    
    public boolean isCreateOrder() {
        return createOrder;
    }
    
    public String getNotes() {
        return notes;
    }
    
    public static class ItemDecision {
        private final Long quotationItemId;
        private final RetailerAction retailerAction;
        
        public ItemDecision(Long quotationItemId, RetailerAction retailerAction) {
            this.quotationItemId = quotationItemId;
            this.retailerAction = retailerAction;
        }
        
        public Long getQuotationItemId() {
            return quotationItemId;
        }
        
        public RetailerAction getRetailerAction() {
            return retailerAction;
        }
    }
}
