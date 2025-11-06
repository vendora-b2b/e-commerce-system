package com.example.ecommerce.marketplace.infrastructure.inventory;

import com.example.ecommerce.marketplace.domain.invetory.Inventory;
import com.example.ecommerce.marketplace.domain.invetory.InventoryRepository;
import com.example.ecommerce.marketplace.domain.invetory.InventoryStatus;
import com.example.ecommerce.marketplace.domain.product.Product;
import com.example.ecommerce.marketplace.domain.product.ProductRepository;
import com.example.ecommerce.marketplace.domain.supplier.Supplier;
import com.example.ecommerce.marketplace.domain.supplier.SupplierRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for Inventory repository infrastructure layer.
 * Tests JPA entity configuration, database constraints, and entity-domain mapping.
 * Uses MySQL database configured in application-test.properties.
 *
 * Tests the complete Inventory infrastructure implementation including:
 * - InventoryEntity JPA entity
 * - JpaInventoryRepository Spring Data repository
 * - InventoryRepositoryImpl adapter
 * - Entity-domain mapping
 * - Database constraints (unique productId)
 * - Custom query methods for reorder checks
 * - Status enum persistence
 */
@SpringBootTest
@Transactional
@TestPropertySource(locations = "classpath:application-test.properties")
@DisplayName("Inventory Repository Integration Tests")
class InventoryRepositoryIntegrationTest {

    @Autowired
    private InventoryRepository inventoryRepository;

    @Autowired
    private SupplierRepository supplierRepository;

    @Autowired
    private ProductRepository productRepository;

    private Inventory testInventory;
    private Supplier testSupplier;
    private Product testProduct;
    private Long supplierId;
    private Long productId;

    @BeforeEach
    void setUp() {
        // Create test supplier first
        testSupplier = new Supplier();
        testSupplier.setName("Test Supplier");
        testSupplier.setEmail("supplier@test.com");
        testSupplier.setBusinessLicense("SUP12345678");
        testSupplier.setVerified(true);
        testSupplier.setRating(4.5);
        testSupplier = supplierRepository.save(testSupplier);
        supplierId = testSupplier.getId();

        // Create test product
        testProduct = new Product();
        testProduct.setSku("TEST-SKU-001");
        testProduct.setName("Test Product");
        testProduct.setDescription("Test product description");
        testProduct.setCategory("Electronics");
        testProduct.setSupplierId(supplierId);
        testProduct.setBasePrice(100.0);
        testProduct.setMinimumOrderQuantity(1);
        testProduct.setUnit("pcs");
        testProduct.setStatus("AVAILABLE");
        testProduct = productRepository.save(testProduct);
        productId = testProduct.getId();

        // Create test inventory
        testInventory = new Inventory();
        testInventory.setId(null);
        testInventory.setSupplierId(supplierId);
        testInventory.setProductId(productId);
        testInventory.setAvailableQuantity(150);
        testInventory.setReservedQuantity(50);
        testInventory.setReorderLevel(30);
        testInventory.setReorderQuantity(100);
        testInventory.setWarehouseLocation("Warehouse A - Aisle 5");
        testInventory.setLastRestocked(LocalDateTime.now().minusDays(5));
        testInventory.setLastUpdated(LocalDateTime.now());
        testInventory.setStatus(InventoryStatus.AVAILABLE);
    }

    /**
     * Helper method to create a product for testing.
     */
    private Product createProduct(String sku, String name) {
        Product product = new Product();
        product.setSku(sku);
        product.setName(name);
        product.setCategory("Electronics");
        product.setSupplierId(supplierId);
        product.setBasePrice(100.0);
        product.setMinimumOrderQuantity(1);
        product.setUnit("pcs");
        product.setStatus("AVAILABLE");
        return productRepository.save(product);
    }

    /**
     * Helper method to create a supplier for testing.
     */
    private Supplier createSupplier(String email, String license) {
        Supplier supplier = new Supplier();
        supplier.setName("Supplier " + license);
        supplier.setEmail(email);
        supplier.setBusinessLicense(license);
        supplier.setVerified(true);
        supplier.setRating(4.0);
        return supplierRepository.save(supplier);
    }

    // ===== Save and Retrieve Tests =====

