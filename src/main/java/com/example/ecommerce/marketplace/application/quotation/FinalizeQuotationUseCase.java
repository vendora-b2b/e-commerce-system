package com.example.ecommerce.marketplace.application.quotation;

import com.example.ecommerce.marketplace.application.notification.NotificationService;
import com.example.ecommerce.marketplace.domain.quotation.Quotation;
import com.example.ecommerce.marketplace.domain.quotation.QuotationRepository;
import com.example.ecommerce.marketplace.domain.quotation.RetailerAction;
import com.example.ecommerce.marketplace.domain.retailer.Retailer;
import com.example.ecommerce.marketplace.domain.retailer.RetailerRepository;
import com.example.ecommerce.marketplace.domain.user.User;
import com.example.ecommerce.marketplace.domain.user.UserRepository;
import com.example.ecommerce.marketplace.domain.user.UserRole;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Use case for retailer to finalize a quotation.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class FinalizeQuotationUseCase {
    
    private final QuotationRepository quotationRepository;
    private final UserRepository userRepository;
    private final RetailerRepository retailerRepository;
    private final NotificationService notificationService;
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
        
        // Notify supplier about quotation finalization
        notifySupplierQuotationFinalized(quotation, acceptedCount, rejectedCount);
        
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
    
    /**
     * Notifies the supplier about quotation finalization.
     */
    private void notifySupplierQuotationFinalized(Quotation quotation, int acceptedCount, int rejectedCount) {
        try {
            Optional<User> supplierUser = userRepository.findByEntityIdAndRole(
                quotation.getSupplierId(), UserRole.SUPPLIER);
            Optional<Retailer> retailer = retailerRepository.findById(quotation.getRetailerId());
            
            String retailerName = retailer.map(Retailer::getName).orElse("Retailer");
            
            if (supplierUser.isPresent()) {
                // Determine which notification to send based on accepted/rejected items
                if (acceptedCount > 0 && rejectedCount == 0) {
                    // All items accepted
                    notificationService.notifyQuotationAccepted(
                        supplierUser.get().getId(),
                        quotation.getId(),
                        retailerName
                    );
                } else if (acceptedCount == 0 && rejectedCount > 0) {
                    // All items rejected
                    notificationService.notifyQuotationRejected(
                        supplierUser.get().getId(),
                        quotation.getId(),
                        retailerName
                    );
                } else {
                    // Mixed - send finalized notification
                    notificationService.notifyQuotationFinalized(
                        supplierUser.get().getId(),
                        quotation.getId(),
                        quotation.getOrderId() != null
                    );
                }
                log.debug("Notification sent to supplier user {} for quotation finalization {}",
                    supplierUser.get().getId(), quotation.getQuotationNumber());
            } else {
                log.warn("Could not find supplier user for supplier ID {} to send notification",
                    quotation.getSupplierId());
            }
        } catch (Exception e) {
            log.warn("Failed to send quotation finalization notification to supplier: {}", e.getMessage());
        }
    }
}
