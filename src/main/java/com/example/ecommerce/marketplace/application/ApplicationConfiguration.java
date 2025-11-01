package com.example.ecommerce.marketplace.application;

import com.example.ecommerce.marketplace.application.quotation.CreateQuotationRequestUseCase;
import com.example.ecommerce.marketplace.application.quotation.SubmitQuotationOfferUseCase;
import com.example.ecommerce.marketplace.domain.quotation.QuotationRepository;
import com.example.ecommerce.marketplace.domain.retailer.RetailerRepository;
import com.example.ecommerce.marketplace.domain.supplier.SupplierRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Application layer configuration for all marketplace use cases.
 * This configuration is shared across all team members and domains.
 * Add your use case beans here following the same pattern.
 */
@Configuration
public class ApplicationConfiguration {

    // ===== QUOTATION USE CASES =====
    
    @Bean
    public CreateQuotationRequestUseCase createQuotationRequestUseCase(
            QuotationRepository quotationRepository,
            RetailerRepository retailerRepository,
            SupplierRepository supplierRepository) {
        return new CreateQuotationRequestUseCase(quotationRepository, retailerRepository, supplierRepository);
    }

    @Bean
    public SubmitQuotationOfferUseCase submitQuotationOfferUseCase(
            QuotationRepository quotationRepository,
            SupplierRepository supplierRepository) {
        return new SubmitQuotationOfferUseCase(quotationRepository, supplierRepository);
    }

    // ===== TODO: ADD OTHER DOMAIN USE CASES HERE =====
    // 
    // Example pattern for Product and Order use cases:

    
    //
    // @Bean
    // public CreateProductUseCase createProductUseCase(
    //         ProductRepository productRepository,
    //         SupplierRepository supplierRepository) {
    //     return new CreateProductUseCase(productRepository, supplierRepository);
    // }
    //
    // @Bean
    // public PlaceOrderUseCase placeOrderUseCase(
    //         OrderRepository orderRepository,
    //         RetailerRepository retailerRepository,
    //         SupplierRepository supplierRepository,
    //         ProductRepository productRepository) {
    //     return new PlaceOrderUseCase(orderRepository, retailerRepository, supplierRepository, productRepository);
    // }
}