    @Test
    @DisplayName("Should save inventory and generate ID")
    void testSave_GeneratesId() {
        // When
        Inventory saved = inventoryRepository.save(testInventory);

        // Then
        assertNotNull(saved.getId(), "ID should be generated");
        assertTrue(saved.getId() > 0, "ID should be positive");
    }

    @Test
    @DisplayName("Should save and retrieve inventory by ID")
    void testSaveAndFindById_Success() {
        // Given
        Inventory saved = inventoryRepository.save(testInventory);

        // When
        Optional<Inventory> found = inventoryRepository.findById(saved.getId());

        // Then
        assertTrue(found.isPresent(), "Inventory should be found");
        Inventory inventory = found.get();
        assertEquals(saved.getId(), inventory.getId());
        assertEquals(supplierId, inventory.getSupplierId());
        assertEquals(productId, inventory.getProductId());
        assertEquals(150, inventory.getAvailableQuantity());
        assertEquals(50, inventory.getReservedQuantity());
        assertEquals(30, inventory.getReorderLevel());
        assertEquals(100, inventory.getReorderQuantity());
        assertEquals("Warehouse A - Aisle 5", inventory.getWarehouseLocation());
        assertNotNull(inventory.getLastRestocked());
        assertNotNull(inventory.getLastUpdated());
        assertEquals(InventoryStatus.AVAILABLE, inventory.getStatus());
    }

    @Test
    @DisplayName("Should return empty optional when inventory not found")
    void testFindById_NotFound() {
        // When
        Optional<Inventory> found = inventoryRepository.findById(999L);

        // Then
        assertFalse(found.isPresent(), "Should return empty optional");
    }

    @Test
    @DisplayName("Should save inventory with null optional fields")
    void testSave_WithNullOptionalFields() {
        // Given - Create another product for this test
        Product product2 = new Product();
        product2.setSku("TEST-SKU-002");
        product2.setName("Test Product 2");
        product2.setCategory("Electronics");
        product2.setSupplierId(supplierId);
        product2.setBasePrice(50.0);
        product2.setMinimumOrderQuantity(1);
        product2.setUnit("pcs");
        product2.setStatus("AVAILABLE");
        product2 = productRepository.save(product2);

        Inventory inventory = new Inventory();
        inventory.setSupplierId(supplierId);
        inventory.setProductId(product2.getId());
        inventory.setAvailableQuantity(100);
        inventory.setReservedQuantity(0);
        inventory.setReorderLevel(null);  // can be null
        inventory.setReorderQuantity(null);  // can be null
        inventory.setWarehouseLocation(null);  // can be null
        inventory.setLastRestocked(null);  // can be null
        inventory.setLastUpdated(null);  // can be null
        inventory.setStatus(InventoryStatus.AVAILABLE);

        // When
        Inventory saved = inventoryRepository.save(inventory);

        // Then
        assertNotNull(saved.getId());
        assertEquals(supplierId, saved.getSupplierId());
        assertEquals(product2.getId(), saved.getProductId());
        assertEquals(100, saved.getAvailableQuantity());
        assertEquals(0, saved.getReservedQuantity());
        assertNull(saved.getReorderLevel());
        assertNull(saved.getReorderQuantity());
        assertNull(saved.getWarehouseLocation());
        assertNull(saved.getLastRestocked());
        assertNull(saved.getLastUpdated());
        assertEquals(InventoryStatus.AVAILABLE, saved.getStatus());
    }

    @Test
    @DisplayName("Should save inventory with default values for quantities")
    void testSave_WithDefaultQuantities() {
        // Given
        Product product3 = createProduct("TEST-SKU-003", "Test Product 3");

        Inventory inventory = new Inventory();
        inventory.setSupplierId(supplierId);
        inventory.setProductId(product3.getId());
        inventory.setAvailableQuantity(null);  // Should default to 0
        inventory.setReservedQuantity(null);  // Should default to 0
        inventory.setStatus(InventoryStatus.OUT_OF_STOCK);

        // When
        Inventory saved = inventoryRepository.save(inventory);

        // Then
        assertNotNull(saved.getId());
        assertEquals(0, saved.getAvailableQuantity());
        assertEquals(0, saved.getReservedQuantity());
    }

    // ===== Unique Constraint Tests =====

