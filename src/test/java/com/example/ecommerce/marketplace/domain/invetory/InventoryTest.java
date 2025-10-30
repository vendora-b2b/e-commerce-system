package com.example.ecommerce.marketplace.domain.invetory;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for the Inventory domain entity.
 * Tests all domain behaviors, validations, and business rules for inventory management.
 */
class InventoryTest {

    private Inventory inventory;

    @BeforeEach
    void setUp() {
        inventory = new Inventory();
        inventory.setId(1L);
        inventory.setSupplierId(10L);
        inventory.setProductId(100L);
        inventory.setAvailableQuantity(100);
        inventory.setReservedQuantity(0);
        inventory.setReorderLevel(20);
        inventory.setReorderQuantity(50);
        inventory.setWarehouseLocation("Warehouse A");
        inventory.setStatus(InventoryStatus.AVAILABLE);
        inventory.setLastUpdated(LocalDateTime.now());
    }

    // ===== Has Sufficient Stock Tests =====

    @Test
    @DisplayName("Should have sufficient stock for requested quantity")
    void testHasSufficientStock_Success() {
        assertTrue(inventory.hasSufficientStock(50));
        assertTrue(inventory.hasSufficientStock(100));
    }

    @Test
    @DisplayName("Should not have sufficient stock for quantity exceeding available")
    void testHasSufficientStock_Insufficient() {
        assertFalse(inventory.hasSufficientStock(150));
    }

    @Test
    @DisplayName("Should return false for null quantity")
    void testHasSufficientStock_NullQuantity() {
        assertFalse(inventory.hasSufficientStock(null));
    }

    @Test
    @DisplayName("Should return false for zero quantity")
    void testHasSufficientStock_ZeroQuantity() {
        assertFalse(inventory.hasSufficientStock(0));
    }

    @Test
    @DisplayName("Should return false for negative quantity")
    void testHasSufficientStock_NegativeQuantity() {
        assertFalse(inventory.hasSufficientStock(-10));
    }

    @Test
    @DisplayName("Should return false when available quantity is null")
    void testHasSufficientStock_NullAvailableQuantity() {
        inventory.setAvailableQuantity(null);
        assertFalse(inventory.hasSufficientStock(10));
    }

    // ===== Reserve Stock Tests =====

    @Test
    @DisplayName("Should reserve stock successfully")
    void testReserveStock_Success() {
        boolean result = inventory.reserveStock(30);

        assertTrue(result);
        assertEquals(70, inventory.getAvailableQuantity());
        assertEquals(30, inventory.getReservedQuantity());
        assertNotNull(inventory.getLastUpdated());
    }

    @Test
    @DisplayName("Should reserve all available stock")
    void testReserveStock_AllStock() {
        boolean result = inventory.reserveStock(100);

        assertTrue(result);
        assertEquals(0, inventory.getAvailableQuantity());
        assertEquals(100, inventory.getReservedQuantity());
        assertEquals(InventoryStatus.OUT_OF_STOCK, inventory.getStatus());
    }

    @Test
    @DisplayName("Should fail to reserve when insufficient stock")
    void testReserveStock_InsufficientStock() {
        boolean result = inventory.reserveStock(150);

        assertFalse(result);
        assertEquals(100, inventory.getAvailableQuantity());
        assertEquals(0, inventory.getReservedQuantity());
    }

    @Test
    @DisplayName("Should fail to reserve null quantity")
    void testReserveStock_NullQuantity() {
        boolean result = inventory.reserveStock(null);

        assertFalse(result);
        assertEquals(100, inventory.getAvailableQuantity());
    }

    @Test
    @DisplayName("Should fail to reserve zero quantity")
    void testReserveStock_ZeroQuantity() {
        boolean result = inventory.reserveStock(0);

        assertFalse(result);
    }

    @Test
    @DisplayName("Should fail to reserve negative quantity")
    void testReserveStock_NegativeQuantity() {
        boolean result = inventory.reserveStock(-10);

        assertFalse(result);
    }

