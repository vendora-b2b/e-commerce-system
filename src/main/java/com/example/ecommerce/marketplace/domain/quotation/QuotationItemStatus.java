package com.example.ecommerce.marketplace.domain.quotation;

/**
 * Represents the possible states of a quotation item in the system.
 * Each item in a quotation can have its own status tracking.
 */
public enum QuotationItemStatus {
    /**
     * Supplier has not yet responded to this item
     */
    PENDING,
    
    /**
     * Supplier accepts the full requested quantity at offered price
     */
    ACCEPTED,
    
    /**
     * Supplier can fulfill only partial quantity or different terms
     */
    PARTIAL,
    
    /**
     * Supplier cannot fulfill this item
     */
    REJECTED
}
