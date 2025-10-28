package com.example.ecommerce.marketplace.infrastructure.product;

import com.example.ecommerce.marketplace.domain.product.Product;
import com.example.ecommerce.marketplace.domain.product.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for Product repository infrastructure layer.
 * Tests JPA entity configuration, database constraints, and entity-domain mapping.
 * Uses H2 in-memory database configured in application-test.properties.
 * 
 * Tests the complete Product infrastructure implementation including:
 * - ProductEntity JPA entity
 * - JpaProductRepository Spring Data repository
 * - ProductRepositoryImpl adapter
 * - Entity-domain mapping and cascading operations
 */
@SpringBootTest
@Transactional
@TestPropertySource(locations = "classpath:application-test.properties")
@DisplayName("Product Repository Integration Tests")
class ProductRepositoryIntegrationTest {

    @Autowired
    private ProductRepository productRepository;

    private Product testProduct;

    @BeforeEach
    void setUp() {
        testProduct = new Product();
        testProduct.setId(null);
        testProduct.setSku("PROD-TEST-001");
        testProduct.setName("Integration Test Product");
        testProduct.setDescription("This is a test product for integration testing");
        testProduct.setCategory("Electronics");
        testProduct.setSupplierId(1L);
        testProduct.setBasePrice(99.99);
        testProduct.setMinimumOrderQuantity(10);
        testProduct.setUnit("pcs");
        testProduct.setImages(Arrays.asList("img1.jpg", "img2.jpg"));
        testProduct.setStatus("ACTIVE");
    }

    // ===== Save and Retrieve Tests =====

    @Test
    @DisplayName("Should save product and generate ID")
    void testSave_GeneratesId() {
        // When
        Product saved = productRepository.save(testProduct);

        // Then
        assertNotNull(saved.getId(), "ID should be generated");
        assertTrue(saved.getId() > 0, "ID should be positive");
    }

    @Test
    @DisplayName("Should save and retrieve product by ID")
    void testSaveAndFindById_Success() {
        // Given
        Product saved = productRepository.save(testProduct);

        // When
        Optional<Product> found = productRepository.findById(saved.getId());

        // Then
        assertTrue(found.isPresent(), "Product should be found");
        assertEquals(saved.getId(), found.get().getId());
        assertEquals("PROD-TEST-001", found.get().getSku());
        assertEquals("Integration Test Product", found.get().getName());
        assertEquals("This is a test product for integration testing", found.get().getDescription());
        assertEquals("Electronics", found.get().getCategory());
        assertEquals(1L, found.get().getSupplierId());
        assertEquals(99.99, found.get().getBasePrice());
        assertEquals(10, found.get().getMinimumOrderQuantity());
        assertEquals("pcs", found.get().getUnit());
        assertEquals(2, found.get().getImages().size());
        assertEquals("ACTIVE", found.get().getStatus());
    }

    @Test
    @DisplayName("Should return empty optional when product not found")
    void testFindById_NotFound() {
        // When
        Optional<Product> found = productRepository.findById(999L);

        // Then
        assertFalse(found.isPresent(), "Should return empty optional");
    }

    @Test
    @DisplayName("Should save product with null optional fields")
    void testSave_WithNullOptionalFields() {
        // Given
        Product product = new Product();
        product.setId(null);
        product.setSku("MIN-PROD-001");
        product.setName("Minimal Product");
        product.setDescription(null);  // description can be null
        product.setCategory("General");
        product.setSupplierId(1L);
        product.setBasePrice(10.0);
        product.setMinimumOrderQuantity(1);
        product.setUnit("pcs");
        product.setStatus("ACTIVE");

        // When
        Product saved = productRepository.save(product);

        // Then
        assertNotNull(saved.getId());
        assertEquals("Minimal Product", saved.getName());
        assertNull(saved.getDescription());
        assertTrue(saved.getImages().isEmpty());
        assertFalse(saved.hasPriceTiers());
        assertFalse(saved.hasVariants());
    }

    @Test
    @DisplayName("Should save product with images")
    void testSave_WithImages() {
        // Given
        testProduct.setImages(Arrays.asList("image1.jpg", "image2.jpg", "image3.jpg"));

        // When
        Product saved = productRepository.save(testProduct);
        Optional<Product> found = productRepository.findById(saved.getId());

        // Then
        assertTrue(found.isPresent());
        assertEquals(3, found.get().getImages().size());
        assertTrue(found.get().getImages().contains("image1.jpg"));
        assertTrue(found.get().getImages().contains("image2.jpg"));
        assertTrue(found.get().getImages().contains("image3.jpg"));
    }

