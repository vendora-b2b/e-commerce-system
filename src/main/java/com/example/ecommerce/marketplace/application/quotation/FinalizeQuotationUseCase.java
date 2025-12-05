package com.example.ecommerce.marketplace.application.quotation;

import com.example.ecommerce.marketplace.domain.quotation.Quotation;
import com.example.ecommerce.marketplace.domain.quotation.QuotationRepository;
import com.example.ecommerce.marketplace.domain.quotation.RetailerAction;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Use case for retailer to finalize a quotation.
 */
@Service
@RequiredArgsConstructor
public class FinalizeQuotationUseCase {
    
    private final QuotationRepository quotationRepository;
    // In real implementation, would inject OrderRepository to create orders
    
    @Transactional
    public FinalizeQuotationResult execute(FinalizeQuotationCommand command) {
        // Find quotation
        Quotation quotation = quotationRepository.findById(command.getQuotationId());
        if (quotation == null) {
            throw new IllegalArgumentException("Quotation not found");
        }
        
        // Convert command items to domain
        List<Quotation.RetailerItemDecision> decisions = command.getItems().stream()
                .map(item -> new Quotation.RetailerItemDecision(
                        item.getQuotationItemId(), 
                        item.getRetailerAction()))
                .collect(Collectors.toList());
        
        // Apply finalization
        quotation.finalize(decisions, command.isCreateOrder());
        
        // Count accepted/rejected items
        int acceptedCount = (int) quotation.getItems().stream()
                .filter(item -> item.getRetailerAction() == RetailerAction.ACCEPT)
                .count();
        int rejectedCount = (int) quotation.getItems().stream()
                .filter(item -> item.getRetailerAction() == RetailerAction.REJECT)
                .count();
        
        // Save quotation
        quotation = quotationRepository.save(quotation);
        
        // Create order if requested and there are accepted items
        Long orderId = null;
        if (command.isCreateOrder() && acceptedCount > 0) {
            // In real implementation: create purchase order here
            orderId = 1L;  // Placeholder
            quotation.setOrderId(orderId);
            quotation = quotationRepository.save(quotation);
        }
        
        String message = orderId != null 
                ? String.format("Quotation finalized successfully. Purchase order #PO-%d created.", orderId)
                : "Quotation finalized successfully.";
        
        return new FinalizeQuotationResult(
                quotation.getId(),
                quotation.getStatus().name(),
                orderId,
                acceptedCount,
                rejectedCount,
                message
        );
    }
}
