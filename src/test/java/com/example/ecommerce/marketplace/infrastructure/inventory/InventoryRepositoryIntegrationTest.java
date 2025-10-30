package com.example.ecommerce.marketplace.infrastructure.inventory;

import com.example.ecommerce.marketplace.domain.invetory.Inventory;
import com.example.ecommerce.marketplace.domain.invetory.InventoryRepository;
import com.example.ecommerce.marketplace.domain.invetory.InventoryStatus;
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
 * Uses H2 in-memory database configured in application-test.properties.
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

    private Inventory testInventory;

    @BeforeEach
    void setUp() {
        testInventory = new Inventory();
        testInventory.setId(null);
        testInventory.setSupplierId(100L);
        testInventory.setProductId(200L);
        testInventory.setAvailableQuantity(150);
        testInventory.setReservedQuantity(50);
        testInventory.setReorderLevel(30);
        testInventory.setReorderQuantity(100);
        testInventory.setWarehouseLocation("Warehouse A - Aisle 5");
        testInventory.setLastRestocked(LocalDateTime.now().minusDays(5));
        testInventory.setLastUpdated(LocalDateTime.now());
        testInventory.setStatus(InventoryStatus.AVAILABLE);
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
        assertEquals(100L, inventory.getSupplierId());
        assertEquals(200L, inventory.getProductId());
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
        // Given
        Inventory inventory = new Inventory();
        inventory.setSupplierId(101L);
        inventory.setProductId(201L);
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
        assertEquals(101L, saved.getSupplierId());
        assertEquals(201L, saved.getProductId());
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
        Inventory inventory = new Inventory();
        inventory.setSupplierId(102L);
        inventory.setProductId(202L);
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

        // When - Try to save another inventory with same productId
        Inventory duplicate = new Inventory();
        duplicate.setSupplierId(999L);  // Different supplier
        duplicate.setProductId(200L);  // Same productId
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
        Optional<Inventory> found = inventoryRepository.findByProductId(200L);

        // Then
        assertTrue(found.isPresent());
        assertEquals(200L, found.get().getProductId());
        assertEquals(100L, found.get().getSupplierId());
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
        Optional<Inventory> found = inventoryRepository.findBySupplierIdAndProductId(100L, 200L);

        // Then
        assertTrue(found.isPresent());
        assertEquals(100L, found.get().getSupplierId());
        assertEquals(200L, found.get().getProductId());
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

        Inventory inventory2 = new Inventory();
        inventory2.setSupplierId(100L);
        inventory2.setProductId(201L);
        inventory2.setAvailableQuantity(75);
        inventory2.setReservedQuantity(25);
        inventory2.setStatus(InventoryStatus.AVAILABLE);
        inventoryRepository.save(inventory2);

        Inventory inventory3 = new Inventory();
        inventory3.setSupplierId(200L);  // Different supplier
        inventory3.setProductId(202L);
        inventory3.setAvailableQuantity(50);
        inventory3.setReservedQuantity(0);
        inventory3.setStatus(InventoryStatus.AVAILABLE);
        inventoryRepository.save(inventory3);

        // When
        List<Inventory> supplierInventories = inventoryRepository.findBySupplierId(100L);

        // Then
        assertEquals(2, supplierInventories.size());
        assertTrue(supplierInventories.stream()
            .allMatch(inv -> inv.getSupplierId().equals(100L)));
    }

    @Test
    @DisplayName("Should find inventory by status")
    void testFindByStatus() {
        // Given
        inventoryRepository.save(testInventory);  // AVAILABLE

        Inventory lowStock = new Inventory();
        lowStock.setSupplierId(101L);
        lowStock.setProductId(203L);
        lowStock.setAvailableQuantity(10);
        lowStock.setReservedQuantity(5);
        lowStock.setReorderLevel(20);
        lowStock.setStatus(InventoryStatus.LOW_STOCK);
        inventoryRepository.save(lowStock);

        Inventory outOfStock = new Inventory();
        outOfStock.setSupplierId(102L);
        outOfStock.setProductId(204L);
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
        Inventory needsReorder1 = new Inventory();
        needsReorder1.setSupplierId(101L);
        needsReorder1.setProductId(205L);
        needsReorder1.setAvailableQuantity(15);
        needsReorder1.setReservedQuantity(10);
        needsReorder1.setReorderLevel(30);
        needsReorder1.setStatus(InventoryStatus.LOW_STOCK);
        inventoryRepository.save(needsReorder1);

        // This one needs reorder: total stock (5 + 0 = 5) <= reorder level (20)
        Inventory needsReorder2 = new Inventory();
        needsReorder2.setSupplierId(102L);
        needsReorder2.setProductId(206L);
        needsReorder2.setAvailableQuantity(5);
        needsReorder2.setReservedQuantity(0);
        needsReorder2.setReorderLevel(20);
        needsReorder2.setStatus(InventoryStatus.LOW_STOCK);
        inventoryRepository.save(needsReorder2);

        // This one is discontinued - should NOT appear in reorder list
        Inventory discontinued = new Inventory();
        discontinued.setSupplierId(103L);
        discontinued.setProductId(207L);
        discontinued.setAvailableQuantity(5);
        discontinued.setReservedQuantity(0);
        discontinued.setReorderLevel(20);
        discontinued.setStatus(InventoryStatus.DISCONTINUED);
        inventoryRepository.save(discontinued);

        // This one has null reorder level - should NOT appear in reorder list
        Inventory noReorderLevel = new Inventory();
        noReorderLevel.setSupplierId(104L);
        noReorderLevel.setProductId(208L);
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
            .anyMatch(inv -> inv.getProductId().equals(205L)));
        assertTrue(needsReorder.stream()
            .anyMatch(inv -> inv.getProductId().equals(206L)));
        assertFalse(needsReorder.stream()
            .anyMatch(inv -> inv.getProductId().equals(207L)), "Discontinued should not need reorder");
        assertFalse(needsReorder.stream()
            .anyMatch(inv -> inv.getProductId().equals(208L)), "Null reorder level should not need reorder");
    }

    @Test
    @DisplayName("Should find inventory needing reorder by supplierId")
    void testFindInventoryNeedingReorderBySupplierId() {
        // Given - Create inventories for different suppliers

        // Supplier 100 - needs reorder
        Inventory supplier100NeedsReorder = new Inventory();
        supplier100NeedsReorder.setSupplierId(100L);
        supplier100NeedsReorder.setProductId(210L);
        supplier100NeedsReorder.setAvailableQuantity(10);
        supplier100NeedsReorder.setReservedQuantity(5);
        supplier100NeedsReorder.setReorderLevel(20);
        supplier100NeedsReorder.setStatus(InventoryStatus.LOW_STOCK);
        inventoryRepository.save(supplier100NeedsReorder);

        // Supplier 100 - does NOT need reorder
        Inventory supplier100NoReorder = new Inventory();
        supplier100NoReorder.setSupplierId(100L);
        supplier100NoReorder.setProductId(211L);
        supplier100NoReorder.setAvailableQuantity(100);
        supplier100NoReorder.setReservedQuantity(50);
        supplier100NoReorder.setReorderLevel(20);
        supplier100NoReorder.setStatus(InventoryStatus.AVAILABLE);
        inventoryRepository.save(supplier100NoReorder);

        // Supplier 200 - needs reorder
        Inventory supplier200NeedsReorder = new Inventory();
        supplier200NeedsReorder.setSupplierId(200L);
        supplier200NeedsReorder.setProductId(212L);
        supplier200NeedsReorder.setAvailableQuantity(8);
        supplier200NeedsReorder.setReservedQuantity(2);
        supplier200NeedsReorder.setReorderLevel(15);
        supplier200NeedsReorder.setStatus(InventoryStatus.LOW_STOCK);
        inventoryRepository.save(supplier200NeedsReorder);

        // When
        List<Inventory> supplier100Reorders = inventoryRepository.findInventoryNeedingReorderBySupplierId(100L);
        List<Inventory> supplier200Reorders = inventoryRepository.findInventoryNeedingReorderBySupplierId(200L);

        // Then
        assertEquals(1, supplier100Reorders.size());
        assertEquals(210L, supplier100Reorders.get(0).getProductId());

        assertEquals(1, supplier200Reorders.size());
        assertEquals(212L, supplier200Reorders.get(0).getProductId());
    }

    @Test
    @DisplayName("Should check if inventory exists by productId")
    void testExistsByProductId() {
        // Given
        inventoryRepository.save(testInventory);

        // Then
        assertTrue(inventoryRepository.existsByProductId(200L));
        assertFalse(inventoryRepository.existsByProductId(999L));
    }

    @Test
    @DisplayName("Should find all inventories")
    void testFindAll() {
        // Given
        inventoryRepository.save(testInventory);

        Inventory another = new Inventory();
        another.setSupplierId(101L);
        another.setProductId(213L);
        another.setAvailableQuantity(50);
        another.setReservedQuantity(10);
        another.setStatus(InventoryStatus.AVAILABLE);
        inventoryRepository.save(another);

        // When
        List<Inventory> allInventories = inventoryRepository.findAll();

        // Then
        assertEquals(2, allInventories.size());
        assertTrue(allInventories.stream()
            .anyMatch(inv -> inv.getProductId().equals(200L)));
        assertTrue(allInventories.stream()
            .anyMatch(inv -> inv.getProductId().equals(213L)));
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

        Inventory another = new Inventory();
        another.setSupplierId(101L);
        another.setProductId(214L);
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

        Inventory lowStock = new Inventory();
        lowStock.setSupplierId(101L);
        lowStock.setProductId(215L);
        lowStock.setAvailableQuantity(10);
        lowStock.setReservedQuantity(5);
        lowStock.setStatus(InventoryStatus.LOW_STOCK);
        inventoryRepository.save(lowStock);

        Inventory anotherLowStock = new Inventory();
        anotherLowStock.setSupplierId(102L);
        anotherLowStock.setProductId(216L);
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
        Inventory domain = new Inventory();
        domain.setSupplierId(999L);
        domain.setProductId(999L);
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
        assertEquals(999L, retrieved.getSupplierId());
        assertEquals(999L, retrieved.getProductId());
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
