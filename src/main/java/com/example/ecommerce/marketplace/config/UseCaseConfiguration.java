package com.example.ecommerce.marketplace.config;

import com.example.ecommerce.marketplace.application.inventory.*;
import com.example.ecommerce.marketplace.application.quotation.CreateQuotationRequestUseCase;
import com.example.ecommerce.marketplace.application.quotation.SubmitQuotationOfferUseCase;
import com.example.ecommerce.marketplace.application.supplier.RegisterSupplierUseCase;
import com.example.ecommerce.marketplace.application.supplier.UpdateSupplierProfileUseCase;
import com.example.ecommerce.marketplace.domain.invetory.InventoryRepository;
import com.example.ecommerce.marketplace.domain.quotation.QuotationRepository;
import com.example.ecommerce.marketplace.domain.retailer.RetailerRepository;
import com.example.ecommerce.marketplace.domain.supplier.SupplierRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration class for wiring use cases with Spring.
 * This keeps framework concerns separate from business logic.
 */
@Configuration
public class UseCaseConfiguration {

    // ===== SUPPLIER USE CASES =====

    /**
     * Creates RegisterSupplierUseCase bean.
     */
    @Bean
    public RegisterSupplierUseCase registerSupplierUseCase(SupplierRepository supplierRepository) {
        return new RegisterSupplierUseCase(supplierRepository);
    }

    /**
     * Creates UpdateSupplierProfileUseCase bean.
     */
    @Bean
    public UpdateSupplierProfileUseCase updateSupplierProfileUseCase(SupplierRepository supplierRepository) {
        return new UpdateSupplierProfileUseCase(supplierRepository);
    }

    // ===== QUOTATION USE CASES =====

    /**
     * Creates CreateQuotationRequestUseCase bean.
     */
    @Bean
    public CreateQuotationRequestUseCase createQuotationRequestUseCase(
            QuotationRepository quotationRepository,
            RetailerRepository retailerRepository,
            SupplierRepository supplierRepository) {
        return new CreateQuotationRequestUseCase(quotationRepository, retailerRepository, supplierRepository);
    }

    /**
     * Creates SubmitQuotationOfferUseCase bean.
     */
    @Bean
    public SubmitQuotationOfferUseCase submitQuotationOfferUseCase(
            QuotationRepository quotationRepository,
            SupplierRepository supplierRepository) {
        return new SubmitQuotationOfferUseCase(quotationRepository, supplierRepository);
    }

    // ===== INVENTORY USE CASES =====

    /**
     * Creates CheckInventoryAvailabilityUseCase bean.
     */
    @Bean
    public CheckInventoryAvailabilityUseCase checkInventoryAvailabilityUseCase(
            InventoryRepository inventoryRepository) {
        return new CheckInventoryAvailabilityUseCase(inventoryRepository);
    }

    /**
     * Creates RestockInventoryUseCase bean.
     */
    @Bean
    public RestockInventoryUseCase restockInventoryUseCase(
            InventoryRepository inventoryRepository) {
        return new RestockInventoryUseCase(inventoryRepository);
    }

    /**
     * Creates ReserveInventoryUseCase bean.
     */
    @Bean
    public ReserveInventoryUseCase reserveInventoryUseCase(
            InventoryRepository inventoryRepository) {
        return new ReserveInventoryUseCase(inventoryRepository);
    }

    /**
     * Creates ReleaseInventoryUseCase bean.
     */
    @Bean
    public ReleaseInventoryUseCase releaseInventoryUseCase(
            InventoryRepository inventoryRepository) {
        return new ReleaseInventoryUseCase(inventoryRepository);
    }

    /**
     * Creates DeductInventoryUseCase bean.
     */
    @Bean
    public DeductInventoryUseCase deductInventoryUseCase(
            InventoryRepository inventoryRepository) {
        return new DeductInventoryUseCase(inventoryRepository);
    }
}
