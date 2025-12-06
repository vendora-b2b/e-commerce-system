package com.example.ecommerce.marketplace.domain.quotation;

/**
 * Represents the retailer's final decision on a quotation item.
 */
public enum RetailerAction {
    /**
     * Retailer accepts the supplier's offer for this item
     */
    ACCEPT,
    
    /**
     * Retailer declines the supplier's offer
     */
    REJECT,
    
    /**
     * Retailer acknowledges a rejected item (no action needed)
     */
    ACKNOWLEDGE
}