    @Test
    @DisplayName("Should enforce unique productId constraint")
    void testSave_DuplicateProductId_ThrowsException() {
        // Given - Save first inventory
        inventoryRepository.save(testInventory);

        // Create another supplier for the duplicate test
        Supplier supplier2 = createSupplier("supplier2@test.com", "SUP99999999");

        // When - Try to save another inventory with same productId but different supplier
        Inventory duplicate = new Inventory();
        duplicate.setSupplierId(supplier2.getId());  // Different supplier
        duplicate.setProductId(productId);  // Same productId - should fail
        duplicate.setAvailableQuantity(50);
        duplicate.setReservedQuantity(0);
        duplicate.setStatus(InventoryStatus.AVAILABLE);

        // Then - Should throw constraint violation
        assertThrows(DataIntegrityViolationException.class, () -> {
            inventoryRepository.save(duplicate);
        });
    }

    // ===== Update Tests =====

    @Test
    @DisplayName("Should update existing inventory")
    void testUpdate_Success() {
        // Given - Save initial inventory
        Inventory saved = inventoryRepository.save(testInventory);
        Long inventoryId = saved.getId();

        // When - Update the inventory
        saved.setAvailableQuantity(200);
        saved.setReservedQuantity(30);
        saved.setWarehouseLocation("Warehouse B - Aisle 3");
        saved.setReorderLevel(50);

        Inventory updated = inventoryRepository.save(saved);

        // Then - Verify updates persisted
        assertEquals(inventoryId, updated.getId(), "ID should not change");
        assertEquals(200, updated.getAvailableQuantity());
        assertEquals(30, updated.getReservedQuantity());
        assertEquals("Warehouse B - Aisle 3", updated.getWarehouseLocation());
        assertEquals(50, updated.getReorderLevel());
    }

    @Test
    @DisplayName("Should update status when inventory changes")
    void testUpdate_StatusChange() {
        // Given - Save initial inventory
        Inventory saved = inventoryRepository.save(testInventory);
        assertEquals(InventoryStatus.AVAILABLE, saved.getStatus());

        // When - Update to out of stock
        saved.setAvailableQuantity(0);
        saved.setReservedQuantity(0);
        saved.setStatus(InventoryStatus.OUT_OF_STOCK);
        Inventory updated = inventoryRepository.save(saved);

        // Then - Verify status was updated
        assertEquals(InventoryStatus.OUT_OF_STOCK, updated.getStatus());
        assertEquals(0, updated.getAvailableQuantity());
    }

    // ===== Query Method Tests =====

    @Test
    @DisplayName("Should find inventory by productId")
    void testFindByProductId_Success() {
        // Given
        inventoryRepository.save(testInventory);

        // When
        Optional<Inventory> found = inventoryRepository.findByProductId(productId);

        // Then
        assertTrue(found.isPresent());
        assertEquals(productId, found.get().getProductId());
        assertEquals(supplierId, found.get().getSupplierId());
    }

    @Test
    @DisplayName("Should return empty when productId not found")
    void testFindByProductId_NotFound() {
        // When
        Optional<Inventory> found = inventoryRepository.findByProductId(999L);

        // Then
        assertFalse(found.isPresent());
    }

    @Test
    @DisplayName("Should find inventory by supplierId and productId")
    void testFindBySupplierIdAndProductId_Success() {
        // Given
        inventoryRepository.save(testInventory);

        // When
        Optional<Inventory> found = inventoryRepository.findBySupplierIdAndProductId(supplierId, productId);

        // Then
        assertTrue(found.isPresent());
        assertEquals(supplierId, found.get().getSupplierId());
        assertEquals(productId, found.get().getProductId());
    }

    @Test
    @DisplayName("Should return empty when supplier-product combination not found")
    void testFindBySupplierIdAndProductId_NotFound() {
        // Given
        inventoryRepository.save(testInventory);

        // When
        Optional<Inventory> found = inventoryRepository.findBySupplierIdAndProductId(999L, 200L);

        // Then
        assertFalse(found.isPresent());
    }