    @Test
    @DisplayName("Should update status to LOW_STOCK after reservation")
    void testReserveStock_UpdatesStatusToLowStock() {
        inventory.setReorderLevel(20);
        boolean result = inventory.reserveStock(85);

        assertTrue(result);
        assertEquals(15, inventory.getAvailableQuantity());
        assertEquals(InventoryStatus.LOW_STOCK, inventory.getStatus());
    }

    @Test
    @DisplayName("Should handle reserved quantity starting from null")
    void testReserveStock_FromNullReservedQuantity() {
        inventory.setReservedQuantity(null);
        boolean result = inventory.reserveStock(20);

        assertTrue(result);
        assertEquals(80, inventory.getAvailableQuantity());
        assertEquals(20, inventory.getReservedQuantity());
    }

    // ===== Release Reserved Stock Tests =====

    @Test
    @DisplayName("Should release reserved stock successfully")
    void testReleaseReservedStock_Success() {
        inventory.reserveStock(30);
        inventory.releaseReservedStock(15);

        assertEquals(85, inventory.getAvailableQuantity());
        assertEquals(15, inventory.getReservedQuantity());
        assertNotNull(inventory.getLastUpdated());
    }

    @Test
    @DisplayName("Should release all reserved stock")
    void testReleaseReservedStock_AllReserved() {
        inventory.reserveStock(50);
        inventory.releaseReservedStock(50);

        assertEquals(100, inventory.getAvailableQuantity());
        assertEquals(0, inventory.getReservedQuantity());
    }

