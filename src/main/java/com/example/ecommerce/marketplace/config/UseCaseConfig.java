package com.example.ecommerce.marketplace.config;

import com.example.ecommerce.marketplace.application.inventory.UpdateInventoryUseCase;
import com.example.ecommerce.marketplace.application.product.*;
import com.example.ecommerce.marketplace.application.quotation.*;
import com.example.ecommerce.marketplace.application.supplier.RegisterSupplierUseCase;
import com.example.ecommerce.marketplace.application.supplier.UpdateSupplierProfileUseCase;
import com.example.ecommerce.marketplace.domain.inventory.InventoryRepository;
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
public class UseCaseConfig {

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

    /**
     * Creates UpdateProductPriceTierUseCase bean.
     */
    @Bean
    public UpdateProductPriceTierUseCase updateProductPriceTierUseCase(
            ProductRepository productRepository) {
        return new UpdateProductPriceTierUseCase(productRepository);
    }

    /**
     * Creates DeleteProductPriceTierUseCase bean.
     */
    @Bean
    public DeleteProductPriceTierUseCase deleteProductPriceTierUseCase(
            ProductRepository productRepository) {
        return new DeleteProductPriceTierUseCase(productRepository);
    }

    // ===== INVENTORY USE CASES =====

    /**
     * Creates UpdateInventoryUseCase bean.
     */
    @Bean
    public UpdateInventoryUseCase updateInventoryUseCase(InventoryRepository inventoryRepository) {
        return new UpdateInventoryUseCase(inventoryRepository);
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
            SupplierRepository supplierRepository,
            ProductVariantRepository productVariantRepository) {
        return new SubmitQuotationOfferUseCase(quotationRepository, supplierRepository, productVariantRepository);
    }

    /**
     * Creates ListQuotationRequestsUseCase bean.
     */
    @Bean
    public ListQuotationRequestsUseCase listQuotationRequestsUseCase(
            QuotationRepository quotationRepository) {
        return new ListQuotationRequestsUseCase(quotationRepository);
    }

    /**
     * Creates GetQuotationRequestUseCase bean.
     */
    @Bean
    public GetQuotationRequestUseCase getQuotationRequestUseCase(
            QuotationRepository quotationRepository) {
        return new GetQuotationRequestUseCase(quotationRepository);
    }

    /**
     * Creates UpdateQuotationRequestStatusUseCase bean.
     */
    @Bean
    public UpdateQuotationRequestStatusUseCase updateQuotationRequestStatusUseCase(
            QuotationRepository quotationRepository) {
        return new UpdateQuotationRequestStatusUseCase(quotationRepository);
    }

    /**
     * Creates ListQuotationOffersUseCase bean.
     */
    @Bean
    public ListQuotationOffersUseCase listQuotationOffersUseCase(
            QuotationRepository quotationRepository) {
        return new ListQuotationOffersUseCase(quotationRepository);
    }

    /**
     * Creates GetQuotationOfferUseCase bean.
     */
    @Bean
    public GetQuotationOfferUseCase getQuotationOfferUseCase(
            QuotationRepository quotationRepository) {
        return new GetQuotationOfferUseCase(quotationRepository);
    }

    /**
     * Creates UpdateQuotationOfferStatusUseCase bean.
     */
    @Bean
    public UpdateQuotationOfferStatusUseCase updateQuotationOfferStatusUseCase(
            QuotationRepository quotationRepository) {
        return new UpdateQuotationOfferStatusUseCase(quotationRepository);
    }
}