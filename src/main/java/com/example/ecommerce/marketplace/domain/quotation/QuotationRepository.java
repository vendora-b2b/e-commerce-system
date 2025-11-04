package com.example.ecommerce.marketplace.domain.quotation;

import java.util.List;

/**
 * Repository interface for managing quotation-related persistence operations.
 */
public interface QuotationRepository {
    
    /**
     * Save a quotation request to the repository
     * @param request the request to save
     * @return the saved request with generated ID
     */
    QuotationRequest saveQuotationRequest(QuotationRequest request);
    
    /**
     * Save a quotation offer to the repository
     * @param offer the offer to save
     * @return the saved offer with generated ID
     */
    QuotationOffer saveQuotationOffer(QuotationOffer offer);
    
    /**
     * Find a quotation request by its ID
     * @param id the request ID
     * @return the found request or null if not found
     */
    QuotationRequest findRequestById(Long id);
    
    /**
     * Find a quotation offer by its ID
     * @param id the offer ID
     * @return the found offer or null if not found
     */
    QuotationOffer findOfferById(Long id);
    
    /**
     * Find all quotation requests for a specific retailer
     * @param retailerId the retailer ID
     * @return list of quotation requests
     */
    List<QuotationRequest> findRequestsByRetailerId(Long retailerId);
    
    /**
     * Find all quotation offers for a specific request
     * @param requestId the request ID
     * @return list of quotation offers
     */
    List<QuotationOffer> findOffersByRequestId(Long requestId);
    
    /**
     * Find all quotation offers submitted by a specific supplier
     * @param supplierId the supplier ID
     * @return list of quotation offers
     */
    List<QuotationOffer> findOffersBySupplierId(Long supplierId);
}
