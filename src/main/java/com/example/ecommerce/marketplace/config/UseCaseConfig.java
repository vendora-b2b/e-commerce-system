package com.example.ecommerce.marketplace.config;

import com.example.ecommerce.marketplace.application.ai.DeleteProductFromAiUseCase;
import com.example.ecommerce.marketplace.application.ai.IngestProductUseCase;
import com.example.ecommerce.marketplace.application.ai.IngestSupplierUseCase;
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
     * Includes IngestProductUseCase for AI integration.
     */
    @Bean
    public CreateProductUseCase createProductUseCase(
            ProductRepository productRepository,
            SupplierRepository supplierRepository,
            IngestProductUseCase ingestProductUseCase) {
        return new CreateProductUseCase(productRepository, supplierRepository, ingestProductUseCase);
    }

    /**
     * Creates UpdateProductUseCase bean.
     * Includes IngestProductUseCase for AI re-indexing on updates.
     */
    @Bean
    public UpdateProductUseCase updateProductUseCase(
            ProductRepository productRepository,
            IngestProductUseCase ingestProductUseCase) {
        return new UpdateProductUseCase(productRepository, ingestProductUseCase);
    }

    /**
     * Creates DeleteProductUseCase bean.
     * Includes DeleteProductFromAiUseCase for AI cleanup on deletion.
     */
    @Bean
    public DeleteProductUseCase deleteProductUseCase(
            ProductRepository productRepository,
            OrderRepository orderRepository,
            DeleteProductFromAiUseCase deleteProductFromAiUseCase) {
        return new DeleteProductUseCase(productRepository, orderRepository, deleteProductFromAiUseCase);
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
     * Includes IngestSupplierUseCase for AI integration on registration.
     */
    @Bean
    public RegisterSupplierUseCase registerSupplierUseCase(
            SupplierRepository supplierRepository,
            IngestSupplierUseCase ingestSupplierUseCase) {
        return new RegisterSupplierUseCase(supplierRepository, ingestSupplierUseCase);
    }

    /**
     * Creates UpdateSupplierProfileUseCase bean.
     * Includes IngestSupplierUseCase for AI integration on profile updates.
     */
    @Bean
    public UpdateSupplierProfileUseCase updateSupplierProfileUseCase(
            SupplierRepository supplierRepository,
            IngestSupplierUseCase ingestSupplierUseCase) {
        return new UpdateSupplierProfileUseCase(supplierRepository, ingestSupplierUseCase);
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