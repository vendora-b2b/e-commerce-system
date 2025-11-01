package com.example.ecommerce.marketplace.domain.quotation;

/**
 * Represents the possible states of a quotation offer in the system.
 */
public enum QuotationOfferStatus {
    /**
     * Offer is created but not yet submitted to retailer
     */
    DRAFT,
    
    /**
     * Offer has been submitted to retailer
     */
    SUBMITTED,
    
    /**
     * Offer has been accepted by the retailer
     */
    ACCEPTED,
    
    /**
     * Offer has been rejected by the retailer
     */
    REJECTED,
    
    /**
     * Offer has been withdrawn by the supplier
     */
    WITHDRAWN,
    
    /**
     * Offer has expired without being accepted
     */
    EXPIRED
}
