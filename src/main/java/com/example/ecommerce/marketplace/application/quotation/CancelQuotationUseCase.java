package com.example.ecommerce.marketplace.application.quotation;

import com.example.ecommerce.marketplace.domain.quotation.Quotation;
import com.example.ecommerce.marketplace.domain.quotation.QuotationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Use case for cancelling a quotation.
 */
@Service
@RequiredArgsConstructor
public class CancelQuotationUseCase {
    
    private final QuotationRepository quotationRepository;
    
    @Transactional
    public Quotation execute(Long quotationId, String reason) {
        // Find quotation
        Quotation quotation = quotationRepository.findById(quotationId);
        if (quotation == null) {
            throw new IllegalArgumentException("Quotation not found");
        }
        
        // Cancel quotation
        quotation.cancel(reason);
        
        // Save and return
        return quotationRepository.save(quotation);
    }
}
