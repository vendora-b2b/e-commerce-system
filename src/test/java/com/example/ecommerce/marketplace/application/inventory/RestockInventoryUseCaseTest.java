package com.example.ecommerce.marketplace.application.inventory;

import com.example.ecommerce.marketplace.domain.invetory.Inventory;
import com.example.ecommerce.marketplace.domain.invetory.InventoryRepository;
import com.example.ecommerce.marketplace.domain.invetory.InventoryStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for RestockInventoryUseCase using Mockito.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("RestockInventoryUseCase Unit Tests")
class RestockInventoryUseCaseTest {

    @Mock
    private InventoryRepository inventoryRepository;

    @InjectMocks
    private RestockInventoryUseCase useCase;

    private RestockInventoryCommand validCommand;
    private Inventory testInventory;

    @BeforeEach
    void setUp() {
        validCommand = new RestockInventoryCommand(100L, 50);

        testInventory = new Inventory();
        testInventory.setId(1L);
        testInventory.setProductId(100L);
        testInventory.setSupplierId(10L);
        testInventory.setAvailableQuantity(20);
        testInventory.setReservedQuantity(10);
        testInventory.setReorderLevel(30);
        testInventory.setStatus(InventoryStatus.LOW_STOCK);
    }

    // ===== Success Case Tests =====

    @Test
    @DisplayName("Should successfully restock inventory with valid data")
    void testExecute_Success() {
        // Given
        when(inventoryRepository.findByProductId(100L)).thenReturn(Optional.of(testInventory));
        when(inventoryRepository.save(any(Inventory.class))).thenReturn(testInventory);

        // When
        RestockInventoryResult result = useCase.execute(validCommand);

        // Then
        assertTrue(result.isSuccess());
        assertEquals("Inventory restocked successfully", result.getMessage());
        assertNull(result.getErrorCode());
        assertEquals(70, testInventory.getAvailableQuantity());
        assertNotNull(testInventory.getLastRestocked());

        verify(inventoryRepository).findByProductId(100L);
        verify(inventoryRepository).save(any(Inventory.class));
    }

    @Test
    @DisplayName("Should update status from OUT_OF_STOCK to AVAILABLE after restock")
    void testExecute_UpdatesStatusFromOutOfStock() {
        // Given
        testInventory.setAvailableQuantity(0);
        testInventory.setStatus(InventoryStatus.OUT_OF_STOCK);
        when(inventoryRepository.findByProductId(100L)).thenReturn(Optional.of(testInventory));
        when(inventoryRepository.save(any(Inventory.class))).thenReturn(testInventory);

        // When
        RestockInventoryResult result = useCase.execute(validCommand);

        // Then
        assertTrue(result.isSuccess());
        assertEquals(50, testInventory.getAvailableQuantity());
        assertEquals(InventoryStatus.AVAILABLE, testInventory.getStatus());
    }

    @Test
    @DisplayName("Should update status from LOW_STOCK to AVAILABLE after large restock")
    void testExecute_UpdatesStatusFromLowStock() {
        // Given
        RestockInventoryCommand command = new RestockInventoryCommand(100L, 100);
        when(inventoryRepository.findByProductId(100L)).thenReturn(Optional.of(testInventory));
        when(inventoryRepository.save(any(Inventory.class))).thenReturn(testInventory);

        // When
        RestockInventoryResult result = useCase.execute(command);

        // Then
        assertTrue(result.isSuccess());
        assertEquals(120, testInventory.getAvailableQuantity());
        assertEquals(InventoryStatus.AVAILABLE, testInventory.getStatus());
    }

    @Test
    @DisplayName("Should restock minimum quantity of 1")
    void testExecute_MinimumQuantity() {
        // Given
        RestockInventoryCommand command = new RestockInventoryCommand(100L, 1);
        when(inventoryRepository.findByProductId(100L)).thenReturn(Optional.of(testInventory));
        when(inventoryRepository.save(any(Inventory.class))).thenReturn(testInventory);

        // When
        RestockInventoryResult result = useCase.execute(command);

        // Then
        assertTrue(result.isSuccess());
        assertEquals(21, testInventory.getAvailableQuantity());
    }

