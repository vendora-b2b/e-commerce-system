package com.example.ecommerce.marketplace.config;

import com.example.ecommerce.marketplace.application.supplier.RegisterSupplierUseCase;
import com.example.ecommerce.marketplace.application.supplier.UpdateSupplierProfileUseCase;
import com.example.ecommerce.marketplace.domain.supplier.SupplierRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration class for wiring use cases with Spring.
 * This keeps framework concerns separate from business logic.
 */
@Configuration
public class UseCaseConfiguration {

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
}
