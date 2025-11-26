package com.example.ecommerce.marketplace.domain.quotation;

/**
 * Represents the possible states of a quotation offer in the system.
 */
public enum QuotationOfferStatus {
    /**
     * Offer has been submitted to retailer and awaiting response
     */
    PENDING,
    
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