    @Test
    @DisplayName("Should save product with price tiers")
    void testSave_WithPriceTiers() {
        // Given
        Product.PriceTier tier1 = new Product.PriceTier(null, 10, 49, 90.0, 10.0);
        Product.PriceTier tier2 = new Product.PriceTier(null, 50, 99, 80.0, 20.0);
        testProduct.addPriceTier(tier1);
        testProduct.addPriceTier(tier2);

        // When
        Product saved = productRepository.save(testProduct);
        Optional<Product> found = productRepository.findById(saved.getId());

        // Then
        assertTrue(found.isPresent());
        assertTrue(found.get().hasPriceTiers());
        assertEquals(2, found.get().getPriceTiers().size());
    }

    @Test
    @DisplayName("Should save product with variants")
    void testSave_WithVariants() {
        // Given
        Product.ProductVariant variant1 = new Product.ProductVariant(null, "Color", "Red", 0.0, null);
        Product.ProductVariant variant2 = new Product.ProductVariant(null, "Size", "Large", 5.0, null);
        testProduct.addVariant(variant1);
        testProduct.addVariant(variant2);

        // When
        Product saved = productRepository.save(testProduct);
        Optional<Product> found = productRepository.findById(saved.getId());

        // Then
        assertTrue(found.isPresent());
        assertTrue(found.get().hasVariants());
        assertEquals(2, found.get().getVariants().size());
    }

    // ===== Unique Constraint Tests =====

    @Test
    @DisplayName("Should enforce unique SKU constraint")
    void testSave_DuplicateSku_ThrowsException() {
        // Given - Save first product
        productRepository.save(testProduct);

        // When - Try to save another product with same SKU
        Product duplicate = new Product();
        duplicate.setSku("PROD-TEST-001");  // Same SKU
        duplicate.setName("Different Product");
        duplicate.setCategory("Different Category");
        duplicate.setSupplierId(2L);
        duplicate.setBasePrice(50.0);
        duplicate.setMinimumOrderQuantity(5);
        duplicate.setUnit("box");
        duplicate.setStatus("ACTIVE");

        // Then - Should throw constraint violation
        assertThrows(DataIntegrityViolationException.class, () -> {
            productRepository.save(duplicate);
            // Force flush to trigger constraint check
        });
    }

    // ===== Update Tests =====

    @Test
    @DisplayName("Should update existing product")
    void testUpdate_Success() {
        // Given - Save initial product
        Product saved = productRepository.save(testProduct);
        Long productId = saved.getId();

        // When - Update the product
        saved.updateProductInfo(
            "Updated Product Name",
            "Updated description",
            "Updated Category",
            "kg"
        );
        saved.updateBasePrice(150.0);
        saved.updateMinimumOrderQuantity(20);

        Product updated = productRepository.save(saved);

        // Then - Verify updates persisted
        assertEquals(productId, updated.getId(), "ID should not change");
        assertEquals("Updated Product Name", updated.getName());
        assertEquals("Updated description", updated.getDescription());
        assertEquals("Updated Category", updated.getCategory());
        assertEquals("kg", updated.getUnit());
        assertEquals(150.0, updated.getBasePrice());
        assertEquals(20, updated.getMinimumOrderQuantity());
    }

    @Test
    @DisplayName("Should update product status")
    void testUpdate_Status() {
        // Given
        Product saved = productRepository.save(testProduct);
        assertEquals("ACTIVE", saved.getStatus());

        // When
        saved.deactivate();
        Product updated = productRepository.save(saved);

        // Then
        assertEquals("INACTIVE", updated.getStatus());
        assertFalse(updated.isActive());
    }

    // ===== Query Method Tests =====

    @Test
    @DisplayName("Should find product by SKU")
    void testFindBySku_Success() {
        // Given
        productRepository.save(testProduct);

        // When
        Optional<Product> found = productRepository.findBySku("PROD-TEST-001");

        // Then
        assertTrue(found.isPresent());
        assertEquals("Integration Test Product", found.get().getName());
    }