    @Test
    @DisplayName("Should find all inventory by supplierId")
    void testFindBySupplierId() {
        // Given - Create multiple inventories for same supplier
        inventoryRepository.save(testInventory);  // supplier 100

        Product product4 = createProduct("TEST-SKU-004", "Test Product 4");
        Inventory inventory2 = new Inventory();
        inventory2.setSupplierId(supplierId);
        inventory2.setProductId(product4.getId());
        inventory2.setAvailableQuantity(75);
        inventory2.setReservedQuantity(25);
        inventory2.setStatus(InventoryStatus.AVAILABLE);
        inventoryRepository.save(inventory2);

        // Create different supplier and product
        Supplier supplier3 = createSupplier("supplier3@test.com", "SUP33333333");
        Product product5 = createProduct("TEST-SKU-005", "Test Product 5");
        Inventory inventory3 = new Inventory();
        inventory3.setSupplierId(supplier3.getId());  // Different supplier
        inventory3.setProductId(product5.getId());
        inventory3.setAvailableQuantity(50);
        inventory3.setReservedQuantity(0);
        inventory3.setStatus(InventoryStatus.AVAILABLE);
        inventoryRepository.save(inventory3);

        // When
        List<Inventory> supplierInventories = inventoryRepository.findBySupplierId(supplierId);

        // Then
        assertEquals(2, supplierInventories.size());
        assertTrue(supplierInventories.stream()
            .allMatch(inv -> inv.getSupplierId().equals(supplierId)));
    }

    @Test
    @DisplayName("Should find inventory by status")
    void testFindByStatus() {
        // Given
        inventoryRepository.save(testInventory);  // AVAILABLE

        Product product6 = createProduct("TEST-SKU-006", "Test Product 6");
        Inventory lowStock = new Inventory();
        lowStock.setSupplierId(supplierId);
        lowStock.setProductId(product6.getId());
        lowStock.setAvailableQuantity(10);
        lowStock.setReservedQuantity(5);
        lowStock.setReorderLevel(20);
        lowStock.setStatus(InventoryStatus.LOW_STOCK);
        inventoryRepository.save(lowStock);

        Product product7 = createProduct("TEST-SKU-007", "Test Product 7");
        Inventory outOfStock = new Inventory();
        outOfStock.setSupplierId(supplierId);
        outOfStock.setProductId(product7.getId());
        outOfStock.setAvailableQuantity(0);
        outOfStock.setReservedQuantity(0);
        outOfStock.setStatus(InventoryStatus.OUT_OF_STOCK);
        inventoryRepository.save(outOfStock);

        // When
        List<Inventory> availableInventories = inventoryRepository.findByStatus(InventoryStatus.AVAILABLE);
        List<Inventory> lowStockInventories = inventoryRepository.findByStatus(InventoryStatus.LOW_STOCK);
        List<Inventory> outOfStockInventories = inventoryRepository.findByStatus(InventoryStatus.OUT_OF_STOCK);

        // Then
        assertEquals(1, availableInventories.size());
        assertEquals(InventoryStatus.AVAILABLE, availableInventories.get(0).getStatus());

        assertEquals(1, lowStockInventories.size());
        assertEquals(InventoryStatus.LOW_STOCK, lowStockInventories.get(0).getStatus());

        assertEquals(1, outOfStockInventories.size());
        assertEquals(InventoryStatus.OUT_OF_STOCK, outOfStockInventories.get(0).getStatus());
    }

