package com.example.ecommerce.marketplace.application.quotation;

import com.example.ecommerce.marketplace.domain.quotation.Quotation;
import com.example.ecommerce.marketplace.domain.quotation.QuotationRepository;
import com.example.ecommerce.marketplace.domain.supplier.SupplierRepository;
import com.example.ecommerce.marketplace.domain.retailer.RetailerRepository;
import com.example.ecommerce.marketplace.domain.product.ProductRepository;
import com.example.ecommerce.marketplace.domain.product.ProductVariantRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Use case for retrieving a quotation by ID.
 */
@Service
@RequiredArgsConstructor
public class GetQuotationUseCase {
    
    private final QuotationRepository quotationRepository;
    private final SupplierRepository supplierRepository;
    private final RetailerRepository retailerRepository;
    private final ProductRepository productRepository;
    private final ProductVariantRepository variantRepository;
    
    @Transactional(readOnly = true)
    public Quotation execute(Long quotationId) {
        Quotation quotation = quotationRepository.findById(quotationId);
        if (quotation == null) {
            throw new IllegalArgumentException("Quotation not found with ID: " + quotationId);
        }
        return quotation;
    }
    
    public SupplierRepository getSupplierRepository() {
        return supplierRepository;
    }
    
    public RetailerRepository getRetailerRepository() {
        return retailerRepository;
    }
    
    public ProductRepository getProductRepository() {
        return productRepository;
    }
    
    public ProductVariantRepository getVariantRepository() {
        return variantRepository;
    }
}
