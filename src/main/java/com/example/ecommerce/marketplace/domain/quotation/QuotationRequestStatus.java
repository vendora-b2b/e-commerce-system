package com.example.ecommerce.marketplace.domain.quotation;

/**
 * Represents the possible states of a quotation request in the system.
 */
public enum QuotationRequestStatus {
    /**
     * Request is created but not yet submitted to suppliers
     */
    DRAFT,
    
    /**
     * Request has been submitted to suppliers and awaiting offers
     */
    PENDING,
    
    /**
     * At least one offers has been received for this request
     */
    OFFERS_RECEIVED,
    
    /**
     * An offer has been accepted for this request
     */
    OFFER_ACCEPTED,
    
    /**
     * Request has been cancelled by the retailer
     */
    CANCELLED,
    
    /**
     * Request has expired without any accepted offers
     */
    EXPIRED
}