    @Test
    @DisplayName("Should throw exception when releasing null quantity")
    void testReleaseReservedStock_NullQuantity() {
        inventory.reserveStock(30);

        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            inventory.releaseReservedStock(null);
        });
        assertEquals("Quantity must be positive", exception.getMessage());
    }

    @Test
    @DisplayName("Should throw exception when releasing zero quantity")
    void testReleaseReservedStock_ZeroQuantity() {
        inventory.reserveStock(30);

        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            inventory.releaseReservedStock(0);
        });
        assertEquals("Quantity must be positive", exception.getMessage());
    }

    @Test
    @DisplayName("Should throw exception when releasing negative quantity")
    void testReleaseReservedStock_NegativeQuantity() {
        inventory.reserveStock(30);

        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            inventory.releaseReservedStock(-10);
        });
        assertEquals("Quantity must be positive", exception.getMessage());
    }

    @Test
    @DisplayName("Should throw exception when insufficient reserved stock")
    void testReleaseReservedStock_InsufficientReserved() {
        inventory.reserveStock(20);

        Exception exception = assertThrows(IllegalStateException.class, () -> {
            inventory.releaseReservedStock(30);
        });
        assertEquals("Not enough reserved stock to release", exception.getMessage());
    }

    @Test
    @DisplayName("Should throw exception when reserved quantity is null")
    void testReleaseReservedStock_NullReservedQuantity() {
        inventory.setReservedQuantity(null);

        Exception exception = assertThrows(IllegalStateException.class, () -> {
            inventory.releaseReservedStock(10);
        });
        assertEquals("Not enough reserved stock to release", exception.getMessage());
    }

    @Test
    @DisplayName("Should update status when releasing stock")
    void testReleaseReservedStock_UpdatesStatus() {
        inventory.setAvailableQuantity(10);
        inventory.setReservedQuantity(50);
        inventory.setStatus(InventoryStatus.LOW_STOCK);

        inventory.releaseReservedStock(50);

        assertEquals(60, inventory.getAvailableQuantity());
        assertEquals(InventoryStatus.AVAILABLE, inventory.getStatus());
    }

    // ===== Deduct Stock Tests =====

    @Test
    @DisplayName("Should deduct stock from reserved quantity")
    void testDeductStock_Success() {
        inventory.reserveStock(50);
        boolean result = inventory.deductStock(30);

        assertTrue(result);
        assertEquals(50, inventory.getAvailableQuantity());
        assertEquals(20, inventory.getReservedQuantity());
        assertNotNull(inventory.getLastUpdated());
    }

    @Test
    @DisplayName("Should deduct all reserved stock")
    void testDeductStock_AllReserved() {
        inventory.reserveStock(50);
        boolean result = inventory.deductStock(50);

        assertTrue(result);
        assertEquals(50, inventory.getAvailableQuantity());
        assertEquals(0, inventory.getReservedQuantity());
    }

    @Test
    @DisplayName("Should fail to deduct when insufficient reserved stock")
    void testDeductStock_InsufficientReserved() {
        inventory.reserveStock(20);
        boolean result = inventory.deductStock(30);

        assertFalse(result);
        assertEquals(80, inventory.getAvailableQuantity());
        assertEquals(20, inventory.getReservedQuantity());
    }

    @Test
    @DisplayName("Should fail to deduct null quantity")
    void testDeductStock_NullQuantity() {
        inventory.reserveStock(30);
        boolean result = inventory.deductStock(null);

        assertFalse(result);
    }

    @Test
    @DisplayName("Should fail to deduct zero quantity")
    void testDeductStock_ZeroQuantity() {
        inventory.reserveStock(30);
        boolean result = inventory.deductStock(0);

        assertFalse(result);
    }

    @Test
    @DisplayName("Should fail to deduct negative quantity")
    void testDeductStock_NegativeQuantity() {
        inventory.reserveStock(30);
        boolean result = inventory.deductStock(-10);

        assertFalse(result);
    }

    @Test
    @DisplayName("Should fail to deduct when reserved quantity is null")
    void testDeductStock_NullReservedQuantity() {
        inventory.setReservedQuantity(null);
        boolean result = inventory.deductStock(10);

        assertFalse(result);
    }

    // ===== Restock Inventory Tests =====

    @Test
    @DisplayName("Should restock inventory successfully")
    void testRestockInventory_Success() {
        inventory.restockInventory(50);

        assertEquals(150, inventory.getAvailableQuantity());
        assertNotNull(inventory.getLastRestocked());
        assertNotNull(inventory.getLastUpdated());
    }

    @Test
    @DisplayName("Should restock when available quantity is null")
    void testRestockInventory_NullAvailableQuantity() {
        inventory.setAvailableQuantity(null);
        inventory.restockInventory(100);

        assertEquals(100, inventory.getAvailableQuantity());
    }

    @Test
    @DisplayName("Should update status after restocking")
    void testRestockInventory_UpdatesStatus() {
        inventory.setAvailableQuantity(0);
        inventory.setStatus(InventoryStatus.OUT_OF_STOCK);

        inventory.restockInventory(100);

        assertEquals(100, inventory.getAvailableQuantity());
        assertEquals(InventoryStatus.AVAILABLE, inventory.getStatus());
    }

    @Test
    @DisplayName("Should throw exception when restocking null quantity")
    void testRestockInventory_NullQuantity() {
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            inventory.restockInventory(null);
        });
        assertEquals("Restock quantity must be positive", exception.getMessage());
    }

    @Test
    @DisplayName("Should throw exception when restocking zero quantity")
    void testRestockInventory_ZeroQuantity() {
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            inventory.restockInventory(0);
        });
        assertEquals("Restock quantity must be positive", exception.getMessage());
    }

    @Test
    @DisplayName("Should throw exception when restocking negative quantity")
    void testRestockInventory_NegativeQuantity() {
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            inventory.restockInventory(-50);
        });
        assertEquals("Restock quantity must be positive", exception.getMessage());
    }

    // ===== Needs Reorder Tests =====

    @Test
    @DisplayName("Should not need reorder when stock is above reorder level")
    void testNeedsReorder_NoReorderNeeded() {
        inventory.setAvailableQuantity(100);
        inventory.setReservedQuantity(0);
        inventory.setReorderLevel(20);

        assertFalse(inventory.needsReorder());
    }

    @Test
    @DisplayName("Should need reorder when stock is at reorder level")
    void testNeedsReorder_AtReorderLevel() {
        inventory.setAvailableQuantity(15);
        inventory.setReservedQuantity(5);
        inventory.setReorderLevel(20);

        assertTrue(inventory.needsReorder());
    }

    @Test
    @DisplayName("Should need reorder when stock is below reorder level")
    void testNeedsReorder_BelowReorderLevel() {
        inventory.setAvailableQuantity(10);
        inventory.setReservedQuantity(0);
        inventory.setReorderLevel(20);

        assertTrue(inventory.needsReorder());
    }

    @Test
    @DisplayName("Should not need reorder when product is discontinued")
    void testNeedsReorder_Discontinued() {
        inventory.setAvailableQuantity(5);
        inventory.setReorderLevel(20);
        inventory.setStatus(InventoryStatus.DISCONTINUED);

        assertFalse(inventory.needsReorder());
    }

    @Test
    @DisplayName("Should not need reorder when reorder level is null")
    void testNeedsReorder_NullReorderLevel() {
        inventory.setReorderLevel(null);

        assertFalse(inventory.needsReorder());
    }

    @Test
    @DisplayName("Should calculate total stock for reorder check")
    void testNeedsReorder_ConsidersTotalStock() {
        inventory.setAvailableQuantity(10);
        inventory.setReservedQuantity(10);
        inventory.setReorderLevel(20);

        assertTrue(inventory.needsReorder());
    }

    // ===== Get Total Stock Tests =====

    @Test
    @DisplayName("Should calculate total stock correctly")
    void testGetTotalStock_Success() {
        inventory.setAvailableQuantity(70);
        inventory.setReservedQuantity(30);

        assertEquals(100, inventory.getTotalStock());
    }

    @Test
    @DisplayName("Should return available quantity when reserved is zero")
    void testGetTotalStock_NoReserved() {
        inventory.setAvailableQuantity(100);
        inventory.setReservedQuantity(0);

        assertEquals(100, inventory.getTotalStock());
    }

    @Test
    @DisplayName("Should handle null available quantity")
    void testGetTotalStock_NullAvailable() {
        inventory.setAvailableQuantity(null);
        inventory.setReservedQuantity(50);

        assertEquals(50, inventory.getTotalStock());
    }

    @Test
    @DisplayName("Should handle null reserved quantity")
    void testGetTotalStock_NullReserved() {
        inventory.setAvailableQuantity(100);
        inventory.setReservedQuantity(null);

        assertEquals(100, inventory.getTotalStock());
    }

    @Test
    @DisplayName("Should handle both quantities null")
    void testGetTotalStock_BothNull() {
        inventory.setAvailableQuantity(null);
        inventory.setReservedQuantity(null);

        assertEquals(0, inventory.getTotalStock());
    }

    // ===== Is Available For Order Tests =====

    @Test
    @DisplayName("Should be available for order when status is AVAILABLE")
    void testIsAvailableForOrder_Available() {
        inventory.setStatus(InventoryStatus.AVAILABLE);
        assertTrue(inventory.isAvailableForOrder());
    }

    @Test
    @DisplayName("Should be available for order when status is LOW_STOCK")
    void testIsAvailableForOrder_LowStock() {
        inventory.setStatus(InventoryStatus.LOW_STOCK);
        assertTrue(inventory.isAvailableForOrder());
    }

    @Test
    @DisplayName("Should not be available for order when OUT_OF_STOCK")
    void testIsAvailableForOrder_OutOfStock() {
        inventory.setStatus(InventoryStatus.OUT_OF_STOCK);
        assertFalse(inventory.isAvailableForOrder());
    }

    @Test
    @DisplayName("Should not be available for order when DISCONTINUED")
    void testIsAvailableForOrder_Discontinued() {
        inventory.setStatus(InventoryStatus.DISCONTINUED);
        assertFalse(inventory.isAvailableForOrder());
    }

    // ===== Update Status Tests =====

    @Test
    @DisplayName("Should update status to AVAILABLE when stock is above reorder level")
    void testUpdateStatus_Available() {
        inventory.setAvailableQuantity(100);
        inventory.setReorderLevel(20);
        inventory.setStatus(InventoryStatus.LOW_STOCK);

        inventory.updateStatus();

        assertEquals(InventoryStatus.AVAILABLE, inventory.getStatus());
    }

    @Test
    @DisplayName("Should update status to LOW_STOCK when at reorder level")
    void testUpdateStatus_LowStock() {
        inventory.setAvailableQuantity(20);
        inventory.setReorderLevel(20);
        inventory.setStatus(InventoryStatus.AVAILABLE);

        inventory.updateStatus();

        assertEquals(InventoryStatus.LOW_STOCK, inventory.getStatus());
    }

    @Test
    @DisplayName("Should update status to LOW_STOCK when below reorder level")
    void testUpdateStatus_BelowReorderLevel() {
        inventory.setAvailableQuantity(10);
        inventory.setReorderLevel(20);

        inventory.updateStatus();

        assertEquals(InventoryStatus.LOW_STOCK, inventory.getStatus());
    }

    @Test
    @DisplayName("Should update status to OUT_OF_STOCK when available is zero")
    void testUpdateStatus_OutOfStock() {
        inventory.setAvailableQuantity(0);
        inventory.setStatus(InventoryStatus.AVAILABLE);

        inventory.updateStatus();

        assertEquals(InventoryStatus.OUT_OF_STOCK, inventory.getStatus());
    }

    @Test
    @DisplayName("Should not update status when DISCONTINUED")
    void testUpdateStatus_DoesNotChangeDiscontinued() {
        inventory.setAvailableQuantity(100);
        inventory.setStatus(InventoryStatus.DISCONTINUED);

        inventory.updateStatus();

        assertEquals(InventoryStatus.DISCONTINUED, inventory.getStatus());
    }

    @Test
    @DisplayName("Should handle null reorder level in status update")
    void testUpdateStatus_NullReorderLevel() {
        inventory.setAvailableQuantity(50);
        inventory.setReorderLevel(null);

        inventory.updateStatus();

        assertEquals(InventoryStatus.AVAILABLE, inventory.getStatus());
    }

    @Test
    @DisplayName("Should handle null available quantity in status update")
    void testUpdateStatus_NullAvailableQuantity() {
        inventory.setAvailableQuantity(null);
        inventory.setStatus(InventoryStatus.AVAILABLE);

        inventory.updateStatus();

        assertEquals(InventoryStatus.OUT_OF_STOCK, inventory.getStatus());
    }

    // ===== Integration/Flow Tests =====

    @Test
    @DisplayName("Should handle complete order flow: reserve -> deduct")
    void testCompleteOrderFlow() {
        // Reserve stock for order
        assertTrue(inventory.reserveStock(50));
        assertEquals(50, inventory.getAvailableQuantity());
        assertEquals(50, inventory.getReservedQuantity());

        // Deduct stock after shipment
        assertTrue(inventory.deductStock(50));
        assertEquals(50, inventory.getAvailableQuantity());
        assertEquals(0, inventory.getReservedQuantity());
        assertEquals(InventoryStatus.AVAILABLE, inventory.getStatus());
    }

    @Test
    @DisplayName("Should handle order cancellation flow: reserve -> release")
    void testOrderCancellationFlow() {
        // Reserve stock for order
        assertTrue(inventory.reserveStock(40));
        assertEquals(60, inventory.getAvailableQuantity());
        assertEquals(40, inventory.getReservedQuantity());

        // Release stock when order is cancelled
        inventory.releaseReservedStock(40);
        assertEquals(100, inventory.getAvailableQuantity());
        assertEquals(0, inventory.getReservedQuantity());
        assertEquals(InventoryStatus.AVAILABLE, inventory.getStatus());
    }

    @Test
    @DisplayName("Should handle stock depletion and restocking")
    void testStockDepletionAndRestocking() {
        // Deplete stock
        inventory.reserveStock(100);
        assertEquals(InventoryStatus.OUT_OF_STOCK, inventory.getStatus());

        // Restock
        inventory.restockInventory(200);
        assertEquals(200, inventory.getAvailableQuantity());
        assertEquals(InventoryStatus.AVAILABLE, inventory.getStatus());
        assertFalse(inventory.needsReorder());
    }

    @Test
    @DisplayName("Should trigger reorder when stock falls below threshold")
    void testReorderTrigger() {
        inventory.setReorderLevel(30);

        // Use stock until reorder needed - reserve and then deduct
        inventory.reserveStock(75);
        inventory.deductStock(75);

        assertEquals(25, inventory.getAvailableQuantity());
        assertEquals(0, inventory.getReservedQuantity());
        assertEquals(25, inventory.getTotalStock());
        assertEquals(InventoryStatus.LOW_STOCK, inventory.getStatus());
        assertTrue(inventory.needsReorder());
    }

    // ===== Getters and Setters Tests =====

    @Test
    @DisplayName("Should get and set id correctly")
    void testGetSetId() {
        inventory.setId(999L);
        assertEquals(999L, inventory.getId());
    }

    @Test
    @DisplayName("Should get and set supplier ID correctly")
    void testGetSetSupplierId() {
        inventory.setSupplierId(50L);
        assertEquals(50L, inventory.getSupplierId());
    }

    @Test
    @DisplayName("Should get and set product ID correctly")
    void testGetSetProductId() {
        inventory.setProductId(200L);
        assertEquals(200L, inventory.getProductId());
    }

    @Test
    @DisplayName("Should get and set available quantity correctly")
    void testGetSetAvailableQuantity() {
        inventory.setAvailableQuantity(500);
        assertEquals(500, inventory.getAvailableQuantity());
    }

    @Test
    @DisplayName("Should get and set reserved quantity correctly")
    void testGetSetReservedQuantity() {
        inventory.setReservedQuantity(75);
        assertEquals(75, inventory.getReservedQuantity());
    }

    @Test
    @DisplayName("Should get and set reorder level correctly")
    void testGetSetReorderLevel() {
        inventory.setReorderLevel(50);
        assertEquals(50, inventory.getReorderLevel());
    }

    @Test
    @DisplayName("Should get and set reorder quantity correctly")
    void testGetSetReorderQuantity() {
        inventory.setReorderQuantity(100);
        assertEquals(100, inventory.getReorderQuantity());
    }

    @Test
    @DisplayName("Should get and set warehouse location correctly")
    void testGetSetWarehouseLocation() {
        inventory.setWarehouseLocation("Warehouse B");
        assertEquals("Warehouse B", inventory.getWarehouseLocation());
    }

    @Test
    @DisplayName("Should get and set status correctly")
    void testGetSetStatus() {
        inventory.setStatus(InventoryStatus.LOW_STOCK);
        assertEquals(InventoryStatus.LOW_STOCK, inventory.getStatus());
    }

    @Test
    @DisplayName("Should get and set last restocked correctly")
    void testGetSetLastRestocked() {
        LocalDateTime time = LocalDateTime.now();
        inventory.setLastRestocked(time);
        assertEquals(time, inventory.getLastRestocked());
    }

    @Test
    @DisplayName("Should get and set last updated correctly")
    void testGetSetLastUpdated() {
        LocalDateTime time = LocalDateTime.now();
        inventory.setLastUpdated(time);
        assertEquals(time, inventory.getLastUpdated());
    }
}
