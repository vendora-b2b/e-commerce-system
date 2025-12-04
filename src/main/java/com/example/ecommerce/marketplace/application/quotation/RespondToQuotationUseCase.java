package com.example.ecommerce.marketplace.application.quotation;

import com.example.ecommerce.marketplace.domain.quotation.Quotation;
import com.example.ecommerce.marketplace.domain.quotation.QuotationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Use case for supplier to respond to a quotation.
 */
@Service
@RequiredArgsConstructor
public class RespondToQuotationUseCase {
    
    private final QuotationRepository quotationRepository;
    
    @Transactional
    public Quotation execute(RespondToQuotationCommand command) {
        // Find quotation
        Quotation quotation = quotationRepository.findById(command.getQuotationId());
        if (quotation == null) {
            throw new IllegalArgumentException("Quotation not found");
        }
        
        // Convert command items to domain
        List<Quotation.QuotationItemResponse> itemResponses = command.getItems().stream()
                .map(RespondToQuotationCommand.ItemResponse::toDomain)
                .collect(Collectors.toList());
        
        // Apply response
        quotation.respond(itemResponses, command.getSupplierNotes(), 
                         command.getTermsAndConditions(), command.getValidUntil());
        
        // Save and return
        return quotationRepository.save(quotation);
    }
}