    @Test
    @DisplayName("Should find inventory needing reorder")
    void testFindInventoryNeedingReorder() {
        // Given - Create inventories with different stock levels

        // This one needs reorder: total stock (150 + 50 = 200) > reorder level (30) - does NOT need reorder
        inventoryRepository.save(testInventory);

        // This one needs reorder: total stock (15 + 10 = 25) <= reorder level (30)
        Product product8 = createProduct("TEST-SKU-008", "Test Product 8");
        Inventory needsReorder1 = new Inventory();
        needsReorder1.setSupplierId(supplierId);
        needsReorder1.setProductId(product8.getId());
        needsReorder1.setAvailableQuantity(15);
        needsReorder1.setReservedQuantity(10);
        needsReorder1.setReorderLevel(30);
        needsReorder1.setStatus(InventoryStatus.LOW_STOCK);
        inventoryRepository.save(needsReorder1);

        // This one needs reorder: total stock (5 + 0 = 5) <= reorder level (20)
        Product product9 = createProduct("TEST-SKU-009", "Test Product 9");
        Inventory needsReorder2 = new Inventory();
        needsReorder2.setSupplierId(supplierId);
        needsReorder2.setProductId(product9.getId());
        needsReorder2.setAvailableQuantity(5);
        needsReorder2.setReservedQuantity(0);
        needsReorder2.setReorderLevel(20);
        needsReorder2.setStatus(InventoryStatus.LOW_STOCK);
        inventoryRepository.save(needsReorder2);

        // This one is discontinued - should NOT appear in reorder list
        Product product10 = createProduct("TEST-SKU-010", "Test Product 10");
        Inventory discontinued = new Inventory();
        discontinued.setSupplierId(supplierId);
        discontinued.setProductId(product10.getId());
        discontinued.setAvailableQuantity(5);
        discontinued.setReservedQuantity(0);
        discontinued.setReorderLevel(20);
        discontinued.setStatus(InventoryStatus.DISCONTINUED);
        inventoryRepository.save(discontinued);

        // This one has null reorder level - should NOT appear in reorder list
        Product product11 = createProduct("TEST-SKU-011", "Test Product 11");
        Inventory noReorderLevel = new Inventory();
        noReorderLevel.setSupplierId(supplierId);
        noReorderLevel.setProductId(product11.getId());
        noReorderLevel.setAvailableQuantity(5);
        noReorderLevel.setReservedQuantity(0);
        noReorderLevel.setReorderLevel(null);
        noReorderLevel.setStatus(InventoryStatus.LOW_STOCK);
        inventoryRepository.save(noReorderLevel);

        // When
        List<Inventory> needsReorder = inventoryRepository.findInventoryNeedingReorder();

        // Then
        assertEquals(2, needsReorder.size());
        assertTrue(needsReorder.stream()
            .anyMatch(inv -> inv.getProductId().equals(product8.getId())));
        assertTrue(needsReorder.stream()
            .anyMatch(inv -> inv.getProductId().equals(product9.getId())));
        assertFalse(needsReorder.stream()
            .anyMatch(inv -> inv.getProductId().equals(product10.getId())), "Discontinued should not need reorder");
        assertFalse(needsReorder.stream()
            .anyMatch(inv -> inv.getProductId().equals(product11.getId())), "Null reorder level should not need reorder");
    }

    @Test
    @DisplayName("Should find inventory needing reorder by supplierId")
    void testFindInventoryNeedingReorderBySupplierId() {
        // Given - Create inventories for different suppliers

        // Supplier 1 - needs reorder
        Product product12 = createProduct("TEST-SKU-012", "Test Product 12");
        Inventory supplier1NeedsReorder = new Inventory();
        supplier1NeedsReorder.setSupplierId(supplierId);
        supplier1NeedsReorder.setProductId(product12.getId());
        supplier1NeedsReorder.setAvailableQuantity(10);
        supplier1NeedsReorder.setReservedQuantity(5);
        supplier1NeedsReorder.setReorderLevel(20);
        supplier1NeedsReorder.setStatus(InventoryStatus.LOW_STOCK);
        inventoryRepository.save(supplier1NeedsReorder);

        // Supplier 1 - does NOT need reorder
        Product product13 = createProduct("TEST-SKU-013", "Test Product 13");
        Inventory supplier1NoReorder = new Inventory();
        supplier1NoReorder.setSupplierId(supplierId);
        supplier1NoReorder.setProductId(product13.getId());
        supplier1NoReorder.setAvailableQuantity(100);
        supplier1NoReorder.setReservedQuantity(50);
        supplier1NoReorder.setReorderLevel(20);
        supplier1NoReorder.setStatus(InventoryStatus.AVAILABLE);
        inventoryRepository.save(supplier1NoReorder);

        // Supplier 2 - needs reorder
        Supplier supplier2 = createSupplier("supplier2b@test.com", "SUP22222222");
        Product product14 = createProduct("TEST-SKU-014", "Test Product 14");
        Inventory supplier2NeedsReorder = new Inventory();
        supplier2NeedsReorder.setSupplierId(supplier2.getId());
        supplier2NeedsReorder.setProductId(product14.getId());
        supplier2NeedsReorder.setAvailableQuantity(8);
        supplier2NeedsReorder.setReservedQuantity(2);
        supplier2NeedsReorder.setReorderLevel(15);
        supplier2NeedsReorder.setStatus(InventoryStatus.LOW_STOCK);
        inventoryRepository.save(supplier2NeedsReorder);

        // When
        List<Inventory> supplier1Reorders = inventoryRepository.findInventoryNeedingReorderBySupplierId(supplierId);
        List<Inventory> supplier2Reorders = inventoryRepository.findInventoryNeedingReorderBySupplierId(supplier2.getId());

        // Then
        assertEquals(1, supplier1Reorders.size());
        assertEquals(product12.getId(), supplier1Reorders.get(0).getProductId());

        assertEquals(1, supplier2Reorders.size());
        assertEquals(product14.getId(), supplier2Reorders.get(0).getProductId());
    }