    @Test
    @DisplayName("Should return empty when SKU not found")
    void testFindBySku_NotFound() {
        // When
        Optional<Product> found = productRepository.findBySku("NONEXISTENT-SKU");

        // Then
        assertFalse(found.isPresent());
    }

    @Test
    @DisplayName("Should find products by supplier ID")
    void testFindBySupplierId() {
        // Given
        productRepository.save(testProduct);

        Product anotherProduct = new Product();
        anotherProduct.setSku("PROD-TEST-002");
        anotherProduct.setName("Another Product");
        anotherProduct.setCategory("Hardware");
        anotherProduct.setSupplierId(1L);  // Same supplier
        anotherProduct.setBasePrice(50.0);
        anotherProduct.setMinimumOrderQuantity(5);
        anotherProduct.setUnit("pcs");
        anotherProduct.setStatus("ACTIVE");
        productRepository.save(anotherProduct);

        Product differentSupplier = new Product();
        differentSupplier.setSku("PROD-TEST-003");
        differentSupplier.setName("Different Supplier Product");
        differentSupplier.setCategory("Hardware");
        differentSupplier.setSupplierId(2L);  // Different supplier
        differentSupplier.setBasePrice(30.0);
        differentSupplier.setMinimumOrderQuantity(3);
        differentSupplier.setUnit("pcs");
        differentSupplier.setStatus("ACTIVE");
        productRepository.save(differentSupplier);

        // When
        List<Product> supplier1Products = productRepository.findBySupplierId(1L);
        List<Product> supplier2Products = productRepository.findBySupplierId(2L);

        // Then
        assertEquals(2, supplier1Products.size());
        assertEquals(1, supplier2Products.size());
        assertEquals("Different Supplier Product", supplier2Products.get(0).getName());
    }

    @Test
    @DisplayName("Should find products by category")
    void testFindByCategory() {
        // Given
        productRepository.save(testProduct);  // Electronics

        Product hardwareProduct = new Product();
        hardwareProduct.setSku("PROD-TEST-002");
        hardwareProduct.setName("Hardware Product");
        hardwareProduct.setCategory("Hardware");
        hardwareProduct.setSupplierId(1L);
        hardwareProduct.setBasePrice(75.0);
        hardwareProduct.setMinimumOrderQuantity(8);
        hardwareProduct.setUnit("pcs");
        hardwareProduct.setStatus("ACTIVE");
        productRepository.save(hardwareProduct);

        // When
        List<Product> electronics = productRepository.findByCategory("Electronics");
        List<Product> hardware = productRepository.findByCategory("Hardware");

        // Then
        assertEquals(1, electronics.size());
        assertEquals("Integration Test Product", electronics.get(0).getName());
        assertEquals(1, hardware.size());
        assertEquals("Hardware Product", hardware.get(0).getName());
    }

    @Test
    @DisplayName("Should find products by status")
    void testFindByStatus() {
        // Given
        productRepository.save(testProduct);  // ACTIVE

        Product inactiveProduct = new Product();
        inactiveProduct.setSku("PROD-TEST-002");
        inactiveProduct.setName("Inactive Product");
        inactiveProduct.setCategory("General");
        inactiveProduct.setSupplierId(1L);
        inactiveProduct.setBasePrice(25.0);
        inactiveProduct.setMinimumOrderQuantity(5);
        inactiveProduct.setUnit("pcs");
        inactiveProduct.setStatus("INACTIVE");
        productRepository.save(inactiveProduct);

        // When
        List<Product> activeProducts = productRepository.findByStatus("ACTIVE");
        List<Product> inactiveProducts = productRepository.findByStatus("INACTIVE");

        // Then
        assertEquals(1, activeProducts.size());
        assertEquals("Integration Test Product", activeProducts.get(0).getName());
        assertEquals(1, inactiveProducts.size());
        assertEquals("Inactive Product", inactiveProducts.get(0).getName());
    }

    @Test
    @DisplayName("Should check if SKU exists")
    void testExistsBySku() {
        // Given
        productRepository.save(testProduct);

        // Then
        assertTrue(productRepository.existsBySku("PROD-TEST-001"));
        assertFalse(productRepository.existsBySku("NONEXISTENT-SKU"));
    }

