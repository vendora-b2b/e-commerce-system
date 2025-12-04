package com.example.ecommerce.marketplace.application.quotation;

import com.example.ecommerce.marketplace.domain.quotation.Quotation;
import com.example.ecommerce.marketplace.domain.quotation.QuotationRepository;
import com.example.ecommerce.marketplace.domain.quotation.QuotationStatus;
import com.example.ecommerce.marketplace.domain.supplier.SupplierRepository;
import com.example.ecommerce.marketplace.domain.retailer.RetailerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Use case for listing quotations with pagination and filtering.
 */
@Service
@RequiredArgsConstructor
public class ListQuotationsUseCase {
    
    private final QuotationRepository quotationRepository;
    private final SupplierRepository supplierRepository;
    private final RetailerRepository retailerRepository;
    
    @Transactional(readOnly = true)
    public Page<Quotation> execute(String role, Long retailerId, Long supplierId, 
                                   QuotationStatus status, Pageable pageable) {
        // Validate role
        if (!"retailer".equalsIgnoreCase(role) && !"supplier".equalsIgnoreCase(role)) {
            throw new IllegalArgumentException("Role must be 'retailer' or 'supplier'");
        }
        
        // Apply role-based filtering
        Long filterRetailerId = "retailer".equalsIgnoreCase(role) ? retailerId : null;
        Long filterSupplierId = "supplier".equalsIgnoreCase(role) ? supplierId : null;
        
        return quotationRepository.findByFilter(filterRetailerId, filterSupplierId, status, pageable);
    }
    
    public SupplierRepository getSupplierRepository() {
        return supplierRepository;
    }
    
    public RetailerRepository getRetailerRepository() {
        return retailerRepository;
    }
}