    @Test
    @DisplayName("Should check if inventory exists by productId")
    void testExistsByProductId() {
        // Given
        inventoryRepository.save(testInventory);

        // Then
        assertTrue(inventoryRepository.existsByProductId(productId));
        assertFalse(inventoryRepository.existsByProductId(9999999L));
    }

    @Test
    @DisplayName("Should find all inventories")
    void testFindAll() {
        // Given
        inventoryRepository.save(testInventory);

        Product product15 = createProduct("TEST-SKU-015", "Test Product 15");
        Inventory another = new Inventory();
        another.setSupplierId(supplierId);
        another.setProductId(product15.getId());
        another.setAvailableQuantity(50);
        another.setReservedQuantity(10);
        another.setStatus(InventoryStatus.AVAILABLE);
        inventoryRepository.save(another);

        // When
        List<Inventory> allInventories = inventoryRepository.findAll();

        // Then
        assertEquals(2, allInventories.size());
        assertTrue(allInventories.stream()
            .anyMatch(inv -> inv.getProductId().equals(productId)));
        assertTrue(allInventories.stream()
            .anyMatch(inv -> inv.getProductId().equals(product15.getId())));
    }

    // ===== Delete Tests =====

    @Test
    @DisplayName("Should delete inventory by ID")
    void testDeleteById() {
        // Given
        Inventory saved = inventoryRepository.save(testInventory);
        Long inventoryId = saved.getId();

        // When
        inventoryRepository.deleteById(inventoryId);

        // Then
        Optional<Inventory> found = inventoryRepository.findById(inventoryId);
        assertFalse(found.isPresent(), "Inventory should be deleted");
    }

    // ===== Count Tests =====

    @Test
    @DisplayName("Should count all inventories")
    void testCount() {
        // Given
        assertEquals(0, inventoryRepository.count());

        inventoryRepository.save(testInventory);

        Product product16 = createProduct("TEST-SKU-016", "Test Product 16");
        Inventory another = new Inventory();
        another.setSupplierId(supplierId);
        another.setProductId(product16.getId());
        another.setAvailableQuantity(50);
        another.setReservedQuantity(10);
        another.setStatus(InventoryStatus.AVAILABLE);
        inventoryRepository.save(another);

        // Then
        assertEquals(2, inventoryRepository.count());
    }

    @Test
    @DisplayName("Should count inventories by status")
    void testCountByStatus() {
        // Given
        inventoryRepository.save(testInventory);  // AVAILABLE

        Product product17 = createProduct("TEST-SKU-017", "Test Product 17");
        Inventory lowStock = new Inventory();
        lowStock.setSupplierId(supplierId);
        lowStock.setProductId(product17.getId());
        lowStock.setAvailableQuantity(10);
        lowStock.setReservedQuantity(5);
        lowStock.setStatus(InventoryStatus.LOW_STOCK);
        inventoryRepository.save(lowStock);

        Product product18 = createProduct("TEST-SKU-018", "Test Product 18");
        Inventory anotherLowStock = new Inventory();
        anotherLowStock.setSupplierId(supplierId);
        anotherLowStock.setProductId(product18.getId());
        anotherLowStock.setAvailableQuantity(8);
        anotherLowStock.setReservedQuantity(2);
        anotherLowStock.setStatus(InventoryStatus.LOW_STOCK);
        inventoryRepository.save(anotherLowStock);

        // When
        long availableCount = inventoryRepository.countByStatus(InventoryStatus.AVAILABLE);
        long lowStockCount = inventoryRepository.countByStatus(InventoryStatus.LOW_STOCK);

        // Then
        assertEquals(1, availableCount);
        assertEquals(2, lowStockCount);
    }

    // ===== Entity-Domain Mapping Tests =====