    @Test
    @DisplayName("Should find products by price range")
    void testFindByPriceRange() {
        // Given
        productRepository.save(testProduct);  // 99.99

        Product cheapProduct = new Product();
        cheapProduct.setSku("CHEAP-001");
        cheapProduct.setName("Cheap Product");
        cheapProduct.setCategory("General");
        cheapProduct.setSupplierId(1L);
        cheapProduct.setBasePrice(10.0);
        cheapProduct.setMinimumOrderQuantity(1);
        cheapProduct.setUnit("pcs");
        cheapProduct.setStatus("ACTIVE");
        productRepository.save(cheapProduct);

        Product expensiveProduct = new Product();
        expensiveProduct.setSku("EXPENSIVE-001");
        expensiveProduct.setName("Expensive Product");
        expensiveProduct.setCategory("General");
        expensiveProduct.setSupplierId(1L);
        expensiveProduct.setBasePrice(500.0);
        expensiveProduct.setMinimumOrderQuantity(1);
        expensiveProduct.setUnit("pcs");
        expensiveProduct.setStatus("ACTIVE");
        productRepository.save(expensiveProduct);

        // When
        List<Product> midRange = productRepository.findByBasePriceBetween(50.0, 150.0);
        List<Product> allRange = productRepository.findByBasePriceBetween(0.0, 1000.0);

        // Then
        assertEquals(1, midRange.size());
        assertEquals("Integration Test Product", midRange.get(0).getName());
        assertEquals(3, allRange.size());
    }

    // ===== Delete Tests =====

    @Test
    @DisplayName("Should delete product by ID")
    void testDeleteById() {
        // Given
        Product saved = productRepository.save(testProduct);
        Long productId = saved.getId();

        // When
        productRepository.deleteById(productId);

        // Then
        Optional<Product> found = productRepository.findById(productId);
        assertFalse(found.isPresent(), "Product should be deleted");
    }

    // ===== Count Tests =====

    @Test
    @DisplayName("Should count all products")
    void testCount() {
        // Given
        assertEquals(0, productRepository.count());

        productRepository.save(testProduct);

        Product another = new Product();
        another.setSku("PROD-TEST-002");
        another.setName("Another Product");
        another.setCategory("General");
        another.setSupplierId(1L);
        another.setBasePrice(50.0);
        another.setMinimumOrderQuantity(5);
        another.setUnit("pcs");
        another.setStatus("ACTIVE");
        productRepository.save(another);

        // Then
        assertEquals(2, productRepository.count());
    }

    @Test
    @DisplayName("Should count products by supplier")
    void testCountBySupplierId() {
        // Given
        productRepository.save(testProduct);  // supplierId = 1

        Product supplier2Product = new Product();
        supplier2Product.setSku("PROD-TEST-002");
        supplier2Product.setName("Supplier 2 Product");
        supplier2Product.setCategory("General");
        supplier2Product.setSupplierId(2L);
        supplier2Product.setBasePrice(30.0);
        supplier2Product.setMinimumOrderQuantity(3);
        supplier2Product.setUnit("pcs");
        supplier2Product.setStatus("ACTIVE");
        productRepository.save(supplier2Product);

        // Then
        assertEquals(1, productRepository.countBySupplierId(1L));
        assertEquals(1, productRepository.countBySupplierId(2L));
        assertEquals(0, productRepository.countBySupplierId(999L));
    }

    @Test
    @DisplayName("Should count products by status")
    void testCountByStatus() {
        // Given
        productRepository.save(testProduct);  // ACTIVE

        Product inactive = new Product();
        inactive.setSku("PROD-TEST-002");
        inactive.setName("Inactive");
        inactive.setCategory("General");
        inactive.setSupplierId(1L);
        inactive.setBasePrice(20.0);
        inactive.setMinimumOrderQuantity(2);
        inactive.setUnit("pcs");
        inactive.setStatus("INACTIVE");
        productRepository.save(inactive);

        // Then
        assertEquals(1, productRepository.countByStatus("ACTIVE"));
        assertEquals(1, productRepository.countByStatus("INACTIVE"));
        assertEquals(0, productRepository.countByStatus("DISCONTINUED"));
    }

