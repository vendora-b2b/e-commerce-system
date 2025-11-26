package com.example.ecommerce.marketplace.domain.quotation;

/**
 * Represents the possible states of a quotation request in the system.
 */
public enum QuotationRequestStatus {
    /**
     * Request has been submitted to suppliers and awaiting responses
     */
    PENDING,
    
    /**
     * Request has been successfully received by suppliers
     */
    REQUEST_RECEIVED,
    
    /**
     * Request has been cancelled by the retailer
     */
    CANCELLED,
    
    /**
     * Request has expired without any accepted offers
     */
    EXPIRED
}
