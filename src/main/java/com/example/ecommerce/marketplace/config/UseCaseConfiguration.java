package com.example.ecommerce.marketplace.config;

import com.example.ecommerce.marketplace.application.inventory.*;
import com.example.ecommerce.marketplace.application.product.*;
import com.example.ecommerce.marketplace.application.quotation.CreateQuotationRequestUseCase;
import com.example.ecommerce.marketplace.application.quotation.SubmitQuotationOfferUseCase;
import com.example.ecommerce.marketplace.application.supplier.RegisterSupplierUseCase;
import com.example.ecommerce.marketplace.application.supplier.UpdateSupplierProfileUseCase;
import com.example.ecommerce.marketplace.domain.invetory.InventoryRepository;
import com.example.ecommerce.marketplace.domain.order.OrderRepository;
import com.example.ecommerce.marketplace.domain.product.ProductRepository;
import com.example.ecommerce.marketplace.domain.product.ProductVariantRepository;
import com.example.ecommerce.marketplace.domain.quotation.QuotationRepository;
import com.example.ecommerce.marketplace.domain.retailer.RetailerRepository;
import com.example.ecommerce.marketplace.domain.supplier.SupplierRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration class for wiring use cases with Spring.
 * This keeps framework concerns separate from business logic.
 * Note: Use cases with @Service annotation are auto-detected and don't need beans here.
 */
@Configuration
public class UseCaseConfiguration {

    // ===== PRODUCT USE CASES =====

    /**
     * Creates CreateProductUseCase bean.
     */
    @Bean
    public CreateProductUseCase createProductUseCase(
            ProductRepository productRepository,
            SupplierRepository supplierRepository) {
        return new CreateProductUseCase(productRepository, supplierRepository);
    }

    /**
     * Creates UpdateProductUseCase bean.
     */
    @Bean
    public UpdateProductUseCase updateProductUseCase(ProductRepository productRepository) {
        return new UpdateProductUseCase(productRepository);
    }

    /**
     * Creates DeleteProductUseCase bean.
     */
    @Bean
    public DeleteProductUseCase deleteProductUseCase(
            ProductRepository productRepository,
            OrderRepository orderRepository) {
        return new DeleteProductUseCase(productRepository, orderRepository);
    }

    /**
     * Creates ListProductVariantsUseCase bean.
     */
    @Bean
    public ListProductVariantsUseCase listProductVariantsUseCase(
            ProductRepository productRepository,
            ProductVariantRepository productVariantRepository) {
        return new ListProductVariantsUseCase(productRepository, productVariantRepository);
    }

    /**
     * Creates CreateProductVariantUseCase bean.
     */
    @Bean
    public CreateProductVariantUseCase createProductVariantUseCase(
            ProductRepository productRepository,
            ProductVariantRepository productVariantRepository,
            InventoryRepository inventoryRepository) {
        return new CreateProductVariantUseCase(productRepository, productVariantRepository, inventoryRepository);
    }

    /**
     * Creates UpdateProductVariantUseCase bean.
     */
    @Bean
    public UpdateProductVariantUseCase updateProductVariantUseCase(
            ProductRepository productRepository,
            ProductVariantRepository productVariantRepository) {
        return new UpdateProductVariantUseCase(productRepository, productVariantRepository);
    }

    /**
     * Creates DeleteProductVariantUseCase bean.
     */
    @Bean
    public DeleteProductVariantUseCase deleteProductVariantUseCase(
            ProductRepository productRepository,
            ProductVariantRepository productVariantRepository,
            OrderRepository orderRepository,
            InventoryRepository inventoryRepository) {
        return new DeleteProductVariantUseCase(productRepository, productVariantRepository, orderRepository, inventoryRepository);
    }

    /**
     * Creates ListProductPriceTiersUseCase bean.
     */
    @Bean
    public ListProductPriceTiersUseCase listProductPriceTiersUseCase(
            ProductRepository productRepository) {
        return new ListProductPriceTiersUseCase(productRepository);
    }

    /**
     * Creates CreateProductPriceTierUseCase bean.
     */
    @Bean
    public CreateProductPriceTierUseCase createProductPriceTierUseCase(
            ProductRepository productRepository) {
        return new CreateProductPriceTierUseCase(productRepository);
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
     * Creates ReserveInventoryUseCase bean.
     */
    @Bean
    public ReserveInventoryUseCase reserveInventoryUseCase(InventoryRepository inventoryRepository) {
        return new ReserveInventoryUseCase(inventoryRepository);
    }

    /**
     * Creates ReleaseInventoryUseCase bean.
     */
    @Bean
    public ReleaseInventoryUseCase releaseInventoryUseCase(InventoryRepository inventoryRepository) {
        return new ReleaseInventoryUseCase(inventoryRepository);
    }

    /**
     * Creates DeductInventoryUseCase bean.
     */
    @Bean
    public DeductInventoryUseCase deductInventoryUseCase(InventoryRepository inventoryRepository) {
        return new DeductInventoryUseCase(inventoryRepository);
    }

    /**
     * Creates RestockInventoryUseCase bean.
     */
    @Bean
    public RestockInventoryUseCase restockInventoryUseCase(InventoryRepository inventoryRepository) {
        return new RestockInventoryUseCase(inventoryRepository);
    }

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
}