    @Test
    @DisplayName("Should count products by category")
    void testCountByCategory() {
        // Given
        productRepository.save(testProduct);  // Electronics

        Product hardware = new Product();
        hardware.setSku("PROD-TEST-002");
        hardware.setName("Hardware Product");
        hardware.setCategory("Hardware");
        hardware.setSupplierId(1L);
        hardware.setBasePrice(40.0);
        hardware.setMinimumOrderQuantity(4);
        hardware.setUnit("pcs");
        hardware.setStatus("ACTIVE");
        productRepository.save(hardware);

        // Then
        assertEquals(1, productRepository.countByCategory("Electronics"));
        assertEquals(1, productRepository.countByCategory("Hardware"));
        assertEquals(0, productRepository.countByCategory("Software"));
    }

    // ===== Entity-Domain Mapping Tests =====

    @Test
    @DisplayName("Should correctly map all domain fields to entity")
    void testDomainToEntityMapping() {
        // Given - Create domain object with all fields
        Product domain = new Product();
        domain.setSku("MAPPING-TEST-001");
        domain.setName("Mapping Test Product");
        domain.setDescription("Testing entity mapping with all fields");
        domain.setCategory("Test Category");
        domain.setSupplierId(5L);
        domain.setBasePrice(123.45);
        domain.setMinimumOrderQuantity(15);
        domain.setUnit("kg");
        domain.setImages(Arrays.asList("map1.jpg", "map2.jpg"));
        domain.setStatus("ACTIVE");

        // When - Save (triggers domain → entity mapping)
        Product saved = productRepository.save(domain);

        // Then - Retrieve and verify all fields
        Product retrieved = productRepository.findById(saved.getId()).get();

        assertNotNull(retrieved.getId());
        assertEquals("MAPPING-TEST-001", retrieved.getSku());
        assertEquals("Mapping Test Product", retrieved.getName());
        assertEquals("Testing entity mapping with all fields", retrieved.getDescription());
        assertEquals("Test Category", retrieved.getCategory());
        assertEquals(5L, retrieved.getSupplierId());
        assertEquals(123.45, retrieved.getBasePrice());
        assertEquals(15, retrieved.getMinimumOrderQuantity());
        assertEquals("kg", retrieved.getUnit());
        assertEquals(2, retrieved.getImages().size());
        assertEquals("ACTIVE", retrieved.getStatus());
    }

    @Test
    @DisplayName("Should preserve domain object state after save")
    void testDomainObjectUnchangedAfterSave() {
        // Given
        String originalSku = testProduct.getSku();
        String originalName = testProduct.getName();
        Double originalPrice = testProduct.getBasePrice();

        // When
        Product saved = productRepository.save(testProduct);

        // Then - Original domain object should be unchanged
        assertEquals(originalSku, testProduct.getSku());
        assertEquals(originalName, testProduct.getName());
        assertEquals(originalPrice, testProduct.getBasePrice());

        // But returned object should have ID
        assertNotNull(saved.getId());
    }

    @Test
    @DisplayName("Should handle cascading saves for price tiers")
    void testCascadePriceTiers() {
        // Given
        Product.PriceTier tier = new Product.PriceTier(null, 100, 199, 85.0, 15.0);
        testProduct.addPriceTier(tier);

        // When
        Product saved = productRepository.save(testProduct);
        Product retrieved = productRepository.findById(saved.getId()).get();

        // Then
        assertTrue(retrieved.hasPriceTiers());
        assertEquals(1, retrieved.getPriceTiers().size());
        Product.PriceTier retrievedTier = retrieved.getPriceTiers().get(0);
        assertNotNull(retrievedTier.getId());
        assertEquals(100, retrievedTier.getMinQuantity());
        assertEquals(199, retrievedTier.getMaxQuantity());
        assertEquals(85.0, retrievedTier.getPricePerUnit());
    }

    @Test
    @DisplayName("Should handle cascading saves for variants")
    void testCascadeVariants() {
        // Given
        Product.ProductVariant variant = new Product.ProductVariant(null, "Material", "Steel", 10.0, null);
        testProduct.addVariant(variant);

        // When
        Product saved = productRepository.save(testProduct);
        Product retrieved = productRepository.findById(saved.getId()).get();

        // Then
        assertTrue(retrieved.hasVariants());
        assertEquals(1, retrieved.getVariants().size());
        Product.ProductVariant retrievedVariant = retrieved.getVariants().get(0);
        assertNotNull(retrievedVariant.getId());
        assertEquals("Material", retrievedVariant.getVariantName());
        assertEquals("Steel", retrievedVariant.getVariantValue());
        assertEquals(10.0, retrievedVariant.getPriceAdjustment());
    }

}
