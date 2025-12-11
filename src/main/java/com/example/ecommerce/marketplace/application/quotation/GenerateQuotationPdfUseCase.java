package com.example.ecommerce.marketplace.application.quotation;

import com.example.ecommerce.marketplace.domain.quotation.Quotation;
import com.example.ecommerce.marketplace.domain.quotation.QuotationRepository;
import com.example.ecommerce.marketplace.domain.quotation.QuotationStatus;
import com.example.ecommerce.marketplace.infrastructure.pdf.QuotationPdfService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Use case for generating quotation PDF documents.
 * Only finalized quotations can be exported to PDF.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class GenerateQuotationPdfUseCase {

    private final QuotationRepository quotationRepository;
    private final QuotationPdfService pdfService;

    /**
     * Generates a PDF document for a finalized quotation.
     * 
     * @param quotationId the quotation ID
     * @param userRole the role of requesting user ("RETAILER" or "SUPPLIER")
     * @return PDF content as byte array
     * @throws IllegalArgumentException if quotation not found or not finalized
     */
    @Transactional(readOnly = true)
    public byte[] execute(Long quotationId, String userRole) {
        log.info("Generating PDF for quotation {} with role {}", quotationId, userRole);
        
        // 1. Fetch quotation
        Quotation quotation = quotationRepository.findById(quotationId);
        if (quotation == null) {
            throw new IllegalArgumentException("Quotation not found: " + quotationId);
        }
        
        // 2. Validate status
        if (quotation.getStatus() != QuotationStatus.FINALIZED) {
            throw new IllegalArgumentException(
                String.format("Cannot generate PDF for quotation with status %s. Only FINALIZED quotations can be exported.", 
                    quotation.getStatus()));
        }
        
        // 3. Validate user role
        if (!"RETAILER".equals(userRole) && !"SUPPLIER".equals(userRole)) {
            throw new IllegalArgumentException("Invalid user role: " + userRole + ". Must be RETAILER or SUPPLIER.");
        }
        
        // 4. Generate PDF
        log.info("Quotation {} validated. Generating PDF for role {}", quotationId, userRole);
        return pdfService.generateQuotationPdf(quotation, userRole);
    }
}
