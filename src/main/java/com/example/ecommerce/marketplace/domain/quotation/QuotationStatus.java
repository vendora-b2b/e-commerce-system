package com.example.ecommerce.marketplace.domain.quotation;

/**
 * Represents the possible states of a quotation in the system.
 * A quotation tracks communication between one retailer and one supplier.
 */
public enum QuotationStatus {
    /**
     * Quotation has been created and waiting for supplier to respond
     */
    PENDING_SUPPLIER,
    
    /**
     * Supplier has responded, waiting for retailer to finalize
     */
    PENDING_RETAILER,
    
    /**
     * Retailer has reviewed and finalized the quotation (may include order creation)
     */
    FINALIZED,
    
    /**
     * Either party cancelled the quotation before finalization
     */
    CANCELLED,
    
    /**
     * Quotation has exceeded validUntil date without finalization
     */
    EXPIRED
}
