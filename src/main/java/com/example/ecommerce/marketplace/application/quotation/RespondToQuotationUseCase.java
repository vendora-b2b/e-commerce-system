package com.example.ecommerce.marketplace.application.quotation;

import com.example.ecommerce.marketplace.application.notification.NotificationService;
import com.example.ecommerce.marketplace.domain.quotation.Quotation;
import com.example.ecommerce.marketplace.domain.quotation.QuotationRepository;
import com.example.ecommerce.marketplace.domain.supplier.Supplier;
import com.example.ecommerce.marketplace.domain.supplier.SupplierRepository;
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
 * Use case for supplier to respond to a quotation.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class RespondToQuotationUseCase {
    
    private final QuotationRepository quotationRepository;
    private final UserRepository userRepository;
    private final SupplierRepository supplierRepository;
    private final NotificationService notificationService;
    
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
        
        // Save quotation
        Quotation savedQuotation = quotationRepository.save(quotation);
        
        // Notify retailer about quotation response
        notifyRetailerQuotationResponded(savedQuotation);
        
        return savedQuotation;
    }
    
    /**
     * Notifies the retailer that a supplier has responded to their quotation.
     */
    private void notifyRetailerQuotationResponded(Quotation quotation) {
        try {
            Optional<User> retailerUser = userRepository.findByEntityIdAndRole(
                quotation.getRetailerId(), UserRole.RETAILER);
            Optional<Supplier> supplier = supplierRepository.findById(quotation.getSupplierId());
            
            String supplierName = supplier.map(Supplier::getName).orElse("Supplier");
            
            if (retailerUser.isPresent()) {
                notificationService.notifyQuotationResponded(
                    retailerUser.get().getId(),
                    quotation.getId(),
                    supplierName
                );
                log.debug("Notification sent to retailer user {} for quotation response {}",
                    retailerUser.get().getId(), quotation.getQuotationNumber());
            } else {
                log.warn("Could not find retailer user for retailer ID {} to send notification",
                    quotation.getRetailerId());
            }
        } catch (Exception e) {
            log.warn("Failed to send quotation response notification to retailer: {}", e.getMessage());
        }
    }
}
