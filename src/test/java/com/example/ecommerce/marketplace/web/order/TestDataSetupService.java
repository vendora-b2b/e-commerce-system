package com.example.ecommerce.marketplace.web.order;

import com.example.ecommerce.marketplace.domain.product.Product;
import com.example.ecommerce.marketplace.domain.product.ProductRepository;
import com.example.ecommerce.marketplace.domain.retailer.Retailer;
import com.example.ecommerce.marketplace.domain.retailer.RetailerLoyaltyTier;
import com.example.ecommerce.marketplace.domain.retailer.RetailerRepository;
import com.example.ecommerce.marketplace.domain.supplier.Supplier;
import com.example.ecommerce.marketplace.domain.supplier.SupplierRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * Service for setting up test data in integration tests.
 * Uses REQUIRES_NEW propagation to ensure data is committed
 * in a separate transaction before tests run.
 */
@Service
public class TestDataSetupService {

    private final SupplierRepository supplierRepository;
    private final RetailerRepository retailerRepository;
    private final ProductRepository productRepository;

    public TestDataSetupService(SupplierRepository supplierRepository,
                                RetailerRepository retailerRepository,
                                ProductRepository productRepository) {
        this.supplierRepository = supplierRepository;
        this.retailerRepository = retailerRepository;
        this.productRepository = productRepository;
    }

    /**
     * Creates test data (supplier, retailer, products) in a new transaction
     * that will be committed before the method returns.
     */
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public TestData createTestData() {
        // Create test supplier
        Supplier supplier = new Supplier(
            null,
            "Test Supplier Inc",
            "test-supplier@test.com",
            "555-0100",
            "123 Supplier St",
            null,
            "Test supplier for integration tests",
            "LIC-TEST-999",
            null,
            false
        );
        Long supplierId = supplierRepository.save(supplier).getId();

        // Create test retailer
        Retailer retailer = new Retailer(
            null,
            "Test Retailer Corp",
            "test-retailer@test.com",
            "555-0200",
            "456 Retailer Ave",
            null,
            "Test retailer for integration tests",
            "BUS-TEST-999",
            RetailerLoyaltyTier.BRONZE,
            0.0,
            0.0,
            0
        );
        Long retailerId = retailerRepository.save(retailer).getId();

        // Create test products
        Product product1 = new Product(
            null,
            "TEST-PROD-100",
            "Test Product A",
            "Test product for integration tests",
            1L,
            supplierId,
            29.99,
            1,
            "PIECE",
            null,
            null,
            null,
            "ACTIVE",
            LocalDateTime.now(),
            LocalDateTime.now()
        );
        Long productId1 = productRepository.save(product1).getId();

        Product product2 = new Product(
            null,
            "TEST-PROD-101",
            "Test Product B",
            "Another test product",
            1L,
            supplierId,
            49.99,
            1,
            "PIECE",
            null,
            null,
            null,
            "ACTIVE",
            LocalDateTime.now(),
            LocalDateTime.now()
        );
        Long productId2 = productRepository.save(product2).getId();

        return new TestData(supplierId, retailerId, productId1, productId2);
    }

    /**
     * Record to hold test data IDs.
     */
    public record TestData(Long supplierId, Long retailerId, Long productId1, Long productId2) {
    }
}