    @Test
    @DisplayName("Should restock large quantity")
    void testExecute_LargeQuantity() {
        // Given
        RestockInventoryCommand command = new RestockInventoryCommand(100L, 10000);
        when(inventoryRepository.findByProductId(100L)).thenReturn(Optional.of(testInventory));
        when(inventoryRepository.save(any(Inventory.class))).thenReturn(testInventory);

        // When
        RestockInventoryResult result = useCase.execute(command);

        // Then
        assertTrue(result.isSuccess());
        assertEquals(10020, testInventory.getAvailableQuantity());
    }

    // ===== Validation Tests =====

    @Test
    @DisplayName("Should fail when product ID is null")
    void testExecute_NullProductId() {
        // Given
        RestockInventoryCommand command = new RestockInventoryCommand(null, 50);

        // When
        RestockInventoryResult result = useCase.execute(command);

        // Then
        assertFalse(result.isSuccess());
        assertEquals("Product ID is required", result.getMessage());
        assertEquals("INVALID_PRODUCT_ID", result.getErrorCode());

        verify(inventoryRepository, never()).findByProductId(any());
        verify(inventoryRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should fail when quantity is null")
    void testExecute_NullQuantity() {
        // Given
        RestockInventoryCommand command = new RestockInventoryCommand(100L, null);

        // When
        RestockInventoryResult result = useCase.execute(command);

        // Then
        assertFalse(result.isSuccess());
        assertEquals("Quantity must be greater than 0", result.getMessage());
        assertEquals("INVALID_QUANTITY", result.getErrorCode());
    }

    @Test
    @DisplayName("Should fail when quantity is zero")
    void testExecute_ZeroQuantity() {
        // Given
        RestockInventoryCommand command = new RestockInventoryCommand(100L, 0);

        // When
        RestockInventoryResult result = useCase.execute(command);

        // Then
        assertFalse(result.isSuccess());
        assertEquals("INVALID_QUANTITY", result.getErrorCode());
    }

    @Test
    @DisplayName("Should fail when quantity is negative")
    void testExecute_NegativeQuantity() {
        // Given
        RestockInventoryCommand command = new RestockInventoryCommand(100L, -10);

        // When
        RestockInventoryResult result = useCase.execute(command);

        // Then
        assertFalse(result.isSuccess());
        assertEquals("INVALID_QUANTITY", result.getErrorCode());
    }

    // ===== Inventory Not Found Tests =====

    @Test
    @DisplayName("Should fail when inventory not found")
    void testExecute_InventoryNotFound() {
        // Given
        when(inventoryRepository.findByProductId(100L)).thenReturn(Optional.empty());

        // When
        RestockInventoryResult result = useCase.execute(validCommand);

        // Then
        assertFalse(result.isSuccess());
        assertEquals("Inventory not found for product", result.getMessage());
        assertEquals("INVENTORY_NOT_FOUND", result.getErrorCode());

        verify(inventoryRepository).findByProductId(100L);
        verify(inventoryRepository, never()).save(any());
    }

    // ===== Repository Call Order Tests =====

    @Test
    @DisplayName("Should verify repository is called in correct order")
    void testExecute_RepositoryCallOrder() {
        // Given
        when(inventoryRepository.findByProductId(100L)).thenReturn(Optional.of(testInventory));
        when(inventoryRepository.save(any(Inventory.class))).thenReturn(testInventory);

        // When
        useCase.execute(validCommand);

        // Then - verify order of calls
        var inOrder = inOrder(inventoryRepository);
        inOrder.verify(inventoryRepository).findByProductId(100L);
        inOrder.verify(inventoryRepository).save(any(Inventory.class));
    }

    // ===== Edge Cases =====

    @Test
    @DisplayName("Should handle null available quantity before restock")
    void testExecute_NullAvailableQuantity() {
        // Given
        testInventory.setAvailableQuantity(null);
        when(inventoryRepository.findByProductId(100L)).thenReturn(Optional.of(testInventory));
        when(inventoryRepository.save(any(Inventory.class))).thenReturn(testInventory);

        // When
        RestockInventoryResult result = useCase.execute(validCommand);

        // Then
        assertTrue(result.isSuccess());
        assertEquals(50, testInventory.getAvailableQuantity());
    }
}
