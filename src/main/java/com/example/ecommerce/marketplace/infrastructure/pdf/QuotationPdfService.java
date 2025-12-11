package com.example.ecommerce.marketplace.infrastructure.pdf;

import com.example.ecommerce.marketplace.domain.quotation.Quotation;

/**
 * Service interface for generating quotation PDF documents.
 */
public interface QuotationPdfService {
    
    /**
     * Generates a PDF document for a finalized quotation.
     * 
     * @param quotation the quotation entity
     * @param userRole the role of user requesting PDF ("RETAILER" or "SUPPLIER")
     * @return PDF content as byte array
     */
    byte[] generateQuotationPdf(Quotation quotation, String userRole);
}