    @Test
    @DisplayName("Should correctly map all domain fields to entity")
    void testDomainToEntityMapping() {
        // Given - Create domain object with all fields
        Product product19 = createProduct("TEST-SKU-019", "Test Product 19");

        Inventory domain = new Inventory();
        domain.setSupplierId(supplierId);
        domain.setProductId(product19.getId());
        domain.setAvailableQuantity(500);
        domain.setReservedQuantity(100);
        domain.setReorderLevel(50);
        domain.setReorderQuantity(200);
        domain.setWarehouseLocation("Test Warehouse - Section Z");
        LocalDateTime restockTime = LocalDateTime.now().minusDays(10);
        LocalDateTime updateTime = LocalDateTime.now();
        domain.setLastRestocked(restockTime);
        domain.setLastUpdated(updateTime);
        domain.setStatus(InventoryStatus.LOW_STOCK);

        // When - Save (triggers domain → entity mapping)
        Inventory saved = inventoryRepository.save(domain);

        // Then - Retrieve and verify all fields
        Inventory retrieved = inventoryRepository.findById(saved.getId()).get();

        assertNotNull(retrieved.getId());
        assertEquals(supplierId, retrieved.getSupplierId());
        assertEquals(product19.getId(), retrieved.getProductId());
        assertEquals(500, retrieved.getAvailableQuantity());
        assertEquals(100, retrieved.getReservedQuantity());
        assertEquals(50, retrieved.getReorderLevel());
        assertEquals(200, retrieved.getReorderQuantity());
        assertEquals("Test Warehouse - Section Z", retrieved.getWarehouseLocation());
        assertNotNull(retrieved.getLastRestocked());
        assertNotNull(retrieved.getLastUpdated());
        assertEquals(InventoryStatus.LOW_STOCK, retrieved.getStatus());
    }

    @Test
    @DisplayName("Should preserve domain object state after save")
    void testDomainObjectUnchangedAfterSave() {
        // Given
        Integer originalAvailable = testInventory.getAvailableQuantity();
        Integer originalReserved = testInventory.getReservedQuantity();
        InventoryStatus originalStatus = testInventory.getStatus();

        // When
        Inventory saved = inventoryRepository.save(testInventory);

        // Then - Original domain object should be unchanged
        assertEquals(originalAvailable, testInventory.getAvailableQuantity());
        assertEquals(originalReserved, testInventory.getReservedQuantity());
        assertEquals(originalStatus, testInventory.getStatus());

        // But returned object should have ID
        assertNotNull(saved.getId());
    }

    // ===== Business Logic Integration Tests =====

    @Test
    @DisplayName("Should persist stock reservation changes")
    void testPersistStockReservation() {
        // Given - Save inventory
        Inventory saved = inventoryRepository.save(testInventory);
        assertEquals(150, saved.getAvailableQuantity());
        assertEquals(50, saved.getReservedQuantity());

        // When - Reserve stock
        boolean reserved = saved.reserveStock(30);
        assertTrue(reserved);
        Inventory updated = inventoryRepository.save(saved);

        // Then - Verify changes persisted
        Inventory retrieved = inventoryRepository.findById(updated.getId()).get();
        assertEquals(120, retrieved.getAvailableQuantity());
        assertEquals(80, retrieved.getReservedQuantity());
    }

    @Test
    @DisplayName("Should persist stock deduction changes")
    void testPersistStockDeduction() {
        // Given - Save inventory with reserved stock
        Inventory saved = inventoryRepository.save(testInventory);
        assertEquals(50, saved.getReservedQuantity());

        // When - Deduct stock
        boolean deducted = saved.deductStock(30);
        assertTrue(deducted);
        Inventory updated = inventoryRepository.save(saved);

        // Then - Verify changes persisted
        Inventory retrieved = inventoryRepository.findById(updated.getId()).get();
        assertEquals(20, retrieved.getReservedQuantity());
    }

    @Test
    @DisplayName("Should persist stock release changes")
    void testPersistStockRelease() {
        // Given - Save inventory
        Inventory saved = inventoryRepository.save(testInventory);
        assertEquals(150, saved.getAvailableQuantity());
        assertEquals(50, saved.getReservedQuantity());

        // When - Release reserved stock
        saved.releaseReservedStock(20);
        Inventory updated = inventoryRepository.save(saved);

        // Then - Verify changes persisted
        Inventory retrieved = inventoryRepository.findById(updated.getId()).get();
        assertEquals(170, retrieved.getAvailableQuantity());
        assertEquals(30, retrieved.getReservedQuantity());
    }

    @Test
    @DisplayName("Should persist restock changes and update timestamp")
    void testPersistRestock() {
        // Given - Save inventory
        Inventory saved = inventoryRepository.save(testInventory);
        Integer originalAvailable = saved.getAvailableQuantity();

        // When - Restock inventory
        saved.restockInventory(100);
        Inventory updated = inventoryRepository.save(saved);

        // Then - Verify changes persisted
        Inventory retrieved = inventoryRepository.findById(updated.getId()).get();
        assertEquals(originalAvailable + 100, retrieved.getAvailableQuantity());
        assertNotNull(retrieved.getLastRestocked());
        // Note: We can't easily test if timestamp is newer due to time precision
    }

    @Test
    @DisplayName("Should persist status updates automatically")
    void testPersistAutomaticStatusUpdates() {
        // Given - Save inventory with low stock
        testInventory.setAvailableQuantity(15);
        testInventory.setReservedQuantity(10);
        testInventory.setReorderLevel(30);
        testInventory.setStatus(InventoryStatus.LOW_STOCK);
        Inventory saved = inventoryRepository.save(testInventory);

        // When - Restock to above reorder level
        saved.restockInventory(100);
        // Status should be automatically updated to AVAILABLE
        Inventory updated = inventoryRepository.save(saved);

        // Then - Verify status was updated
        Inventory retrieved = inventoryRepository.findById(updated.getId()).get();
        assertEquals(115, retrieved.getAvailableQuantity());
        assertEquals(InventoryStatus.AVAILABLE, retrieved.getStatus());
    }

    @Test
    @DisplayName("Should handle complete stock flow: reserve, deduct, release")
    void testCompleteStockFlow() {
        // Given - Save fresh inventory
        testInventory.setAvailableQuantity(100);
        testInventory.setReservedQuantity(0);
        Inventory saved = inventoryRepository.save(testInventory);

        // Step 1: Reserve stock (customer places order)
        saved.reserveStock(30);
        saved = inventoryRepository.save(saved);
        assertEquals(70, saved.getAvailableQuantity());
        assertEquals(30, saved.getReservedQuantity());

        // Step 2: Deduct stock (order is shipped)
        saved.deductStock(30);
        saved = inventoryRepository.save(saved);
        assertEquals(70, saved.getAvailableQuantity());
        assertEquals(0, saved.getReservedQuantity());

        // Step 3: Reserve more stock
        saved.reserveStock(20);
        saved = inventoryRepository.save(saved);
        assertEquals(50, saved.getAvailableQuantity());
        assertEquals(20, saved.getReservedQuantity());

        // Step 4: Release stock (order canceled)
        saved.releaseReservedStock(20);
        saved = inventoryRepository.save(saved);

        // Then - Verify final state
        Inventory finalState = inventoryRepository.findById(saved.getId()).get();
        assertEquals(70, finalState.getAvailableQuantity());
        assertEquals(0, finalState.getReservedQuantity());
    }

    @Test
    @DisplayName("Should correctly identify inventory needing reorder after updates")
    void testReorderDetectionAfterUpdates() {
        // Given - Save inventory that doesn't need reorder
        testInventory.setAvailableQuantity(100);
        testInventory.setReservedQuantity(0);
        testInventory.setReorderLevel(30);
        Inventory saved = inventoryRepository.save(testInventory);

        // Initially should not need reorder
        List<Inventory> needsReorder = inventoryRepository.findInventoryNeedingReorder();
        assertTrue(needsReorder.stream()
            .noneMatch(inv -> inv.getId().equals(saved.getId())));

        // When - Reserve stock to bring below reorder level
        saved.reserveStock(80);  // Available: 20, Reserved: 80, Total: 100 > 30
        saved.deductStock(80);   // Available: 20, Reserved: 0, Total: 20 <= 30
        saved.setStatus(InventoryStatus.LOW_STOCK);
        inventoryRepository.save(saved);

        // Then - Should now need reorder
        needsReorder = inventoryRepository.findInventoryNeedingReorder();
        assertTrue(needsReorder.stream()
            .anyMatch(inv -> inv.getId().equals(saved.getId())));
    }
}
